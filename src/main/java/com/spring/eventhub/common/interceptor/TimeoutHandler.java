package com.spring.eventhub.common.interceptor;

import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;


import com.spring.eventhub.common.decorators.RequestTimeout;

@Component
public class TimeoutHandler implements HandlerMethodReturnValueHandler {

    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest) throws Exception {

        if (returnValue == null) {
            return;
        }
           mavContainer.setRequestHandled(true);



            Callable<?> callable = (Callable<?>) returnValue;
            RequestTimeout annotation = returnType.getMethodAnnotation(RequestTimeout.class);
            long time = annotation.value();

            WebAsyncTask<?> webAsyncTask = new WebAsyncTask<>(time,callable);

            WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(webRequest);
            asyncManager.startCallableProcessing(webAsyncTask, mavContainer);

        }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        Class<?> clazz = returnType.getParameterType();
        return returnType.hasMethodAnnotation(RequestTimeout.class) && Callable.class.isAssignableFrom(clazz);
    }
}
