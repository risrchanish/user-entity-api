package com.syncvault.file_service.config;

import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.function.Predicate;

@Configuration
public class ResilienceConfig {

    private static final Predicate<Throwable> IS_SAFE_TO_RETRY = ResilienceConfig::isSafeToRetry;
    @Bean
    public RetryConfigCustomizer userStorageRetryCustomizer(){
        return RetryConfigCustomizer.of("userStorageServiceRetry",
                builder -> builder.retryOnException(IS_SAFE_TO_RETRY));
    }

    private static boolean isSafeToRetry(Throwable throwable){
        Throwable cause = throwable;
        while(cause != null){
            if(cause instanceof HttpConnectTimeoutException){
                return true;
            }
            if(cause instanceof ConnectException){
                return true;
            }
            if(cause instanceof HttpTimeoutException || cause instanceof SocketTimeoutException){
                return false;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
