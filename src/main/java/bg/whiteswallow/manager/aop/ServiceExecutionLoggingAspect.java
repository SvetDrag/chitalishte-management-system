package bg.whiteswallow.manager.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceExecutionLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceExecutionLoggingAspect.class);

    @Around("within(@org.springframework.stereotype.Service *)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMillis = System.currentTimeMillis() - startTime;
            log.debug("{} executed in {} ms", joinPoint.getSignature().toShortString(), elapsedMillis);
        }
    }
}
