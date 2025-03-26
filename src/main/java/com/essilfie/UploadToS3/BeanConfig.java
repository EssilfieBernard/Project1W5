package com.essilfie.UploadToS3;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

import static software.amazon.awssdk.regions.Region.US_EAST_1;

@Configuration
public class BeanConfig {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(US_EAST_1)
                .build();
    }
}
