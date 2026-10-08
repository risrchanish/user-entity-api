package com.syncvault.api_gateway.filter;

import com.syncvault.api_gateway.ratelimit.FixedWindowRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final FixedWindowRateLimiter rateLimiter;
    private final int authenticatedLimit;
    private final int unauthenticatedLimit;

    public RateLimitingFilter(
            FixedWindowRateLimiter rateLimiter,
            @Value("${application.ratelimit.authenticated-limit:100}")
            int authenticatedLimit,
            @Value("${application.ratelimit.unauthenticated-limit:10}")
            int unauthenticatedLimit){

        this.rateLimiter = rateLimiter;
        this.authenticatedLimit = authenticatedLimit;
        this.unauthenticatedLimit = unauthenticatedLimit;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {


        String userId = request.getHeader("X-User-Id");

        String key;
        int limit;
        if(userId != null && !userId.isBlank()){
            key = "user: "+userId;
            limit = authenticatedLimit;
        }else{
            key = "ip: "+getClientIp(request);
            limit = unauthenticatedLimit;
        }
        if(rateLimiter.tryAcquire(key,limit)){
            filterChain.doFilter(request,response);
            return;
        }
        log.debug("Rate limit exceeded for key [{}] (limit [{}]) ", key, limit);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Rate limit exceeded. Try again later.\"}");
    }

    @Value("${gateway.trust-forwarded-headers:false}")
    private boolean trustForwardedHeaders;

    String getClientIp(HttpServletRequest request) {

        if (trustForwardedHeaders) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return forwardedFor.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
