package vn.edu.ute.capstone.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class SecurityAuditAspect {

    /**
     * Bắt và ghi nhận log an toàn sau khi một hành động có gắn @AuditLog hoàn thành thành công
     */
    @AfterReturning(pointcut = "@annotation(auditLogAnnotation)", returning = "result")
    public void logSuccessfulAction(JoinPoint joinPoint, AuditLog auditLogAnnotation, Object result) {
        String username = getCurrentUsername();
        String action = auditLogAnnotation.action();
        String methodName = joinPoint.getSignature().toShortString();

        log.info("[AUDIT SUCCESS] Timestamp: {}, User: '{}', Action: '{}', Target: {}, Result: SUCCESS",
                LocalDateTime.now(), username, action, methodName);
    }

    /**
     * Bắt và ghi nhận log kiểm toán khi một hành động quan trọng bị ném ngoại lệ (thất bại hoặc bị chặn bởi chốt chặn nghiệp vụ)
     */
    @AfterThrowing(pointcut = "@annotation(auditLogAnnotation)", throwing = "exception")
    public void logFailedAction(JoinPoint joinPoint, AuditLog auditLogAnnotation, Throwable exception) {
        String username = getCurrentUsername();
        String action = auditLogAnnotation.action();
        String methodName = joinPoint.getSignature().toShortString();

        log.warn("[AUDIT VIOLATION/FAILURE] Timestamp: {}, User: '{}', Action: '{}', Target: {}, Reason: '{}'",
                LocalDateTime.now(), username, action, methodName, exception.getMessage());
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "ANONYMOUS";
    }
}
