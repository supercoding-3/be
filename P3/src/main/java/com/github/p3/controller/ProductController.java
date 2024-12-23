package com.github.p3.controller;

import com.github.p3.dto.ProductAllDto;
import com.github.p3.dto.ProductDetailResponseDto;
import com.github.p3.dto.ProductRegisterDto;
import com.github.p3.dto.ProductResponseDto;
import com.github.p3.entity.Product;
import com.github.p3.entity.User;
import com.github.p3.repository.ProductRepository;
import com.github.p3.repository.UserRepository;
import com.github.p3.service.ProductService;
import com.github.p3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.github.p3.entity.Image;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {


    private final ProductService productService;
    private final S3Service s3Service;


    @GetMapping("/register")
    public ResponseEntity<Map<String, String>> showProductRegisterPage() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "상품 등록 페이지로 이동합니다.");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/register")
    public ResponseEntity<String> registerProduct(@RequestPart("product") ProductRegisterDto productRegisterDto,
                                                  @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        // 이미지 업로드 및 URL 생성
        List<String> imageUrls = s3Service.uploadFiles(images);

        // 상품 등록 처리
        productService.registerProduct(productRegisterDto, imageUrls);

        return ResponseEntity.status(HttpStatus.CREATED).body("판매 등록이 완료되었습니다.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponseDto> getProductDetail(@PathVariable("id") Long productId) {
        ProductDetailResponseDto productDetail = productService.getProductDetail(productId);
        return ResponseEntity.ok(productDetail);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductAllDto>> getAllProducts() {
        // 서비스에서 상품 목록을 가져옴
        List<ProductAllDto> products = productService.getAllProducts();

        // ResponseEntity로 감싸서 반환
        return ResponseEntity.ok(products); // 200 OK 상태 코드와 함께 반환
    }
}
