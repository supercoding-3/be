package com.github.p3.mapper;

import com.github.p3.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.p3.entity.Image;
import com.github.p3.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(ProductRegisterDto productRegisterDto);

    @Mapping(target = "img", expression = "java(product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl())") // 첫 번째 이미지 URL을 img에 매핑
    @Mapping(target = "category", expression = "java(product.getCategory().name())") // Category enum을 String으로 변환
    @Mapping(target = "price", expression = "java(product.getHighestBidPrice() != null ? product.getHighestBidPrice() : product.getImmediatePrice())") // 입찰가 또는 즉시 구매가
    @Mapping(target = "productStatus", source = "productStatus") // 상품 상태 매핑
    ProductAllDto toProductAllDto(Product product);


    @Mapping(target = "img", expression = "java(product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl())")  // 첫 번째 이미지 URL을 img에 매핑
    @Mapping(target = "category", expression = "java(product.getCategory().name())")  // Category enum을 String으로 변환
    @Mapping(target = "price", expression = "java(product.getHighestBidPrice() != null ? product.getHighestBidPrice() : product.getImmediatePrice())")  // 입찰가 또는 즉시 구매가
    @Mapping(target = "productStatus", source = "productStatus") // 상품 상태 매핑
    CategoryDto toCategoryDto(Product product);

    @Mapping(target = "existingImageUrls", expression = "java(product.getImages() != null ? product.getImages().stream().map(image -> image.getImageUrl()).collect(java.util.stream.Collectors.toList()) : java.util.Collections.emptyList())")
    ProductDetailDto toProductDetailDto(Product product);

    // ProductEditDto -> Product 변환
    @Mapping(target = "user", ignore = true)  // 사용자 정보는 컨트롤러에서 설정
    Product toEntity(ProductEditDto productEditDto);

    // Product -> ProductEditDto 변환 (이 부분을 추가)
    @Mapping(target = "existingImageUrls", expression = "java(product.getImages().stream().map(image -> image.getImageUrl()).collect(java.util.stream.Collectors.toList()))") // 기존 이미지 URL 매핑
    ProductEditDto toProductEditDto(Product product);  // 추가된 메서드
}