package com.spring.eventhub.common.interceptor;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JsonFormatValidationInterceptor implements HandlerInterceptor {

    private final MediaType serverType = MediaType.APPLICATION_JSON;

    private enum HTTP_METHODS {
        POST, PUT, PATCH
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        boolean hasBody = hasBody(request);
        boolean needToBeValidated = methodNeedToBeValidated(request);
        if (!needToBeValidated || !hasBody) {
            return true;
        }
        try {
            MediaType clientType = MediaType.parseMediaType(request.getContentType());

            if (!serverType.equalsTypeAndSubtype(clientType)) {
                throw new HttpMediaTypeNotSupportedException(clientType.toString(),
                        Collections.singletonList(MediaType.APPLICATION_JSON));
            }
            return true;
        } catch (Exception e) {
            throw new HttpMediaTypeNotSupportedException(request.getContentType(),
                    Collections.singletonList(MediaType.APPLICATION_JSON));
        }

    }

    private boolean methodNeedToBeValidated(HttpServletRequest request) {

        String requestMethod = request.getMethod();

        if (requestMethod == null) {
            return false;
        }
        return Arrays.stream(HTTP_METHODS.values())
        .anyMatch(m -> m.name().equalsIgnoreCase(requestMethod));

    }

    private boolean hasBody(HttpServletRequest request) {

        long contentLenght = request.getContentLengthLong();
        if (contentLenght > 0) {
            return true;
        }
        String transferEncoding = request.getHeader("Transfer-Encoding");
        if (transferEncoding != null && transferEncoding.equalsIgnoreCase("chunked")) {
            return true;
        }
        return false;
    }
}
