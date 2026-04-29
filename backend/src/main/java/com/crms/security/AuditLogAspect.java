package com.crms.security;

import com.crms.domain.user.entity.AuditLog;
import com.crms.domain.user.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;

    private static final AtomicReference<String> lastHash = new AtomicReference<>(null);

    @Pointcut("@target(org.springframework.web.bind.annotation.RestController)")
    public void controllerMethods() {}

    @Pointcut("execution(* *(..)) && @annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMappingMethods() {}

    @Pointcut("execution(* *(..)) && @annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMappingMethods() {}

    @Pointcut("execution(* *(..)) && @annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void patchMappingMethods() {}

    @Pointcut("execution(* *(..)) && @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void deleteMappingMethods() {}

    @Before("controllerMethods() && (postMappingMethods() || putMappingMethods() || patchMappingMethods() || deleteMappingMethods())")
    public void auditBefore(JoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        String method = joinPoint.getSignature().toShortString();
        String args = getArgsSummary(joinPoint.getArgs());
        
        log.info("AUDIT: User '{}' executing '{}' with args: {}", username, method, args);
    }

    @AfterReturning(pointcut = "controllerMethods() && (postMappingMethods() || putMappingMethods() || patchMappingMethods() || deleteMappingMethods())", 
                    returning = "result")
    public void auditAfterReturning(JoinPoint joinPoint, Object result) {
        saveAuditLog(joinPoint, "SUCCESS", null);
    }

    @AfterThrowing(pointcut = "controllerMethods() && (postMappingMethods() || putMappingMethods() || patchMappingMethods() || deleteMappingMethods())", 
                   throwing = "exception")
    public void auditAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        saveAuditLog(joinPoint, "ERROR", exception.getMessage());
    }

    private void saveAuditLog(JoinPoint joinPoint, String outcome, String errorMessage) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "anonymous";
            String method = joinPoint.getSignature().toShortString();
            String action = getActionFromMethod(method);
            
            String entityType = extractEntityType(joinPoint.getTargetClass().getSimpleName());
            String entityId = extractEntityId(joinPoint.getArgs());
            
            HttpServletRequest request = getCurrentRequest();
            String ipAddress = request != null ? getClientIp(request) : null;
            String userAgent = request != null ? request.getHeader("User-Agent") : null;
            
            // Get previous hash from the last saved audit log
            String previousHash = getLastHash();
            
            AuditLog auditLog = AuditLog.builder()
                    .userName(username)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .previousHash(previousHash)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // Compute the hash chain
            auditLog.computeHash();
            
            // Persist and update last hash
            auditLogRepository.save(auditLog);
            lastHash.set(auditLog.getSha256());
            
            log.info("AUDIT LOG SAVED: {} {} {} -> hash: {}", action, entityType, entityId, auditLog.getSha256());
            
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }

    private String getActionFromMethod(String methodSignature) {
        if (methodSignature.contains("create") || methodSignature.contains("save") || methodSignature.contains("add")) {
            return "POST";
        } else if (methodSignature.contains("update") || methodSignature.contains("edit")) {
            return "PUT";
        } else if (methodSignature.contains("patch")) {
            return "PATCH";
        } else if (methodSignature.contains("delete") || methodSignature.contains("remove")) {
            return "DELETE";
        }
        return "UNKNOWN";
    }

    private String extractEntityType(String className) {
        // Remove 'Controller' suffix if present
        if (className.endsWith("Controller")) {
            return className.substring(0, className.length() - 10);
        }
        return className;
    }

    private String extractEntityId(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof Long || arg instanceof Integer || arg instanceof String) {
                return arg.toString();
            } else if (arg instanceof java.util.UUID) {
                return arg.toString();
            }
        }
        return null;
    }

    private synchronized String getLastHash() {
        // Fetch from DB for thread safety and consistency
        List<AuditLog> logs = auditLogRepository.findTop1ByOrderByTimestampDesc();
        if (!logs.isEmpty()) {
            return logs.get(0).getSha256();
        }
        return null;
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getArgsSummary(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            Object arg = args[i];
            if (arg == null) {
                sb.append("null");
            } else if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
                sb.append(arg.toString());
            } else if (arg.getClass().isArray()) {
                sb.append(arg.getClass().getComponentType().getSimpleName()).append("[]");
            } else {
                sb.append(arg.getClass().getSimpleName());
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
