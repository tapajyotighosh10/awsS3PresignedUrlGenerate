package com.example.s3.handler;

import com.example.s3.config.S3Config;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

public class GeneratePresignedUrl {

    public static String generatePresignedUrl(String bucketName, String objectKey) {

        S3Presigner presigner = S3Config.createPresigner();
        Duration expiration = Duration.ofHours(1);


        return presigner.presignPutObject(builder ->
                        builder.signatureDuration(expiration)
                                .putObjectRequest(builder1 ->
                                        builder1.bucket(bucketName).key(objectKey)))
                .url()
                .toString();
    }
}
