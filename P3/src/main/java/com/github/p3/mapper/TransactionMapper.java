package com.github.p3.mapper;

import com.github.p3.dto.TransactionDto;
import com.github.p3.entity.Bid;

import com.github.p3.entity.Product;
import com.github.p3.entity.Transaction;
import com.github.p3.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "product", source = "product")
    @Mapping(target = "buyer", source = "buyer")
    @Mapping(target = "seller", source = "seller")
    @Mapping(target = "transactionPrice", source = "bidPrice")
    @Mapping(target = "completedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "bid", source = "bid")
    Transaction toTransaction(Product product, User buyer, User seller, BigDecimal bidPrice, Bid bid);

}
