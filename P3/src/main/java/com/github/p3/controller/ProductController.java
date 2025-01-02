package com.github.p3.controller;

import com.github.p3.dto.*;
import com.github.p3.entity.Category;
import com.github.p3.entity.User;
import com.github.p3.service.ProductService;
import com.github.p3.service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.github.p3.config.AuthenticatedUser;

import java.util.ArrayList;
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
        System.out.println("Current User: " + currentUser);
        // 서비스에서 상품 정보와 권한 체크를 처리
        ProductDetailDto productDet = productService.getProductInfo(productId, currentUser);

        return ResponseEntity.ok(productDet);  // 상품 수정 페이지로 이동할 때 상품 정보만 반환
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

        // 새로운 이미지는 S3에 업로드
        List<String> newImageUrls = newImages != null ? s3Service.uploadFiles(newImages) : new ArrayList<>();

        // 상품 수정 처리
        productService.updateProduct(productId, productEditDto, newImageUrls, currentUser);

        return ResponseEntity.ok("상품 정보가 수정되었습니다.");
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

    // 상품 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable("id") Long productId,
            @AuthenticatedUser User currentUser) {

        // 서비스 호출
        boolean isDeleted = productService.deleteProduct(productId, currentUser);

        if (isDeleted) {
            return ResponseEntity.ok("상품이 삭제되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("상품 삭제를 실패하였습니다.");
        }

    }

    @PostMapping("/{id}/bid")
    public ResponseEntity<String> bidProduct(
            @PathVariable("id") Long productId,
            @AuthenticatedUser User currentUser,
            @RequestBody BidDto bidDto
    ){
        try{
            // 서비스 호출
            productService.bidProduct(productId, currentUser.getUserEmail(), bidDto);
            return ResponseEntity.ok("입찰이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/award")
    public ResponseEntity<String> completedTransaction(
            @PathVariable("id") Long productId,
            @RequestBody TransactionDto transactionDto,
            @AuthenticatedUser User currentUser
    ) {
        try {
            // 서비스에서 검증 및 트랜잭션 생성
            productService.completedTransaction(productId, transactionDto.getBidId(), currentUser);

            // 응답 메시지: 트랜잭션이 성공적으로 완료되었음을 알림
            return ResponseEntity.ok("낙찰이 완료되었습니다.");

        } catch (EntityNotFoundException e) {
            // 상품이나 입찰 정보가 없을 때 404 반환
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("상품 또는 입찰 정보를 찾을 수 없습니다.");
        } catch (Exception e) {
            // 예기치 않은 오류 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("거래를 완료하는 도중 에러가 발생하였습니다.");
        }
    }

}
