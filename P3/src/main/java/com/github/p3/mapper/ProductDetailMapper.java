package com.github.p3.mapper;

import com.github.p3.dto.ProductDetailResponseDto;
import com.github.p3.entity.Bid;
import com.github.p3.entity.BidStatus;
import com.github.p3.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductDetailMapper {

    @Mapping(target = "imageUrls", ignore = true)  // imageUrls 필드는 수동으로 처리
    @Mapping(target = "latestBid", ignore = true)  // latestBid 필드는 수동으로 처리
    @Mapping(target = "isSeller", ignore = true)  // isSeller 필드는 수동으로 처리
    @Mapping(target = "productStatus", source = "productStatus") // 상품 상태 매핑
    @Mapping(target = "bidStatus", expression = "java(product.getHighestBidPrice() != null ? BidStatus.입찰 : BidStatus.입찰없음)") // 입찰 상태 매핑
    ProductDetailResponseDto toDto(Product product);

    default ProductDetailResponseDto toDtoWithAdditionalFields(Product product, List<String> imageUrls, Bid latestBid, boolean isSeller) {
        ProductDetailResponseDto dto = toDto(product); // 기본 필드 변환
        dto.setImageUrls(imageUrls);
        dto.setLatestBid(latestBid);
        dto.setIsSeller(isSeller);
        dto.setProductStatus(product.getProductStatus()); // 상품 상태 설정
        dto.setBidStatus(latestBid != null ? BidStatus.입찰 : BidStatus.입찰없음); // 입찰 상태 설정
        return dto;
    }
}
