package com.github.p3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // 파일 업로드 메서드
    public List<String> uploadFiles(List<MultipartFile> files) {
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            // S3에 파일을 업로드하고 URL을 반환하는 로직
            String imageUrl = uploadFileToS3(file);
            imageUrls.add(imageUrl);
        }
        return imageUrls;
    }

    // 파일을 S3에 업로드하고 URL을 반환하는 메서드
    private String uploadFileToS3(MultipartFile file) {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename(); // 파일명에 타임스탬프 추가
        try {
            // S3에 파일 업로드
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), new ObjectMetadata()));
            // 업로드한 파일의 URL 생성
            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            log.error("Error uploading file to S3", e);
            throw new RuntimeException("Error uploading file to S3", e);
        }
    }
}
