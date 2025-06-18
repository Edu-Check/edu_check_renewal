package org.example.educheck.global.common.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.global.common.exception.ErrorCode;
import org.example.educheck.global.common.exception.custom.common.ServerErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private static final String FILE_PATH_PREFIX = "attendance-absences/";
    //버킷명, 리전, 파일경로
    private static final String IMAGE_URL_FORMAT = "https://%s.s3.%s.amazonaws.com/%s";
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    @Value("${BUCKET_NAME}")
    private String bucketName;
    @Value("${REGION}")
    private String region;

    public List<Map<String, String>> generatePresignedUrls(String[] fileNames) {
        List<Map<String, String>> result = new ArrayList<>();

        for (String fileName : fileNames) {
            try {
                String s3Key = FILE_PATH_PREFIX + UUID.randomUUID() + "_" + fileName;
                log.info("s3Key : {}", s3Key);

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .build();

                PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(putObjectRequest)
                        .build();

                String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
                String fileUrl = String.format(IMAGE_URL_FORMAT, bucketName, region, s3Key);

                result.add(Map.of(
                        "fileUrl", fileUrl,
                        "s3Key", s3Key,
                        "presignedUrl", presignedUrl
                ));
            } catch (SdkException e) {
                log.error("Failed to generate presigned url for file : {}", fileName, e);
                throw new RuntimeException("Presigned URL 생성 중 에러 발생", e);
            }
        }

        return result;
    }

    public void deleteFile(String s3Key) {
        log.info("동작");
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ServerErrorException(ErrorCode.FILE_DELETE_ERROR);
        }
    }

}


