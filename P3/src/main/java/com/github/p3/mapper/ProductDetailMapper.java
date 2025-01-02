package com.github.p3.mapper;

import com.github.p3.dto.BidInfoDto;
import com.github.p3.dto.ProductDetailResponseDto;
import com.github.p3.entity.Bid;
import com.github.p3.entity.BidStatus;
import com.github.p3.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductDetailMapper {

    @Mapping(target = "imageUrls", ignore = true)  // imageUrls 필드는 수동으로 처리
    @Mapping(target = "latestBid", ignore = true)  // latestBid 필드는 수동으로 처리
    @Mapping(target = "isSeller", ignore = true)  // isSeller 필드는 수동으로 처리
    @Mapping(target = "productStatus", source = "productStatus") // 상품 상태 매핑
    @Mapping(target = "bidStatus", expression = "java(product.getHighestBidPrice() != null ? BidStatus.입찰중 : BidStatus.입찰없음)") // 입찰 상태 매핑
    ProductDetailResponseDto toDto(Product product);


    // 단일 Bid -> BidInfoDto 변환
    default BidInfoDto mapToBidDto(Bid bid) {
        if (bid == null) {
            return null;
        }
        BidInfoDto dto = new BidInfoDto();
        dto.setBidId(bid.getBidId());
        dto.setUserNickname(bid.getUser().getUserNickname()); // User 엔티티에서 닉네임 가져오기
        dto.setBidPrice(bid.getBidPrice());
        dto.setBidCreatedAt(bid.getBidCreatedAt());
        return dto;
    }

    // List<Bid> -> List<BidInfoDto> 변환
    default List<BidInfoDto> mapToBidDtos(List<Bid> allBids) {
        return allBids.stream()
                .map(this::mapToBidDto) // 단일 변환 메서드 활용
                .collect(Collectors.toList());
    }

    default ProductDetailResponseDto toDtoWithAdditionalFields(Product product, List<String> imageUrls, Bid latestBid,List<Bid> allBids, boolean isSeller) {
        ProductDetailResponseDto dto = toDto(product); // 기본 필드 변환

        dto.setImageUrls(imageUrls);
        dto.setIsSeller(isSeller);

        // latestBid를 Bid -> BidInfoDto로 변환하여 설정
        BidInfoDto latestBidDto = mapToBidDto(latestBid);
        dto.setLatestBid(latestBidDto);

        // Bid -> BidDto 변환
        List<BidInfoDto> bidInfoDtos = mapToBidDtos(allBids);
        dto.setAllBids(bidInfoDtos); // BidDto 리스트 설정

        dto.setProductStatus(product.getProductStatus()); // 상품 상태 설정
        dto.setBidStatus(latestBid != null ? BidStatus.입찰중 : BidStatus.입찰없음); // 입찰 상태 설정
        return dto;
    }
}
