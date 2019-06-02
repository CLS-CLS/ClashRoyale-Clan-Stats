package org.lytsiware.clash.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Slf4j
public class ContentLengthHttpInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.info("---- REQUEST ----");
        log.info("URL: {}", request.getURI());
        log.info("HEADERS: {} ", request.getHeaders());
        log.info("BODY : {} ", body);

        ClientHttpResponse response = execution.execute(request, body);
        log.info("---- RESPONSE ----");
        log.info("HEADERS: {}", response.getHeaders());
        log.info("content length: {}", response.getHeaders().getContentLength());

        return response;
    }

}
