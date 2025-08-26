package com.example.cloudfour.cartservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PerformanceAspect {

    @Around("execution(* com.example.cloudfour.cartservice.domain..service..*(..))")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (executionTime > 1000) {
                log.warn("느린 메서드 실행: {} - {}ms", methodName, executionTime);
            } else {
                log.debug("메서드 실행 완료: {} - {}ms", methodName, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("메서드 실행 실패: {} - {}ms, 오류: {}", methodName, executionTime, e.getMessage());
            throw e;
        }
    }

    @Around("execution(* com.example.cloudfour.cartservice.client..*(..))")
    public Object measureClientExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (executionTime > 2000) {
                log.warn("느린 외부 서비스 호출: {} - {}ms", methodName, executionTime);
            } else {
                log.debug("외부 서비스 호출 완료: {} - {}ms", methodName, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("외부 서비스 호출 실패: {} - {}ms, 오류: {}", methodName, executionTime, e.getMessage());
            throw e;
        }
    }
}
