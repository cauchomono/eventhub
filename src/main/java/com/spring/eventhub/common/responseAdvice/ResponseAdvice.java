package com.spring.eventhub.common.responseAdvice;

import java.time.Instant;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.spring.eventhub.common.Constants;
import com.spring.eventhub.common.GlobalExceptionHandler;
import com.spring.eventhub.common.decorators.NoEnvelopeBody;

@RestControllerAdvice 
public class ResponseAdvice implements ResponseBodyAdvice<Object>{

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object arg0, MethodParameter arg1, MediaType arg2,
            Class<? extends HttpMessageConverter<?>> arg3, ServerHttpRequest arg4, ServerHttpResponse arg5) {
                
        
                String requestId =arg4.getAttributes().get(Constants.REQUEST_ID_NAME).toString();
                MetaEnvelope meta = new MetaEnvelope(requestId, Instant.now());
             

               return new ResponseEnveloped(arg0, meta);

    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
                Class<?> returnTypeClass = returnType.getDeclaringClass();
                if(returnTypeClass ==GlobalExceptionHandler.class){

                return false;
               }
    

        
          boolean hasNoEnvelopeMethod = returnType.hasMethodAnnotation(NoEnvelopeBody.class);

          boolean hasNoEnvelopeClass = AnnotationUtils.findAnnotation(returnType.getDeclaringClass(), NoEnvelopeBody.class) != null;
          

                return !(hasNoEnvelopeClass || hasNoEnvelopeMethod);
        }

}
