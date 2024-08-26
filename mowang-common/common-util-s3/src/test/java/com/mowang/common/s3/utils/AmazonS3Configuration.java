package com.mowang.common.s3.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;


@Configuration
public class AmazonS3Configuration {
    @Value("${app.awss3.bucketName}")
    private String bucketName;
    @Value("${app.awss3.accessKey}")
    private String accessKey;
    @Value("${app.awss3.secretKey}")
    private String secretKey;
    @Value("${app.awss3.region}")
    private String regionsName;

    @Bean
    public S3Client initAmazonS3() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
                .region(Region.of(regionsName))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }


}
