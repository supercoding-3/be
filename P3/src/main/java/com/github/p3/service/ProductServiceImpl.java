package com.github.p3.service;

import com.github.p3.dto.ProductDetailResponseDto;
import com.github.p3.dto.ProductRegisterDto;
import com.github.p3.entity.Image;
import com.github.p3.entity.Product;
import com.github.p3.entity.User;
import com.github.p3.exception.CustomException;
import com.github.p3.exception.ErrorCode;
import com.github.p3.mapper.ProductMapper;
import com.github.p3.repository.ImageRepository;
import com.github.p3.repository.ProductRepository;
import com.github.p3.repository.UserRepository;
import com.github.p3.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public void registerProduct(ProductRegisterDto productRegisterDto, List<String> imageUrls) {       // 엑세스 토큰을 통해 인증된 사용자 정보 가져오기
        // SecurityContext에서 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();  // 인증된 사용자의 이메일을 가져옴

        // 사용자 정보 조회
        User currentUser = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

        // ProductRegisterDto -> Product 변환 (User 포함)
        Product product = productMapper.toEntity(productRegisterDto);
        product.setUser(currentUser);  // 사용자 정보 설정

        // 상품 저장
        productRepository.save(product);

        // 이미지 엔티티 생성 및 저장
        for (String imageUrl : imageUrls) {
            Image image = new Image();
            image.setProduct(product); // 상품과 이미지 연결
            image.setImageUrl(imageUrl); // 이미지 URL 설정
            imageRepository.save(image); // 이미지 저장
        }
    }

    @Override
    public ProductDetailResponseDto getProductDetail(Long productId) {
        return null;
    }


    public User getAuthenticatedUser(String accessToken) {
        // 엑세스 토큰에서 이메일 추출
        String userEmail = jwtTokenProvider.extractUserEmail(accessToken);

        // 이메일로 사용자 조회
        return userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));
    }
}

