package com.github.p3.controller;


import com.github.p3.config.AuthenticatedUser;
import com.github.p3.dto.MyPageResponseDto;
import com.github.p3.dto.ProductDto;
import com.github.p3.dto.UserProfileUpdateDto;
import com.github.p3.entity.ProductStatus;
import com.github.p3.entity.User;
import com.github.p3.service.MyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/my-page")
    public ResponseEntity<MyPageResponseDto> getMyPage(@AuthenticatedUser User currentUser) {
        // MyPageService를 통해 마이페이지 정보 가져오기
        MyPageResponseDto myPageInfo = myPageService.getMyPageInfo(currentUser);

        // ResponseEntity로 MyPageResponseDto 반환
        return ResponseEntity.ok(myPageInfo);
    }


    @GetMapping("/my-page/bought")
    public MyPageResponseDto getBoughtProductsByStatus(@AuthenticatedUser User currentUser, @RequestParam ProductStatus status) {
        Integer userId = currentUser.getUserId();

        // 상태에 맞는 구매한 상품을 조회
        List<ProductDto> bidProducts = myPageService.getBidProductsByStatus(userId, status);

        // MyPageResponseDto 반환 (빈 리스트 처리)
        return new MyPageResponseDto(bidProducts != null ? bidProducts : new ArrayList<>(), new ArrayList<>(), currentUser.getUserNickname());
    }


    @GetMapping("/my-page/sold")
    public MyPageResponseDto getSoldProductsByStatus(@AuthenticatedUser User currentUser, @RequestParam ProductStatus status) {
        Integer userId = currentUser.getUserId();

        // 상태에 맞는 판매한 상품을 조회
        List<ProductDto> soldProducts = myPageService.getSoldProductsByStatus(userId, status);

        // MyPageResponseDto 반환
        return new MyPageResponseDto(new ArrayList<>(), soldProducts, currentUser.getUserNickname());
    }

    @PostMapping("/my-page/bid/cancel")
    public ResponseEntity<String> cancelBid(@AuthenticatedUser User currentUser, @RequestParam Long bidId) {
        Integer userId = currentUser.getUserId();

        // 입찰 취소 처리 로직
        boolean isCancelled = myPageService.cancelBid(bidId, userId);

        if (isCancelled) {
            return ResponseEntity.ok("입찰이 취소되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("입찰 취소 실패");
        }
    }

    // 거래 취소 요청 처리
    @PostMapping("/my-page/transaction/cancel")
    public ResponseEntity<String> cancelTransaction(
            @AuthenticatedUser User currentUser,
            @RequestParam Long transactionId) {

        Integer sellerId = currentUser.getUserId();  // 현재 로그인된 판매자의 ID

        // 거래 취소 처리
        boolean isCancelled = myPageService.cancelTransaction(transactionId, sellerId);

        if (isCancelled) {
            return ResponseEntity.ok("낙찰 취소가 완료되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("낙찰 취소 실패");
        }
    }

    @GetMapping("/my-page/edit")
    public ResponseEntity<Map<String, String>> showUserEditPage() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "회원정보 수정 페이지로 이동합니다.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/my-page/edit/profile")
    public ResponseEntity<String> updateUserProfile(
            @RequestParam("newImage") MultipartFile newImage,
            @AuthenticatedUser User currentUser) {

        myPageService.updateUserProfile(newImage, currentUser);
        return ResponseEntity.ok("유저 프로필 사진 업데이트가 완료되었습니다.");
    }


    @PatchMapping("/my-page/edit")
    public ResponseEntity<String> updateUserProfile(
            @RequestBody UserProfileUpdateDto dto,  // 요청 본문에서 수정할 정보를 받음
            @AuthenticatedUser User currentUser) {  // 인증된 사용자의 정보

        // 서비스 로직 호출하여 사용자 정보 수정
        myPageService.updateUserProfile(currentUser, dto);

        return ResponseEntity.ok("유저 프로필이 성공적으로 변경되었습니다.");
    }
}
