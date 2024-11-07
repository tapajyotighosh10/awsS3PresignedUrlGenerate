package com.example.s3.model;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public class FileRequest {

    private String filename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
    public static FileRequest from(APIGatewayProxyRequestEvent input) {
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFilename(input.getQueryStringParameters().get("filename"));
        return fileRequest;
    }
}

