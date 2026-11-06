package com.spring.eventhub.config.webmvc;


import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.spring.eventhub.common.interceptor.TimeoutHandler;

import jakarta.annotation.PostConstruct;
@Configuration
public class HandlerConfig{
    
    RequestMappingHandlerAdapter requestMappingHandler;
    TimeoutHandler timeoutHandler;
    HandlerConfig(RequestMappingHandlerAdapter requestMappingHandler, TimeoutHandler timeoutHandler){
        this.requestMappingHandler = requestMappingHandler;
        this.timeoutHandler = timeoutHandler;
    }
  
    @PostConstruct 
    public void prioritizeCustomReturnValueHandler() {
        List<HandlerMethodReturnValueHandler> originalHandlers = requestMappingHandler.getReturnValueHandlers();

         List<HandlerMethodReturnValueHandler> newHandlers = new ArrayList<>();
         newHandlers.add(timeoutHandler);

          if (originalHandlers != null) {
            newHandlers.addAll(originalHandlers);
        
        }
        requestMappingHandler.setReturnValueHandlers(newHandlers);
    }
}
