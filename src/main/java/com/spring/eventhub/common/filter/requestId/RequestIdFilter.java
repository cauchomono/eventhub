package com.spring.eventhub.common.filter.requestId;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.spring.eventhub.common.Constants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestIdFilter.class);

    private static final Pattern UUID_PATTERN = Pattern
            .compile("^([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}|[0-9a-fA-F]{64})$");
    private static final String MDC_KEY = "requestId";
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
       try{
        String requestId;
        List<String> requestIds = Collections.list(request.getHeaders("x-request-id"));
        boolean isValid = isValidRequestId(requestIds);
        if(  !isValid){
            requestId = UUID.randomUUID().toString();

        }else{
            requestId = requestIds.getFirst();
        }
        response.addHeader("X-Request-Id", requestId);
        MDC.put(MDC_KEY, requestId);
        request.setAttribute(Constants.REQUEST_ID_NAME, requestId);
        if(!requestIds.isEmpty() && !isValid){
             String message = messageForInvalidRequestId(requestIds);
            log.warn(message);
        }
       
        filterChain.doFilter(request, response);
       } finally{
        MDC.remove(MDC_KEY);
       }
        
    }

    private boolean isValidRequestId(List<String> requestIds){
    
       return requestIds.size() == 1 && (UUID_PATTERN.matcher(requestIds.getFirst()).matches());
    }

    private String messageForInvalidRequestId(List<String> requestId ){
        String newRequestId;
        String defaultMessage = "Invalid Request id: ";

        if(requestId.size() > 1){
            return defaultMessage + "more than one request id";

        }
        else if(requestId.get(0).length() >=64){
        newRequestId   = requestId.get(0).substring(0,64);
        }else{
            newRequestId = requestId.get(0);
        }
        newRequestId = Encode.forJava(newRequestId);
        return defaultMessage + newRequestId;
    }

}
