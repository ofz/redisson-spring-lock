package io.github.ofz.lock.core.aspect;

import io.github.ofz.lock.core.annotation.DistributedLock;
import io.github.ofz.lock.core.key.LockKeyParser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class DistributedLockAspect {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockAspect.class);

    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(lock)")
    public Object around(ProceedingJoinPoint pjp, DistributedLock lock) throws Throwable {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        String key = LockKeyParser.parse(ms.getMethod(), pjp.getArgs(), lock.key());
        String lockKey = lock.prefix() + ":" + key;

        int retry = lock.retry();
        long delay = lock.delay();
        double multiplier = lock.multiplier();

        RLock rLock = redissonClient.getLock(lockKey);
        boolean locked = false;

        try {
            if(retry <= 0){
                throw new IllegalStateException("Can't to acquire lock: " + lockKey);
            }
            int retryCount = 0;
            while (true){
                locked = rLock.tryLock(lock.waitTime(), lock.leaseTime(), TimeUnit.SECONDS);
                if(locked){
                    log.debug("Lock acquired, key={}", lockKey);
                    return pjp.proceed();
                }

                retry--;
                retryCount++;

                if(retry <= 0){
                    throw new IllegalStateException("Failed to acquire lock: " + lockKey);
                }
                long sleepTimeStep = Double.valueOf(delay * (1 + (retryCount - 1) * multiplier)).longValue();
                log.debug("Lock not acquired, retrying... key={}", lockKey);
                Thread.sleep(sleepTimeStep);
            }
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                try {
                    rLock.unlock();
                    log.debug("Lock released, key={}", lockKey);
                } catch (Throwable t) {
                    log.error("Unlock failed, key={}", lockKey, t);
                }
            }
            if (locked && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }
}
