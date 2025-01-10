package com.github.p3.service;

import com.github.p3.dto.MyPageResponseDto;
import com.github.p3.dto.ProductDto;
import com.github.p3.dto.UserProfileUpdateDto;
import com.github.p3.entity.ProductStatus;
import com.github.p3.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MyPageService {
    MyPageResponseDto getMyPageInfo(User currentUser);

    List<ProductDto> getBidProductsByStatus(Integer userId, ProductStatus status);

    List<ProductDto> getSoldProductsByStatus(Integer userId, ProductStatus status);

    boolean cancelBid(Long bidId, Integer userId);

    boolean cancelTransaction(Long transactionId, Integer sellerId);

    void updateUserProfile(MultipartFile newImage, User currentUser);

    void updateUserProfile(User currentUser,UserProfileUpdateDto dto);
}
