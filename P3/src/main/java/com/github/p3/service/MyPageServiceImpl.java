package com.github.p3.service;

import com.github.p3.dto.MyPageResponseDto;
import com.github.p3.dto.ProductDto;
import com.github.p3.dto.UserProfileUpdateDto;
import com.github.p3.entity.*;
import com.github.p3.exception.CustomException;
import com.github.p3.exception.ErrorCode;
import com.github.p3.mapper.MyPageMapper;
import com.github.p3.repository.BidRepository;
import com.github.p3.repository.ProductRepository;
import com.github.p3.repository.TransactionRepository;
import com.github.p3.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService {

    private final MyPageMapper myPageMapper;
    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final PasswordEncoder passwordEncoder;

    @Transactional  // 트랜잭션 처리
    @Override
    public MyPageResponseDto getMyPageInfo(User currentUser) {
        Integer userId = currentUser.getUserId();

        // 입찰한 상품 목록 가져오기, 없으면 빈 리스트 반환
        List<ProductDto> bidProducts = getBidProducts(userId);
        if (bidProducts == null) {
            bidProducts = new ArrayList<>();
        }

        // 판매한 상품 목록 가져오기, 없으면 빈 리스트 반환
        List<ProductDto> soldProducts = getSoldProducts(userId);
        if (soldProducts == null) {
            soldProducts = new ArrayList<>();
        }

        // MyPageResponseDto 반환 (닉네임 추가)
        return new MyPageResponseDto(bidProducts, soldProducts, currentUser.getUserNickname());
    }

    public List<ProductDto> getBidProducts(Integer userId) {
        // 사용자가 입찰한 상품 목록 조회
        List<Bid> bids = bidRepository.findByUser_UserId(userId);

        return bids.stream()
                .map(bid -> {
                    Product product = bid.getProduct();
                    boolean isWinningBid = isWinningBid(product, userId);

                    // 낙찰된 상품과 그렇지 않은 상품을 구분
                    return myPageMapper.toBidProductDto(product, bid, isWinningBid);
                })
                .collect(Collectors.toList());
    }

    public List<ProductDto> getSoldProducts(Integer userId) {
        // 판매한 상품 목록 조회 (Product 테이블에서 직접 조회)
        List<Product> products = productRepository.findByUser_UserId(userId);

        // Product -> ProductDto 변환
        return products.stream()
                .map(product -> {
                    // 해당 상품의 가장 비싼 입찰 가격을 가져오기
                    BigDecimal highestBidPrice = product.getHighestBidPrice();
                    return myPageMapper.toSoldProductDto(product, highestBidPrice);
                })
                .collect(Collectors.toList());
    }

    // 낙찰 여부 확인 메서드
    private boolean isWinningBid(Product product, Integer userId) {
        // 거래 테이블에서 낙찰자 확인
        return transactionRepository.findByProduct_ProductId(product.getProductId())
                .map(transaction -> transaction.getBuyer().getUserId().equals(userId))
                .orElse(false);

    }

    @Transactional
    public List<ProductDto> getBidProductsByStatus(Integer userId, ProductStatus status) {
        // 상태에 맞는 입찰 상품 목록 조회
        List<Bid> bids = bidRepository.findByUser_UserId(userId);

        return bids.stream()
                .map(bid -> {
                    Product product = bid.getProduct();
                    boolean isWinningBid = isWinningBid(product, userId);

                    // 상태별 필터링
                    if (!product.getProductStatus().equals(status)) {  // ProductStatus로 비교
                        return null;
                    }

                    // 낙찰된 상품과 그렇지 않은 상품을 구분
                    return myPageMapper.toBidProductDto(product, bid, isWinningBid);
                })
                .filter(dto -> dto != null)  // null 값 제거
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ProductDto> getSoldProductsByStatus(Integer userId, ProductStatus status) {
        // 상태에 맞는 판매한 상품 목록 조회
        List<Product> products = productRepository.findByUser_UserId(userId);

        return products.stream()
                .map(product -> {
                    // 상태별 필터링
                    if (!product.getProductStatus().equals(status)) {
                        return null;
                    }

                    // 해당 상품의 가장 비싼 입찰 가격을 가져오기
                    BigDecimal highestBidPrice = product.getHighestBidPrice();

                    // 해당 상품을 ProductDto로 변환 (가격 포함)
                    return myPageMapper.toSoldProductDto(product, highestBidPrice);
                })
                .filter(dto -> dto != null)  // null 값 제거
                .collect(Collectors.toList());
    }

    @Override
    public boolean cancelBid(Long bidId, Integer userId) {
        // Bid 객체를 DB에서 조회
        Optional<Bid> bidOpt = bidRepository.findById(bidId);

        if (bidOpt.isPresent()) {
            Bid bid = bidOpt.get();

            // 입찰자와 현재 사용자가 일치하는지 확인
            if (!bid.getUser().getUserId().equals(userId)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
            }

            // 입찰 기록 삭제
            bidRepository.delete(bid);

            return true;
        }

        return false;
    }

    // 거래 취소 처리
    @Override
    @Transactional
    public boolean cancelTransaction(Long transactionId, Integer sellerId) {
        // 거래 조회
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);

        if (transactionOpt.isEmpty()) {
            return false;  // 거래가 존재하지 않으면 실패
        }

        Transaction transaction = transactionOpt.get();

        // 판매자와 거래의 판매자가 일치하는지 확인
        if (!transaction.getSeller().getUserId().equals(sellerId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 거래 상태가 "거래중"일 때만 취소 가능
        if (transaction.getStatus() != TransactionStatus.거래중) {
            throw new CustomException(ErrorCode.TRANSACTION_NOT_FOUND);
        }

        // 거래 삭제
        transactionRepository.delete(transaction);

        // 상품 상태 업데이트: 입찰중으로 변경
        Product product = transaction.getProduct();
        product.setProductStatus(ProductStatus.입찰중);
        productRepository.save(product);


        // 해당 거래와 연결된 입찰 삭제
        Long bidId = transaction.getBid() != null ? transaction.getBid().getBidId() : null;
        if (bidId != null) {
            bidRepository.deleteById(bidId);  // ID로 입찰 삭제
        }

        return true;  // 성공적으로 거래 취소 및 상태 업데이트
    }


    @Transactional
    @Override
    public void updateUserProfile(MultipartFile newImage, User currentUser) {

        // 유저 정보 조회
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 새 이미지 업로드
        String newImageUrl = s3Service.uploadSingleFile(newImage);

        // 기존 이미지 삭제 (선택 사항)
        if (user.getProfileImageUrl() != null) {
            s3Service.deleteFileFromS3(user.getProfileImageUrl());
        }

        // 새 이미지 URL 저장
        user.setProfileImageUrl(newImageUrl);

        // 변경된 유저 정보 저장
        userRepository.save(user);

        System.out.println("User profile updated with new image URL: " + newImageUrl);
    }

    @Transactional
    @Override
    public void updateUserProfile(User currentUser, UserProfileUpdateDto dto) {

        // 사용자 정보 수정 로직
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (dto.getPassword() != null) {
            if (!dto.getPassword().equals(dto.getConfirmPassword())) {
                throw new CustomException(ErrorCode.INCORRECT_PASSWORD);
            }
            user.setUserPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // 이메일, 닉네임, 전화번호 등 다른 정보 수정
        if (dto.getEmail() != null) {
            user.setUserEmail(dto.getEmail());
        }
        if (dto.getNickname() != null) {
            user.setUserNickname(dto.getNickname());
        }
        if (dto.getPhoneNumber() != null) {
            user.setUserPhone(dto.getPhoneNumber());
        }

        // 변경된 유저 정보 저장
        userRepository.save(user);
    }
}

