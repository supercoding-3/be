package com.github.p3.service;

import com.github.p3.dto.MyPageResponseDto;
import com.github.p3.dto.ProductDto;
import com.github.p3.entity.ProductStatus;
import com.github.p3.entity.User;

import java.util.List;

public interface MyPageService {
    MyPageResponseDto getMyPageInfo(User currentUser);

    List<ProductDto> getBidProductsByStatus(Integer userId, ProductStatus status);

    List<ProductDto> getSoldProductsByStatus(Integer userId, ProductStatus status);

    boolean cancelBid(Long bidId, Integer userId);

    boolean cancelTransaction(Long transactionId, Integer sellerId);
}
