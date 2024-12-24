package com.github.p3.controller;

import com.github.p3.dto.*;
import com.github.p3.entity.Category;
import com.github.p3.entity.User;
import com.github.p3.service.ProductService;
import com.github.p3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.github.p3.config.AuthenticatedUser;

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
    public ResponseEntity<String> registerProduct(
            @RequestPart("product") ProductRegisterDto productRegisterDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticatedUser User currentUser) {

        // 이미지 업로드 및 URL 생성
        List<String> imageUrls = s3Service.uploadFiles(images);

        // 상품 등록 처리
        productService.registerProduct(productRegisterDto, imageUrls, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body("판매 등록이 완료되었습니다.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponseDto> getProductDetail(@PathVariable("id") Long productId, @AuthenticatedUser User currentUser) {
        ProductDetailResponseDto productDetail = productService.getProductDetail(productId, currentUser);
        return ResponseEntity.ok(productDetail);
    }

    // 상품 수정 페이지로 이동
    @GetMapping("/{id}/edit")
    public ResponseEntity<ProductDetailDto> getProductEdit(@PathVariable("id") Long productId, @AuthenticatedUser User currentUser) {
        // 서비스에서 상품 정보와 권한 체크를 처리
        ProductDetailDto productDet = productService.getProductInfo(productId, currentUser);

        return ResponseEntity.ok(productDet);  // 상품 수정 페이지로 이동할 때 상품 정보만 반환
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductAllDto>> getAllProducts() {
        // 서비스에서 상품 목록을 가져옴
        List<ProductAllDto> products = productService.getAllProducts();

        // ResponseEntity로 감싸서 반환
        return ResponseEntity.ok(products); // 200 OK 상태 코드와 함께 반환
    }

    // 카테고리별 상품 조회
    @GetMapping("/category/{category}")
    public List<CategoryDto> getProductsByCategory(@PathVariable("category") Category category) {
        return productService.getProductsByCategory(category);
    }


}
