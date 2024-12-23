package com.github.p3.mapper;

import com.github.p3.dto.ProductRegisterDto;
import com.github.p3.entity.Product;
import com.github.p3.entity.User;
import com.github.p3.repository.UserRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;



@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(ProductRegisterDto productRegisterDto);


}