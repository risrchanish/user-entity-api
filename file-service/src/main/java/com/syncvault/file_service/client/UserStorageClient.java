package com.syncvault.file_service.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class UserStorageClient {

    public static final Logger log = LoggerFactory.getLogger(UserStorageClient.class);

    private final RestClient restClient;
    private final String internalApiSecret;

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public UserStorageClient(
            @Value("${services.user-service.base-url}") String baseUrl,
            @Value("${internal.api.secret}") String internalApiSecret,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry){

        HttpClientSettings timeoutSettings = HttpClientSettings.defaults()
                .withConnectTimeout(Duration.ofSeconds(2))
                .withReadTimeout(Duration.ofSeconds(3));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.jdk()
                .build(timeoutSettings);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.internalApiSecret = internalApiSecret;

        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("userStorageService");
        this.retry = retryRegistry.retry("userStorageServiceRetry");
    }

    public void addStorage(UUID userId, long bytesToAdd){

        Supplier<Void> httpCall = () -> {
            restClient.patch()
                    .uri("/users/{userId}/storage", userId)
                    .header("X-Internal-Api-Key", internalApiSecret)
                    .body(new AddStorageRequest(bytesToAdd))
                    .retrieve()
                    .toBodilessEntity();
            return null;
        };

        Supplier<Void> resilientCall = CircuitBreaker.decorateSupplier(
                circuitBreaker,
                Retry.decorateSupplier(retry, httpCall)
        );

        try{
            resilientCall.get();
        }catch(Exception ex){
            log.error("Failed to update storage usage for user {}: {}", userId, ex.getMessage());
        }
    }

    private record AddStorageRequest(long bytesToAdd){}
}