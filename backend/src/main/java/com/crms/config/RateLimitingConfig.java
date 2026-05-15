package com.crms.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class RateLimitingConfig {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final int AUTH_MAX_REQUESTS_PER_MINUTE = 10;

    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    private class RateLimitFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            String clientId = getClientIdentifier(request);
            String path = request.getRequestURI();
            int maxRequests = isAuthEndpoint(path) ? AUTH_MAX_REQUESTS_PER_MINUTE : MAX_REQUESTS_PER_MINUTE;

            RateLimitBucket bucket = buckets.computeIfAbsent(clientId, k -> new RateLimitBucket());

            synchronized (bucket) {
                if (bucket.isExpired()) {
                    bucket.reset();
                }

                if (bucket.getCount() >= maxRequests) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"error\":\"Too many requests. Please try again later.\",\"retryAfter\":60}"
                    );
                    response.setHeader("Retry-After", "60");
                    response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
                    response.setHeader("X-RateLimit-Remaining", "0");
                    return;
                }

                bucket.increment();
            }

            response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(maxRequests - buckets.get(clientId).getCount()));

            filterChain.doFilter(request, response);
        }

        private String getClientIdentifier(HttpServletRequest request) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isEmpty()) {
                return forwardedFor.split(",")[0].trim();
            }
            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isEmpty()) {
                return realIp;
            }
            return request.getRemoteAddr();
        }

        private boolean isAuthEndpoint(String path) {
            return path.contains("/auth/login") || path.contains("/auth/refresh");
        }
    }

    private static class RateLimitBucket {
        private final AtomicInteger count = new AtomicInteger(0);
        private Instant windowStart = Instant.now();

        public boolean isExpired() {
            return Duration.between(windowStart, Instant.now()).toMinutes() >= 1;
        }

        public void reset() {
            count.set(0);
            windowStart = Instant.now();
        }

        public int getCount() {
            return count.get();
        }

        public void increment() {
            count.incrementAndGet();
        }
    }
}
