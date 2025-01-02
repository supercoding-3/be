package com.github.p3.mapper;

import com.github.p3.dto.TransactionDto;
import com.github.p3.entity.Product;
import com.github.p3.entity.Transaction;
import com.github.p3.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "product", source = "product")
    @Mapping(target = "buyer", source = "buyer")
    @Mapping(target = "seller", source = "seller")
    @Mapping(target = "transactionPrice", source = "bidPrice")
    @Mapping(target = "completedAt", expression = "java(java.time.LocalDateTime.now())")
    Transaction toTransaction(Product product, User buyer, User seller, BigDecimal bidPrice);

}