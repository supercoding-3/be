package com.github.p3.service;

import com.github.p3.dto.ProductAllDto;
import com.github.p3.dto.ProductDetailResponseDto;
import com.github.p3.dto.ProductRegisterDto;
import com.github.p3.dto.ProductResponseDto;
import com.github.p3.entity.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    void registerProduct(ProductRegisterDto productRegisterDto, List<String> imageUrls);

    ProductDetailResponseDto getProductDetail(Long productId);

    List<ProductAllDto> getAllProducts();
}
