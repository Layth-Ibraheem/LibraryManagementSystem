package com.layth.Library.Management.System.aspects.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // Log before a method is executed
    @Before("execution(* com.layth.Library.Management.System.services.BookService(..))")
    public void logBeforeMethod(JoinPoint joinPoint) {
        log.info("Executing method: {}", joinPoint.getSignature());
    }

    // Log after a method successfully returns
    @AfterReturning(pointcut = "execution(* com.layth.Library.Management.System.services.BookService(..))", returning = "result")
    public void logAfterMethod(JoinPoint joinPoint, Object result) {
        log.info("Method {} executed successfully with return: {}", joinPoint.getSignature(), result);
    }

    // Log after a method throws an exception
    @AfterThrowing(pointcut = "execution(* com.layth.Library.Management.System.services.BookService(..))", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        log.error("Exception in method {}: {}", joinPoint.getSignature(), exception.getMessage());
    }

    // Log the execution time of a method using @Around
    @Around("execution(* com.layth.Library.Management.System.services.BookService(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();  // Execute the method

        long executionTime = System.currentTimeMillis() - start;
        log.info("Method {} executed in {} ms", joinPoint.getSignature(), executionTime);

        return proceed;
    }
}
