package com.github.p3.mapper;

import com.github.p3.dto.*;

import java.util.stream.Collectors;
import com.github.p3.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;



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

    // 상품 상세 조회용 매핑 (상품 정보 조회)
    @Mapping(target = "existingImageUrls", expression = "java(product.getImages().stream().map(image -> image.getImageUrl()).collect(java.util.stream.Collectors.toList()))") // 기존 이미지 URL 매핑
    ProductDetailDto toProductDetailDto(Product product);

}