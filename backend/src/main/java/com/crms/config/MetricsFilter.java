package com.crms.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class MetricsFilter extends OncePerRequestFilter {

    private final MeterRegistry meterRegistry;

    public MetricsFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String endpoint = normalizeEndpoint(uri);

        if (isExcludedEndpoint(endpoint)) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.nanoTime() - startTime;
            int status = response.getStatus();

            Timer.builder("http.server.requests")
                    .tag("method", method)
                    .tag("uri", endpoint)
                    .tag("status", String.valueOf(status))
                    .register(meterRegistry)
                    .record(duration, TimeUnit.NANOSECONDS);

            meterRegistry.counter("http.server.requests.total",
                    "method", method,
                    "uri", endpoint,
                    "status", String.valueOf(status),
                    "outcome", getOutcome(status))
                    .increment();
        }
    }

    private String normalizeEndpoint(String uri) {
        if (uri.startsWith("/api/")) {
            String[] segments = uri.substring(5).split("/");
            StringBuilder normalized = new StringBuilder("/api/");

            for (int i = 0; i < Math.min(segments.length, 3); i++) {
                String segment = segments[i];
                if (segment.matches("\\d+") || segment.matches("[a-f0-9-]{36}")) {
                    normalized.append("{id}");
                } else {
                    normalized.append(segment);
                }
                if (i < Math.min(segments.length, 3) - 1) {
                    normalized.append("/");
                }
            }
            return normalized.toString();
        }
        return uri;
    }

    private boolean isExcludedEndpoint(String uri) {
        return uri.startsWith("/actuator") ||
               uri.startsWith("/swagger") ||
               uri.startsWith("/api-docs") ||
               uri.equals("/favicon.ico");
    }

    private String getOutcome(int status) {
        if (status >= 200 && status < 300) return "SUCCESS";
        if (status >= 300 && status < 400) return "REDIRECTION";
        if (status >= 400 && status < 500) return "CLIENT_ERROR";
        if (status >= 500) return "SERVER_ERROR";
        return "UNKNOWN";
    }
}
