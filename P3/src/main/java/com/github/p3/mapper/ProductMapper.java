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

    // ProductRegisterDto -> Product 변환
    @Mapping(source = "userId", target = "user")
    Product toEntity(ProductRegisterDto productRegisterDto, @Context UserRepository userRepository);

    // Integer userId -> User 변환
    default User map(Integer userId, @Context UserRepository userRepository) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}