package com.github.p3.service;

import com.github.p3.dto.ProductRegisterDto;
import com.github.p3.dto.ProductResponseDto;
import com.github.p3.entity.Image;
import com.github.p3.entity.Product;
import com.github.p3.entity.User;
import com.github.p3.mapper.ProductMapper;
import com.github.p3.repository.ImageRepository;
import com.github.p3.repository.ProductRepository;
import com.github.p3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    public void registerProduct(ProductRegisterDto productRegisterDto, List<String> imageUrls) {
        // ProductRegisterDto -> Product 변환
        Product product = productMapper.toEntity(productRegisterDto, userRepository);

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

}

