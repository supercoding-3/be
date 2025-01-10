package com.github.p3.mapper;

import com.github.p3.dto.ProductDto;
import com.github.p3.entity.Bid;
import com.github.p3.entity.Product;
import com.github.p3.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface MyPageMapper {

    @Mapping(target = "img", expression = "java(product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl())")
    @Mapping(target = "price", expression = "java(bid.getBidPrice())")
    @Mapping(target = "productStatus", expression = "java(isWinningBid ? \"낙찰\" : \"입찰중\")")
    ProductDto toBidProductDto(Product product, Bid bid, boolean isWinningBid);


    // 판매된 상품에 대한 매핑 (상태 및 첫 번째 이미지 URL 처리)
    @Mapping(target = "img", expression = "java(product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl())")  // 첫 번째 이미지 URL
    @Mapping(target = "price", expression = "java(highestBidPrice)")  // 가장 높은 입찰 가격
    @Mapping(target = "productStatus", source = "product.productStatus")  // 상품 상태 매핑 (Product 테이블의 productStatus)
    ProductDto toSoldProductDto(Product product, BigDecimal highestBidPrice);
}