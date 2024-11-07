package com.example.s3.config;

import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

public class S3Config {

    public static S3Presigner createPresigner() {
        return S3Presigner.builder()
                .serviceConfiguration(S3Configuration.builder()
                        .checksumValidationEnabled(false)
                        .accelerateModeEnabled(true)
                        .build())
                .build();
    }
}

