package com.deliverytech.delivery_api.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuditLoggingAspect {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerPointcut() {}

    @Around("restControllerPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();
        String user = "ANONYMOUS";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            user = authentication.getName();
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        auditLogger.info("REQUEST; user={}; method={}; uri={}; params={}; correlationId={}",
                user,
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                MDC.get("correlationId")
        );

        Object result;
        try {
            result = joinPoint.proceed();

        } catch (Throwable t) {
            long duration = System.currentTimeMillis() - startTime;
            auditLogger.error("FAILED_REQUEST; user={}; method={}; uri={}; durationMs={}; error={}; correlationId={}",
                    user,
                    request.getMethod(),
                    request.getRequestURI(),
                    duration,
                    t.getMessage(),
                    MDC.get("correlationId")
            );
            throw t;
        }

        long duration = System.currentTimeMillis() - startTime;
        auditLogger.info("SUCCESS_REQUEST; user={}; method={}; uri={}; durationMs={}; correlationId={}",
                user,
                request.getMethod(),
                request.getRequestURI(),
                duration,
                MDC.get("correlationId")
        );

        return result;
    }
}
