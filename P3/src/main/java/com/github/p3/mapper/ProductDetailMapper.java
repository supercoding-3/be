package com.github.p3.mapper;

import com.github.p3.dto.ProductDetailResponseDto;
import com.github.p3.entity.Bid;
import com.github.p3.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductDetailMapper {

    // Product 엔티티를 ProductDetailResponseDto로 변환
    @Mapping(target = "imageUrls", ignore = true)  // imageUrls 필드는 수동으로 처리할 것이므로 무시
    @Mapping(target = "latestBid", ignore = true)  // latestBid 필드는 수동으로 처리할 것이므로 무시
    @Mapping(target = "isSeller", ignore = true)  // isSeller 필드는 수동으로 처리할 것이므로 무시
    ProductDetailResponseDto toDto(Product product);

    // 추가 필드 설정을 위한 메서드
    default ProductDetailResponseDto toDtoWithAdditionalFields(Product product, List<String> imageUrls, Bid latestBid, boolean isSeller) {
        ProductDetailResponseDto dto = toDto(product); // 기본 필드 변환
        dto.setImageUrls(imageUrls);
        dto.setLatestBid(latestBid);
        dto.setIsSeller(isSeller);
        return dto;
    }
}
