package com.example.s3.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeneratePresignedUrlHandlerTest {

    private GeneratePresignedUrlHandler handler;
    private Context mockContext;

    @BeforeAll
    static void setupEnvironment() {
        System.setProperty("BUCKET_NAME", "test-bucket");
    }
    @BeforeEach
    void setUp() {
        handler = new GeneratePresignedUrlHandler();
        mockContext = mock(Context.class);
    }

    @Test
    void testBucketNameNotSet() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();

        APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, mockContext);

        assertEquals(500, response.getStatusCode());
        assertEquals("{\"error\": \"Bucket name not set on environment variable\"}", response.getBody());
    }

    @Test
    void testNullInput() {
        APIGatewayProxyResponseEvent response = handler.handleRequest(null, mockContext);

        assertEquals(400, response.getStatusCode());
        assertEquals("{\"error\": \"Request is missing\"}", response.getBody());
    }
    @Test
    void testMissingQueryParameters() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setQueryStringParameters(null);
        APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, mockContext);

        assertEquals(400, response.getStatusCode());
        assertEquals("{\"error\": \"Request is missing\"}", response.getBody());
    }
    @Test
    void testInvalidFilename() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setQueryStringParameters(Map.of("filename", " ")); // Empty or whitespace filename

        APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, mockContext);

        assertEquals(400, response.getStatusCode());
        assertEquals("{\"error\": \"Invalid input: filename required\"}", response.getBody());
    }
    @Test
    void testValidRequest() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setQueryStringParameters(Map.of("filename", "test-file.txt"));

        String mockUrl = "https://example.com/test-file.txt";
        GeneratePresignedUrl mockGeneratePresignedUrl = mock(GeneratePresignedUrl.class);
        when(mockGeneratePresignedUrl.generatePresignedUrl("test-bucket", "test-file.txt")).thenReturn(mockUrl);

        APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, mockContext);

        assertEquals(200, response.getStatusCode());
        assertEquals(String.format("{\"upload_url\": \"%s\"}", mockUrl), response.getBody());
    }
    @Test
    void testS3ExceptionAccessDenied() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setQueryStringParameters(Map.of("filename", "test-file.txt"));

        GeneratePresignedUrl mockGeneratePresignedUrl = mock(GeneratePresignedUrl.class);
        when(mockGeneratePresignedUrl.generatePresignedUrl("test-bucket", "test-file.txt"))
                .thenThrow(S3Exception.builder().statusCode(403).build());

        APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, mockContext);

        assertEquals(403, response.getStatusCode());
        assertEquals("{\"error\": \"Set lambda IAM role permissions to generate presigned URL\"}", response.getBody());
    }

    @Test
    void testS3ExceptionGenericError() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setQueryStringParameters(Map.of("filename", "test-file.txt"));

        GeneratePresignedUrl mockGeneratePresignedUrl = mock(GeneratePresignedUrl.class);
        when(mockGeneratePresignedUrl.generatePresignedUrl("test-bucket", "test-file.txt"))
                .thenThrow(S3Exception.builder().statusCode(500).build());

        APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, mockContext);

        assertEquals(500, response.getStatusCode());
        assertEquals("{\"error\": \"Failed to generate presigned URL due to S3 error.\"}", response.getBody());
    }

    @Test
    void testUnhandledException() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setQueryStringParameters(Map.of("filename", "test-file.txt"));

        GeneratePresignedUrl mockGeneratePresignedUrl = mock(GeneratePresignedUrl.class);
        when(mockGeneratePresignedUrl.generatePresignedUrl("test-bucket", "test-file.txt"))
                .thenThrow(new RuntimeException("Unexpected error"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, mockContext);

        assertEquals(500, response.getStatusCode());
        assertEquals("{\"error\": \"Failed to generate presigned URL\"}", response.getBody());
    }
}

