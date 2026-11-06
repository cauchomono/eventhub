package com.spring.eventhub.common.filter.timeResponse;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.security.web.util.OnCommittedResponseWrapper;
import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(-102)
public class TimeResponseFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startTime = System.nanoTime();

        TimeResponseWrapper timeResponseWrapper = new TimeResponseWrapper(response, startTime);
        try {

            filterChain.doFilter(request, timeResponseWrapper);
        } finally {
            timeResponseWrapper.setResponseTime();
        }

    }

    private static class TimeResponseWrapper extends OnCommittedResponseWrapper {

        private final long startTime;
        private boolean wasModified;

        public TimeResponseWrapper(HttpServletResponse response, long startTime) {
            super(response);
            this.startTime = startTime;

        }

        @Override
        protected void onResponseCommitted() {
            setResponseTime();
        }

        public void setResponseTime() {

            if (wasModified) {
                return;
            }
            Long duration = (System.nanoTime() - startTime) / 1000000;
            wasModified = true;
            setHeader("x-response-time", duration.toString());
        }

    }

}
