package org.example.educheck.global.common.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.global.common.exception.ErrorCode;
import org.example.educheck.global.common.exception.custom.common.ServerErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

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

    public String generateUploadPresignedUrl(String originalFileName, String fileExtension) {

        String fileName = UUID.randomUUID() + "-" + originalFileName;
        String key = FILE_PATH_PREFIX + fileName; // /attendance-absences/abc1234.png 처럼 S3내 고유한 파일 경로 생성

        String contentType = getContentType(fileExtension);

        // S3에 업로드할 파일 요청 정보
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        // PresignedURL 발급 요청
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(request ->
                request.putObjectRequest(putObjectRequest)
                        .signatureDuration(Duration.ofMinutes(10))
        );

        return presignedRequest.url().toString();
    }

    private String getContentType(String fileExtension) {
        switch (fileExtension.toLowerCase()) {
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default:
                return "application/octet-stream"; // 기본 바이너리
        }
    }

    public String generateViewPresignedUrl(String key) {
        //조회 요청 정보 생성
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        // Presigned GET URL 발급
        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);

        return presignedGetObjectRequest.url().toString();
    }

    //TODO: Delete 메서드 구현 및 사용
    /**
     * S3 URL에서 Key 추출
     * ex. https://bucket.s3.amazonaws.com/attendance-absences/abc1234.png -> attendance-absences/abc1234.png
     * @param s3Key
     */
    private String extractKeyFromUrl(String url) {
        int index = url.indexOf(".amazonaws.com/") + ".amazonaws.com/".length();
        return url.substring(index);
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


