package com.github.p3.service;

import com.github.p3.dto.*;
import com.github.p3.entity.*;
import com.github.p3.exception.CustomException;
import com.github.p3.exception.ErrorCode;
import com.github.p3.mapper.ProductDetailMapper;
import com.github.p3.mapper.ProductMapper;
import com.github.p3.repository.BidRepository;
import com.github.p3.repository.ImageRepository;
import com.github.p3.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


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

        // 현재 사용자가 판매자인지 여부 확인
        boolean isSeller = product.getUser().equals(currentUser);

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
}

