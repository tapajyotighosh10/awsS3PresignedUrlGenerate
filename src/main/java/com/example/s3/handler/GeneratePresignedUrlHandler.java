package com.example.s3.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.s3.model.FileRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.HashMap;
import java.util.Map;

public class GeneratePresignedUrlHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String BUCKET_NAME = System.getenv("BUCKET_NAME");

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        try {

            if (input == null) {
                return response
                        .withStatusCode(400)
                        .withBody("{\"error\": \"Request is missing\"}");
            }

            if (BUCKET_NAME == null || BUCKET_NAME.isEmpty()) {
                return response
                        .withStatusCode(500)
                        .withBody("{\"error\": \"Bucket name not set on environment variable\"}");
            }

            if (input.getQueryStringParameters() == null) {
                return response
                        .withStatusCode(400)
                        .withBody("{\"error\": \"Request is missing\"}");
            }

            FileRequest fileRequest = FileRequest.from(input);
            String filename = fileRequest.getFilename();

            if (filename == null || filename.trim().isEmpty()) {
                return response
                        .withStatusCode(400)
                        .withBody("{\"error\": \"Invalid input: filename required\"}");
            }

            String presignedUrl = GeneratePresignedUrl.generatePresignedUrl(BUCKET_NAME, filename);
            String output = String.format("{\"upload_url\": \"%s\"}", presignedUrl);

            return response
                    .withStatusCode(200)
                    .withBody(output);

        } catch (S3Exception e) {
            if (e.statusCode() == 403) {
                return response
                        .withStatusCode(403)
                        .withBody("{\"error\": \"Set lambda IAM role permissions to generate presigned URL\"}");
            }
            return response
                    .withStatusCode(500)
                    .withBody("{\"error\": \"Failed to generate presigned URL due to S3 error.\"}");
        } catch (Exception e) {
            return response
                    .withStatusCode(500)
                    .withBody("{\"error\": \"Failed to generate presigned URL\"}");
        }
    }

}

