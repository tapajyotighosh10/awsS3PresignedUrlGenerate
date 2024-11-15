package com.example.s3.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class GeneratePresignedUrlHandlerTest {
    private GeneratePresignedUrlHandler handler;
    @Mock
    private Context mockContext;

    @Before
    public void setUp() {
        handler = new GeneratePresignedUrlHandler();
        mockContext = mock(Context.class);
    }

    @Test
    public void testHandleRequest_MissingBucketNameEnvVar() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();

        APIGatewayProxyResponseEvent response = handler.handleRequest(requestEvent, mockContext);

        assertEquals(500, response.getStatusCode().intValue());
        assertTrue(response.getBody().contains("Bucket name not set on environment variable"));
    }

}
