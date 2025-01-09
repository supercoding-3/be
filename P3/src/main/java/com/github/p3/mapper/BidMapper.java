package com.github.p3.mapper;

import com.github.p3.dto.BidDto;
import com.github.p3.entity.Bid;
import com.github.p3.entity.Product;
import com.github.p3.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BidMapper {
    @Mapping(target = "product", source = "product")
    @Mapping(target = "user", source = "user") // User 엔티티를 설정
    @Mapping(target = "bidPrice", source = "bidDto.bidPrice")
    Bid toEntity(BidDto bidDto, Product product, User user);
}
