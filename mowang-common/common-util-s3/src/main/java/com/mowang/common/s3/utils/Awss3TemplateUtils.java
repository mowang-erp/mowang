package com.mowang.common.s3.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;


@Component
public class Awss3TemplateUtils {

    private static Logger logger = LoggerFactory.getLogger(Awss3TemplateUtils.class);


    private static S3Client s3Client;

    @Autowired
    public void setAmazonS3(S3Client s3Client) {
        Awss3TemplateUtils.s3Client = s3Client;
    }

    private static String bucketName;
    private static String privateKeyPath;
    private static String domainName;
    private static String keyPairId;

    @Value("${app.awss3.bucketName}")
    public void setBucketName(String bucketName) {
        Awss3TemplateUtils.bucketName = bucketName;
    }

    @Value("${app.awss3.privateKeyPath}")
    public void setPrivateKeyPath(String privateKeyPath) {
        Awss3TemplateUtils.privateKeyPath = privateKeyPath;
    }
    @Value("${app.awss3.domainName}")
    public void setDomainName(String domainName) {
        Awss3TemplateUtils.domainName = domainName;
    }
    @Value("${app.awss3.keyPairId}")
    public void setKeyPairId(String keyPairId) {
        Awss3TemplateUtils.keyPairId = keyPairId;
    }


    public static String putObject(String fileId, File file) {
        logger.debug("fileID={{}}", fileId);
        try {

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileId)
                    .build();
            PutObjectResponse response = s3Client.putObject(request, RequestBody.fromFile(file));
            logger.debug("response={{}}", response);
            return response.toString();
        } catch (Exception e) {
            logger.error("Error Message: " + e.getMessage());
        }
        return null;
    }


    public static Boolean deleteObject(String fileId) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileId)
                    .build();
            s3Client.deleteObject(request);
            return true;
        } catch (Exception e) {
            logger.error("Error Message: " + e.getMessage());
        }
        return false;
    }



    public static String getUrl(String fileId) throws InvalidKeySpecException, IOException {
        // 添加 BouncyCastle 提供者
        Security.addProvider(new BouncyCastleProvider());
        int expiresInSeconds = 3600; // 过期时间，单位为秒
        // 计算过期时间
        Instant expirationTime = Instant.now().plus(expiresInSeconds, ChronoUnit.SECONDS);

        // 生成 Signed URL
        CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
        CannedSignerRequest signerRequest = null;
        try {
            signerRequest = CannedSignerRequest.builder()
                    .resourceUrl(String.format("https://%s/%s", domainName, fileId))
                    .privateKey(Paths.get(privateKeyPath))
                    .keyPairId(keyPairId)
                    .expirationDate(expirationTime)
                    .build();
            SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(signerRequest);
            return signedUrl.url();
        } catch (Exception e) {
            logger.error("Error Message: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public static boolean checkBucketExists() {
        ListBucketsResponse response = s3Client.listBuckets();
        for (Bucket bucket : response.buckets()) {
            if (Objects.equals(bucket.name(), bucketName)) {
                return true;
            }
        }
        return false;
    }
}
