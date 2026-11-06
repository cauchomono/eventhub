package com.spring.eventhub.common.filter.logger;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(-101)
public class LoggerFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(LoggerFilter.class);

    private static final List<String> EXCLUDED_URLS = List.of(
            "/health");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        return EXCLUDED_URLS.stream().anyMatch(path::contentEquals);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String params = truncateLongStrings(request.getQueryString());
        String url = request.getRequestURI();

        if (params != null) {
            url = request.getRequestURI() + "?" + params;
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            filterChain.doFilter(request, response);
        } finally {
            int status = response.getStatus();
            stopWatch.stop();
            long duration = stopWatch.getTotalTimeMillis();
            log.info("{} {} - {} - {} ms", method, url, status, duration);
        }

    }

    private String truncateLongStrings(String text) {
        String newText = text;
        
        if (text != null && text.length() > 512) {
            newText = text.substring(0, 512) + "[truncated]";
        }
        return newText;

    }

}
