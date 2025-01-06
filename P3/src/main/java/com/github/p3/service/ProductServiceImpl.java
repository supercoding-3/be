package com.github.p3.service;

import com.github.p3.dto.*;
import com.github.p3.entity.*;
import com.github.p3.exception.CustomException;
import com.github.p3.exception.ErrorCode;
import com.github.p3.mapper.BidMapper;
import com.github.p3.mapper.ProductDetailMapper;
import com.github.p3.mapper.ProductMapper;
import com.github.p3.mapper.TransactionMapper;
import com.github.p3.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ImageRepository imageRepository;
    private final BidRepository bidRepository;
    private final ProductDetailMapper productDetailMapper;
    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final BidMapper bidMapper;
    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);


    @Override
    @Transactional
    public void registerProduct(ProductRegisterDto productRegisterDto, List<String> imageUrls, User currentUser) {
        // ProductRegisterDto -> Product 변환 (User 포함)
        Product product = productMapper.toEntity(productRegisterDto);
        product.setUser(currentUser);  // 사용자 정보 설정

        // 상품 저장
        productRepository.save(product);

        // 이미지 엔티티 생성 및 저장
        for (String imageUrl : imageUrls) {
            Image image = new Image();
            image.setProduct(product); // 상품과 이미지 연결
            image.setImageUrl(imageUrl); // 이미지 URL 설정
            imageRepository.save(image); // 이미지 저장
        }
    }

    @Override
    @Transactional
    public ProductDetailResponseDto getProductDetail(Long productId, User currentUser) {
        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        log.debug("Product ID: {}", productId);
        log.debug("Product Title: {}", product.getTitle());

        // 현재 사용자가 판매자인지 여부 확인
        boolean isSeller = product.getUser().equals(currentUser);
        log.debug("Current User: {}", currentUser.getUserEmail());
        log.debug("Is Seller: {}", isSeller);

        // 최신 입찰 조회
        Bid latestBid = bidRepository.findTopByProductProductIdOrderByBidCreatedAtDesc(productId).orElse(null);
        log.debug("Latest Bid: {}", latestBid);
        // 모든 입찰 목록 조회
        List<Bid> allBids = bidRepository.findByProductProductIdOrderByBidCreatedAtDesc(productId);
        log.debug("All Bids Count: {}", allBids.size());

        // 상품에 속한 이미지 조회
        List<String> imageUrls = product.getImages().stream()
                .map(Image::getImageUrl)
                .toList();

        log.debug("Image URLs Count: {}", imageUrls.size());

        return productDetailMapper.toDtoWithAdditionalFields(product, imageUrls, latestBid, allBids, isSeller);
    }

    @Override
    @Transactional
    public List<ProductAllDto> getAllProducts() {
        // 모든 상품을 조회하고, DTO로 변환
        return productRepository.findAll().stream()
                .map(productMapper::toProductAllDto)
                .collect(Collectors.toList());
    }


    // 카테고리별 상품 조회
    @Override
    @Transactional
    public List<CategoryDto> getProductsByCategory(Category category) {
        return productRepository.findByCategory(category).stream()
                .map(productMapper::toCategoryDto)  // MapStruct를 사용하여 변환
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDetailDto getProductInfo(Long productId, User currentUser) {
        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 현재 사용자가 판매자인지 확인
        if (!product.getUser().equals(currentUser)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);  // 권한이 없는 경우 예외 처리
        }

        // ProductEditDto로 매핑하여 반환
        return productMapper.toProductDetailDto(product);
    }

    @Override
    @Transactional
    public void updateProduct(Long productId, ProductEditDto productEditDto, List<String> newImageUrls, User currentUser) {
        // 기존 상품 조회
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 권한 확인
        if (!existingProduct.getUser().equals(currentUser)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 상품 정보 업데이트 (수정된 필드만 반영)
        if (productEditDto.getTitle() != null) {
            existingProduct.setTitle(productEditDto.getTitle());
        }
        if (productEditDto.getDescription() != null) {
            existingProduct.setDescription(productEditDto.getDescription());
        }
        if (productEditDto.getStartingBidPrice() != null) {
            existingProduct.setStartingBidPrice(productEditDto.getStartingBidPrice());
        }
        if (productEditDto.getImmediatePrice() != null) {
            existingProduct.setImmediatePrice(productEditDto.getImmediatePrice());
        }
        if (productEditDto.getCategory() != null) {
            existingProduct.setCategory(productEditDto.getCategory());
        }
        if (productEditDto.getProductEndDate() != null) {
            existingProduct.setProductEndDate(productEditDto.getProductEndDate());
        }

        // 새로운 이미지 추가 (null 체크 후)
        if (newImageUrls != null && !newImageUrls.isEmpty()) {
            for (String newImageUrl : newImageUrls) {
                Image image = new Image();
                image.setImageUrl(newImageUrl);
                image.setProduct(existingProduct);
                imageRepository.save(image); // DB에 저장
            }
        }

        // 상품 정보 저장 (수정된 엔티티 저장)
        productRepository.save(existingProduct);
    }

    @Override
    @Transactional
    public ProductEditDto getProductByProductId(Long productId) {
        // 상품을 DB에서 찾기
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // Product -> ProductEditDto로 변환하여 반환
        return productMapper.toProductEditDto(product);
    }

    @Override
    @Transactional
    public boolean deleteProduct(Long productId, User currentUser) {
        // 상품 조회
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 권한 확인: 상품의 주인과 현재 사용자가 일치하는지 확인
        if (!existingProduct.getUser().equals(currentUser)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // S3에서 파일 삭제 (상품에 연결된 이미지들이 있을 경우)
        if (existingProduct.getImages() != null) {
            for (Image image : existingProduct.getImages()) {
                if (image.getImageUrl() != null) {
                    // S3에서 파일 삭제
                    s3Service.deleteFileFromS3(image.getImageUrl());
                }
            }
        }
        // 상품 삭제
        productRepository.delete(existingProduct);
        return true;
    }

    @Override
    @Transactional
    public void bidProduct(Long productId, String userEmail, BidDto bidDto) {
        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // userId로 User 객체 조회
        User user = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

        // 입찰 금액 검증
        BigDecimal bidPrice = bidDto.getBidPrice();

        // 최초 입찰일 경우
        if (product.getBids().isEmpty()) {
            if (bidPrice.compareTo(product.getStartingBidPrice()) <= 0) {
                throw new CustomException(ErrorCode.INVALID_BID_AMOUNT);
            }
        } else {
            // 기존 입찰이 있을 경우, 가장 높은 입찰 금액보다 커야 함
            BigDecimal highestBidPrice = product.getHighestBidPrice();
            if (highestBidPrice != null && bidPrice.compareTo(highestBidPrice) <= 0) {
                throw new CustomException(ErrorCode.INVALID_BID_AMOUNT_LOW);
            }
        }

        // 입찰 객체 생성 (BidMapper 사용)
        Bid bid = bidMapper.toEntity(bidDto, product, user);

        // 입찰 저장
        bidRepository.save(bid);

    }

    @Override
    @Transactional
    public List<ProductAllDto> searchProductsByTitle(String title) {
        List<Product> products = productRepository.findByTitleContainingIgnoreCase(title);
        return products.stream()
                .map(productMapper::toProductAllDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void completedTransaction(Long productId, Long bidId, User currentUser) {
        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 현재 사용자가 판매자인지 확인
        if (!product.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);  // 권한이 없는 경우 예외 처리
        }

        // 입찰 정보 조회
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new CustomException(ErrorCode.BID_NOT_FOUND));

        // 상품과 입찰이 매칭되는지 확인
        if (!bid.getProduct().getProductId().equals(product.getProductId())) {
            throw new CustomException(ErrorCode.INVALID_BID);  // 잘못된 입찰 정보
        }

        // 입찰자 정보 가져오기 (구매자 정보)
        User buyer = bid.getUser();  // 여기서 구매자 정보 추출

        // 입찰 상태를 '낙찰'로 변경
        bid.setBidStatus(BidStatus.낙찰);
        bidRepository.save(bid);

        // 상품 상태를 '낙찰'로 변경
        product.setProductStatus(ProductStatus.낙찰);
        productRepository.save(product);

        Transaction transaction = transactionMapper.toTransaction(product, buyer, currentUser, bid.getBidPrice());
        transaction.setStatus(TransactionStatus.거래중); // 초기 상태 설정
        // 트랜잭션 저장
        transactionRepository.save(transaction);

    }
}

