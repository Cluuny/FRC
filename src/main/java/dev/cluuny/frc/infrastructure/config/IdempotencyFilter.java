package dev.cluuny.frc.infrastructure.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyFilter implements Filter {

    // Simple in-memory store for idempotency keys. In production, use Redis.
    private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String idempotencyKey = req.getHeader("Idempotency-Key");

        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            if (cache.containsKey(idempotencyKey)) {
                CachedResponse cached = cache.get(idempotencyKey);
                res.setStatus(cached.status);
                res.setContentType(cached.contentType);
                res.getOutputStream().write(cached.body);
                return;
            }

            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(res);
            chain.doFilter(request, responseWrapper);

            if (responseWrapper.getStatus() >= 200 && responseWrapper.getStatus() < 300) {
                byte[] body = responseWrapper.getContentAsByteArray();
                cache.put(idempotencyKey, new CachedResponse(responseWrapper.getStatus(), responseWrapper.getContentType(), body));
            }
            responseWrapper.copyBodyToResponse();
        } else {
            chain.doFilter(request, response);
        }
    }

    private static class CachedResponse {
        int status;
        String contentType;
        byte[] body;

        public CachedResponse(int status, String contentType, byte[] body) {
            this.status = status;
            this.contentType = contentType;
            this.body = body;
        }
    }
}
