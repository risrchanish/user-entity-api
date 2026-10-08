package com.syncvault.api_gateway.config;

import com.syncvault.api_gateway.filter.RateLimitingFilter;
import com.syncvault.api_gateway.security.JwtValidationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtValidationFilter> jwtValidationFilterRegistration(
            JwtValidationFilter jwtValidationFilter){
        FilterRegistrationBean<JwtValidationFilter> registration =
                new FilterRegistrationBean<>(jwtValidationFilter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration(
            RateLimitingFilter rateLimitingFilter) {
        FilterRegistrationBean<RateLimitingFilter> registration =
                new FilterRegistrationBean<>(rateLimitingFilter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }

}
