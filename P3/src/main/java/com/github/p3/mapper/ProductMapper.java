package com.github.p3.mapper;

import com.github.p3.dto.CategoryDto;
import com.github.p3.dto.ProductAllDto;
import com.github.p3.dto.ProductRegisterDto;
import com.github.p3.entity.Category;
import com.github.p3.entity.Product;
import com.github.p3.entity.User;
import com.github.p3.repository.UserRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;



@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(ProductRegisterDto productRegisterDto);

    @Mapping(target = "img", expression = "java(product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl())") // 첫 번째 이미지 URL을 img에 매핑
    @Mapping(target = "category", expression = "java(product.getCategory().name())") // Category enum을 String으로 변환
    @Mapping(target = "price", expression = "java(product.getHighestBidPrice() != null ? product.getHighestBidPrice() : product.getImmediatePrice())")
    ProductAllDto toProductAllDto(Product product);


    @Mapping(target = "img", expression = "java(product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl())")  // 첫 번째 이미지 URL을 img에 매핑
    @Mapping(target = "category", expression = "java(product.getCategory().name())")  // Category enum을 String으로 변환
    @Mapping(target = "price", expression = "java(product.getHighestBidPrice() != null ? product.getHighestBidPrice() : product.getImmediatePrice())")  // 입찰가가 있으면 최고 입찰가, 없으면 즉시 구매가
    CategoryDto toCategoryDto(Product product);

}