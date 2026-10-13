package vn.edu.ute.capstone.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PerformanceMonitoringAspect {

    /**
     * Pointcut bắt toàn bộ các phương thức trong tầng Service
     */
    @Pointcut("execution(* vn.edu.ute.capstone.service..*.*(..))")
    public void serviceLayerPointcut() {}

    /**
     * Đo thời gian thực thi (Execution Time) của từng method trong Service Layer.
     * Cảnh báo nếu phương thức chạy chậm hơn 500ms.
     */
    @Around("serviceLayerPointcut()")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (duration > 500) {
                log.warn("[SLOW EXECUTION ALERT] {}.{}() executed in {} ms (exceeds 500ms threshold)",
                        className, methodName, duration);
            } else {
                log.debug("[AOP Performance] {}.{}() took {} ms", className, methodName, duration);
            }
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[AOP Error] {}.{}() failed after {} ms with message: {}",
                    className, methodName, duration, ex.getMessage());
            throw ex;
        }
    }
}
