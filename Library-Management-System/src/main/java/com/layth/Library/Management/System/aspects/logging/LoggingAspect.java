package com.layth.Library.Management.System.aspects.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logs every service call with its duration, and the exception type when it fails.
 * <p>
 * Arguments and return values are never logged: they include raw passwords (login and
 * register) and entities with password hashes, and they would flood the log.
 */
@Aspect
@Component
public class LoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /** Any method of any class in the services package or its subpackages. */
    @Pointcut("execution(* com.layth.Library.Management.System.services..*(..))")
    public void serviceMethods() {
    }

    @Around("serviceMethods()")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            log.debug("{} completed in {} ms", method, elapsedMillis(start));
            return result;
        } catch (Throwable ex) {
            log.info("{} failed after {} ms with {}: {}", method, elapsedMillis(start),
                    ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
