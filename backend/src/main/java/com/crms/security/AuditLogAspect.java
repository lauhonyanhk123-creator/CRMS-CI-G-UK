package com.crms.security;

import com.crms.domain.user.entity.AuditLog;
import com.crms.domain.user.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    private final ObjectMapper objectMapper;

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
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        saveAuditLog(joinPoint, "SUCCESS", null, result);
                    }
                }
            );
        } else {
            saveAuditLog(joinPoint, "SUCCESS", null, result);
        }
    }

    @AfterThrowing(pointcut = "controllerMethods() && (postMappingMethods() || putMappingMethods() || patchMappingMethods() || deleteMappingMethods())",
                   throwing = "exception")
    public void auditAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        saveAuditLog(joinPoint, "ERROR", exception.getMessage(), null);
                    }
                }
            );
        } else {
            saveAuditLog(joinPoint, "ERROR", exception.getMessage(), null);
        }
    }

    private void saveAuditLog(JoinPoint joinPoint, String outcome, String errorMessage, Object result) {
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

            String previousHash = getLastHash();

            // Capture afterState from the returned entity
            String afterState = serializeState(result);

            AuditLog auditLog = AuditLog.builder()
                    .userName(username)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .previousHash(previousHash)
                    .afterState(afterState)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLog.computeHash();

            auditLogRepository.save(auditLog);
            lastHash.set(auditLog.getSha256());

            log.info("AUDIT LOG SAVED: {} {} {} -> hash: {}", action, entityType, entityId, auditLog.getSha256());

        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }

    private String getActionFromMethod(String methodSignature) {
        String lower = methodSignature.toLowerCase();
        if (lower.contains("post") || lower.contains("create") || lower.contains("save") || lower.contains("add")) {
            return "POST";
        } else if (lower.contains("put") || lower.contains("update") || lower.contains("edit")) {
            return "PUT";
        } else if (lower.contains("patch")) {
            return "PATCH";
        } else if (lower.contains("delete") || lower.contains("remove")) {
            return "DELETE";
        }
        return "UNKNOWN";
    }

    private String extractEntityType(String className) {
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

    private String serializeState(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
