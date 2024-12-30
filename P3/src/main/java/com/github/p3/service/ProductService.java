package com.github.p3.service;

import com.github.p3.config.AuthenticatedUser;
import com.github.p3.dto.*;
import com.github.p3.entity.Category;
import com.github.p3.entity.Product;
import com.github.p3.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    void registerProduct(ProductRegisterDto productRegisterDto, List<String> imageUrls, User currentUser);

    ProductDetailResponseDto getProductDetail(Long productId, User currentUser);

    List<ProductAllDto> getAllProducts();

    List<CategoryDto> getProductsByCategory(Category category);

    ProductDetailDto getProductInfo(Long productId, User currentUser);


    void updateProduct(Long productId, ProductEditDto productEditDto, List<String> newImageUrls, User currentUser);

    ProductEditDto getProductByProductId(Long productId);

    boolean deleteProduct(Long productId, User currentUser);

    void bidProduct(Long productId, String userEmail, BidDto bidDto);

    void completedTransaction(Long productId, Integer buyerId, BigDecimal bidPrice, User currentUser);
}
