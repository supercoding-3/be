package com.github.p3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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

    private String uploadFileToS3(MultipartFile file) {
        // 고유한 파일 이름 생성
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            // ObjectMetadata 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize()); // 파일 크기 설정
            metadata.setContentType(file.getContentType()); // 파일 타입 설정 (선택 사항)

            // S3에 파일 업로드
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));

            // 업로드한 파일의 URL 반환
            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            log.error("Error uploading file to S3", e);
            throw new RuntimeException("Error uploading file to S3", e);
        }
    }

    public void deleteFileFromS3(String fileUrl) {
        try {
            // URL에서 파일 이름만 추출
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            // URL 디코딩하여 파일 이름 처리 (특수 문자 및 공백 처리)
            fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

            // S3에서 해당 파일 삭제
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileName));
            log.info("File deleted from S3: {}", fileName);
        } catch (Exception e) {
            log.error("Error deleting file from S3", e);
            throw new RuntimeException("Error deleting file from S3", e);
        }
    }

}
