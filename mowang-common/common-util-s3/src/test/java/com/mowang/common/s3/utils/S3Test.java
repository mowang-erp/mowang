package com.mowang.common.s3.utils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.Security;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class S3Test {
    private static final String ACCESS_KEY = "IAM ACCESS KEY";
    private static final String SECRET_KEY = "IAM SECRET_KEY";
    private static final String BUCKET_NAME = "file.example.com";
    private static final String REGION = "us-west-2";
    private static final String FILE_KEY_NAME = "test.jpg";
    private static final String FILE_PATH = "src/test/resources/test.jpg";
    private static final String DOMAIN_NAME = "file.example.com";
    String KEY_PAIR_ID = "KAxxxxxxx";
    String PRIVATE_KEY_PATH = "src/test/resources/private_key.pem";

    @Test
    @DisplayName("上传图片")
    public void S3Uploader() throws IOException {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);
        S3Client s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(REGION))
                .build();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(FILE_KEY_NAME)
                .build();
        s3Client.putObject(request, Paths.get(FILE_PATH));
        System.out.println("File uploaded successfully.");
    }

    @Test
    @DisplayName("删除图片")
    public void S3Deleter() throws IOException {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);
        S3Client s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(REGION))
                .build();

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(FILE_KEY_NAME)
                .build();
        s3Client.deleteObject(request);

        System.out.println("File deleted successfully.");
    }

    @Test
    @DisplayName("获取图片URL加签")
    public void CloudFrontSignedUrlGenerator() throws IOException {
        try {
            // 添加 BouncyCastle 提供者
            Security.addProvider(new BouncyCastleProvider());
            int expiresInSeconds = 3600; // 过期时间，单位为秒
            // 计算过期时间
            Instant expirationTime = Instant.now().plus(expiresInSeconds, ChronoUnit.SECONDS);

            // 生成 Signed URL
            CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
            CannedSignerRequest signerRequest = CannedSignerRequest.builder()
                    .resourceUrl(String.format("https://%s/%s", DOMAIN_NAME, FILE_KEY_NAME))
                    .privateKey(Paths.get(PRIVATE_KEY_PATH))
                    .keyPairId(KEY_PAIR_ID)
                    .expirationDate(expirationTime)
                    .build();
            SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(signerRequest);
            System.out.println("Signed URL: " + signedUrl.url());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("图片列表")
    public void S3Lister() throws IOException {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);
        S3Client s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(REGION))
                .build();

        ListObjectsV2Request req = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .build();
        ListObjectsV2Response result;

        do {
            result = s3Client.listObjectsV2(req);

            for (S3Object s3Object : result.contents()) {
                System.out.println(" - " + s3Object.key() + "  " +
                        "(size = " + s3Object.size() + ")");
            }
            req = req.toBuilder().continuationToken(result.nextContinuationToken()).build();
        } while (result.isTruncated());
    }
}
