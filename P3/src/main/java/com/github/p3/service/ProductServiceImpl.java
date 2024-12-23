package com.github.p3.service;

import com.github.p3.dto.CategoryDto;
import com.github.p3.dto.ProductAllDto;
import com.github.p3.dto.ProductDetailResponseDto;
import com.github.p3.dto.ProductRegisterDto;
import com.github.p3.entity.*;
import com.github.p3.exception.CustomException;
import com.github.p3.exception.ErrorCode;
import com.github.p3.mapper.ProductDetailMapper;
import com.github.p3.mapper.ProductMapper;
import com.github.p3.repository.BidRepository;
import com.github.p3.repository.ImageRepository;
import com.github.p3.repository.ProductRepository;
import com.github.p3.repository.UserRepository;
import com.github.p3.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BidRepository bidRepository;
    private final ProductDetailMapper productDetailMapper;

    @Override
    @Transactional
    public void registerProduct(ProductRegisterDto productRegisterDto, List<String> imageUrls) {       // 엑세스 토큰을 통해 인증된 사용자 정보 가져오기
        // SecurityContext에서 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();  // 인증된 사용자의 이메일을 가져옴

        // 사용자 정보 조회
        User currentUser = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

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
    public ProductDetailResponseDto getProductDetail(Long productId) {
        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 현재 인증된 사용자 정보 가져오기
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));
        // 현재 사용자가 판매자인지 여부 확인
        boolean isSeller = product.getUser().getUserEmail().equals(userEmail);

        // 최신 입찰 조회
        Bid latestBid = bidRepository.findTopByProductProductIdOrderByBidCreatedAtDesc(productId).orElse(null);

        // 상품에 속한 이미지 조회
        List<String> imageUrls = product.getImages().stream()
                .map(Image::getImageUrl)
                .toList();


        return productDetailMapper.toDtoWithAdditionalFields(product, imageUrls, latestBid, isSeller);
    }

    @Override
    @Transactional
    public List<ProductAllDto> getAllProducts() {
        // 모든 상품을 조회하고, DTO로 변환
        return productRepository.findAll().stream()
                .map(productMapper::toProductAllDto)
                .collect(Collectors.toList());
    }


    public User getAuthenticatedUser(String accessToken) {
        // 엑세스 토큰에서 이메일 추출
        String userEmail = jwtTokenProvider.extractUserEmail(accessToken);

        // 이메일로 사용자 조회
        return userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));
    }

    // 카테고리별 상품 조회
    @Override
    @Transactional
    public List<CategoryDto> getProductsByCategory(Category category) {
        return productRepository.findByCategory(category).stream()
                .map(productMapper::toCategoryDto)  // MapStruct를 사용하여 변환
                .collect(Collectors.toList());
    }
}

