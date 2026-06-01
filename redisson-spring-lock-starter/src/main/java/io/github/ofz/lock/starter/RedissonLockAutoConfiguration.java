package io.github.ofz.lock.starter;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RedissonClient.class)
@ComponentScan("io.github.ofz.lock.core")
public class RedissonLockAutoConfiguration {
}
