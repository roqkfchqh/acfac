package com.example.acfac.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    private final ThreadLocal<Long> startTimeThreadLocal = new ThreadLocal<>();
    private final MetricsRecorder metricsRecorder;

    public LoggingAspect(MetricsRecorder metricsRecorder) {
        this.metricsRecorder = metricsRecorder;
    }

    @Around("execution(* com.example.acfac.common.RequestProcessor.processRequest(..))")
    public Object logProcessRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        metricsRecorder.incrementRequest();

        String methodName = joinPoint.getSignature().toShortString();
        startTimeThreadLocal.set(System.currentTimeMillis());
        log.info("logging {} started at {}", methodName, startTimeThreadLocal.get());

        try {
            Object result = joinPoint.proceed();

            long endTime = System.currentTimeMillis();
            long startTime = startTimeThreadLocal.get();
            log.info("logging {} completed at {}, total execution time: {} ms",
                    methodName, endTime, endTime - startTime);
            log.info("logging {} returned: {}", methodName, result);

            return result;

        } catch (Exception e) {
            metricsRecorder.incrementError(); // 오류 수 증가
            long errorTime = System.currentTimeMillis();
            log.error("logging {} : error at {}: {}", methodName, errorTime, e.getMessage());
            throw e;

        } finally {
            startTimeThreadLocal.remove();
        }
    }
}
