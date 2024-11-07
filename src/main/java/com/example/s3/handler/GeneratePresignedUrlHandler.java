package com.example.s3.handler;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.s3.model.FileRequest;

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

            FileRequest fileRequest = FileRequest.from(input);


            String presignedUrl = GeneratePresignedUrl.generatePresignedUrl(BUCKET_NAME, fileRequest.getFilename());


            String output = String.format("{ \"upload_url\": \"%s\" }", presignedUrl);

            return response
                    .withStatusCode(200)
                    .withBody(output);
        } catch (NullPointerException e) {
            return response
                    .withBody("{}")
                    .withStatusCode(500);
        }
    }
}

