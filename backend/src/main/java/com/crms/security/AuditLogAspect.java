package com.crms.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@Aspect
public class AuditLogAspect {

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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        String method = joinPoint.getSignature().toShortString();
        
        log.info("AUDIT: User '{}' completed '{}' successfully at {}", username, method, LocalDateTime.now());
    }

    @AfterThrowing(pointcut = "controllerMethods() && (postMappingMethods() || putMappingMethods() || patchMappingMethods() || deleteMappingMethods())", 
                   throwing = "exception")
    public void auditAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        String method = joinPoint.getSignature().toShortString();
        
        log.error("AUDIT: User '{}' failed '{}' with exception: {}", username, method, exception.getMessage());
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
