package com.reliaquest.api.aop;

import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    public static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(public * com.reliaquest.api.controller.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        logger.info("Executing method: " + methodName + " with arguments: " + Arrays.toString(joinPoint.getArgs()));
    }

    @After("execution(public * com.reliaquest.api.controller.*.*(..))")
    public void logAfter(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        logger.info("Method executed: " + methodName);
    }

    @Before("execution(public * com.reliaquest.api.service.EmployeeService.*(..))")
    public void logBeforeService(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        logger.info("Executing method: " + methodName + " with arguments: " + Arrays.toString(joinPoint.getArgs()));
    }

    @After("execution(public * com.reliaquest.api.service.EmployeeService.*(..))")
    public void logAfterService(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        logger.info("Method executed: " + methodName);
    }
}
