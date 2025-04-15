package com.github.p3.controller;

import com.github.p3.dto.*;
import com.github.p3.entity.Category;
import com.github.p3.entity.User;
import com.github.p3.service.ProductService;
import com.github.p3.service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.github.p3.config.AuthenticatedUser;


import java.util.List;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {


    private final ProductService productService;
    private final S3Service s3Service;


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
        log.info("현재 사용자: {}", currentUser);
        ProductDetailResponseDto productDetail = productService.getProductDetail(productId, currentUser);
        return ResponseEntity.ok(productDetail);
    }


    @PatchMapping("/{id}/edit")
    public ResponseEntity<String> updateProduct(
            @PathVariable("id") Long productId,
            @RequestPart(value = "product", required = false) ProductEditDto productEditDto, // 수정할 상품 정보
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages, // 새로운 이미지
            @AuthenticatedUser User currentUser) {

        // productEditDto가 null이면 기존의 상품 정보를 그대로 사용하도록 처리
        if (productEditDto == null) {
            // 예를 들어, 기존 상품 정보를 가져오는 서비스 메소드 호출
            productEditDto = productService.getProductByProductId(productId);
            if (productEditDto == null) {
                return ResponseEntity.badRequest().body("Product not found");
            }
        }

        // 상품 수정 처리
        productService.updateProduct(productId, productEditDto, newImages, currentUser);

        return ResponseEntity.ok("상품 정보가 수정되었습니다.");
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ProductAllDto>> getAllProducts(
            @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 서비스에서 상품 목록을 가져옴
        Page<ProductAllDto> products = productService.getAllProducts(pageable);

        // ResponseEntity로 감싸서 반환
        return ResponseEntity.ok(products); // 200 OK 상태 코드와 함께 반환
    }

    // 카테고리별 상품 조회
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<CategoryDto>> getProductsByCategory(@PathVariable("category") Category category,
                                                                   @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable){
        Page<CategoryDto> products = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(products);
    }

    // 상품 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable("id") Long productId,
            @AuthenticatedUser User currentUser) {

        // 서비스 호출
        boolean isDeleted = productService.deleteProduct(productId, currentUser);

        return ResponseEntity.ok("상품이 삭제되었습니다.");

    }

    @PostMapping("/{id}/bid")
    public ResponseEntity<String> bidProduct(
            @PathVariable("id") Long productId,
            @AuthenticatedUser User currentUser,
            @RequestBody BidDto bidDto
    ){
            productService.bidProduct(productId, currentUser.getUserEmail(), bidDto);
            return ResponseEntity.ok("입찰이 완료되었습니다.");

    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductAllDto>> searchProducts(@RequestParam String title) {
        List<ProductAllDto> products = productService.searchProductsByTitle(title);
        return ResponseEntity.ok(products);
    }
  
    @PostMapping("/{id}/award")
    public ResponseEntity<String> completedTransaction(
            @PathVariable("id") Long productId,
            @RequestBody TransactionDto transactionDto,
            @AuthenticatedUser User currentUser
    ) {
        // 서비스에서 검증 및 트랜잭션 생성
        productService.completedTransaction(productId, transactionDto.getBidId(), currentUser);

        // 응답 메시지: 트랜잭션이 성공적으로 완료되었음을 알림
        return ResponseEntity.ok("낙찰이 완료되었습니다.");


    }

}
