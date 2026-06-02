# redisson-spring-lock

基于 Redisson 的 Spring Boot 注解式分布式锁，支持 Spring Boot 2.x & 3.x。

Annotation-based distributed lock for Spring Boot (2.x & 3.x), powered by Redisson.

---

## 模块结构 / Modules

```
redisson-spring-lock
├── redisson-spring-lock-core      # 核心实现 / Core implementation
├── redisson-spring-lock-starter   # Spring Boot 自动装配 / Spring Boot auto-configuration
└── redisson-spring-lock-demo-spring-boot  # 示例工程 / Demo project
```

---

## 快速开始 / Quick Start

### 1. 添加依赖 / Add Dependency

```xml
<dependency>
    <groupId>io.github.ofz</groupId>
    <artifactId>redisson-spring-lock-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 配置 RedissonClient / Configure RedissonClient

在你的 Spring 配置类中注册 `RedissonClient` Bean：

Register a `RedissonClient` bean in your Spring configuration:

```java
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setDatabase(0);
        return Redisson.create(config);
    }
}
```

### 3. 使用注解 / Use the Annotation

在需要加分布式锁的方法上添加 `@DistributedLock`，`key` 支持 Spring EL 表达式：

Add `@DistributedLock` to any method that needs a distributed lock. The `key` field supports Spring EL expressions:

```java
@Service
public class OrderService {

    @DistributedLock(
            key = "#orderId",
            prefix = "order",
            waitTime = 3,
            leaseTime = 10
    )
    public String createOrder(Long orderId) {
        // 业务逻辑 / business logic
        return "Order " + orderId + " created";
    }
}
```

最终加锁的 Redis key 为 `{prefix}:{key}`，例如上例为 `order:12345`。

The final Redis lock key is `{prefix}:{key}`, e.g. `order:12345` in the example above.

---

## 注解参数说明 / Annotation Parameters

| 参数 / Parameter | 类型 / Type | 默认值 / Default | 说明 / Description |
|---|---|---|---|
| `key` | `String` | — | 锁的唯一标识，支持 SpEL 表达式 / Lock key, supports SpEL expressions |
| `prefix` | `String` | `"lock"` | Redis key 前缀 / Redis key prefix |
| `waitTime` | `int` | `5` | 尝试获取锁的等待时间（秒）/ Max wait time to acquire lock (seconds) |
| `leaseTime` | `int` | `30` | 锁的持有时间（秒）/ Lock lease time (seconds) |
| `retry` | `int` | `1` | 最大重试次数 / Max retry attempts |
| `delay` | `long` | `1000` | 重试初始间隔（毫秒）/ Initial retry delay (ms) |
| `multiplier` | `double` | `0` | 重试间隔递增倍数，0 表示固定间隔 / Retry delay multiplier; 0 means fixed interval |

### 重试退避说明 / Retry Backoff

每次重试的等待时间计算公式：

Each retry sleep duration is calculated as:

```
sleepTime = delay * (1 + (retryCount - 1) * multiplier)
```

- `multiplier = 0`：固定间隔，每次等待 `delay` ms / Fixed interval
- `multiplier > 0`：线性递增，例如 `delay=1000, multiplier=1` 时，第 1 次等 1000ms，第 2 次等 2000ms，以此类推 / Linear backoff

---

## SpEL 表达式示例 / SpEL Key Examples

```java
// 使用方法参数 / Use method parameter
@DistributedLock(key = "#userId")
public void doSomething(Long userId) { ... }

// 使用对象属性 / Use object field
@DistributedLock(key = "#order.id")
public void processOrder(Order order) { ... }

// 字符串拼接 / String concatenation
@DistributedLock(key = "#userId + ':' + #productId")
public void purchase(Long userId, Long productId) { ... }
```

---

## 工作原理 / How It Works

1. AOP 拦截所有标注了 `@DistributedLock` 的方法。
2. 通过 Spring EL 解析 `key` 表达式，拼接 `prefix` 生成最终 Redis key。
3. 调用 Redisson `tryLock(waitTime, leaseTime, TimeUnit.SECONDS)` 尝试获取锁。
4. 获取成功则执行目标方法，执行完毕后自动释放锁。
5. 获取失败则按配置进行重试，超过重试次数后抛出 `IllegalStateException`。

---

1. AOP intercepts all methods annotated with `@DistributedLock`.
2. The `key` SpEL expression is evaluated and combined with `prefix` to form the final Redis key.
3. Redisson `tryLock(waitTime, leaseTime, TimeUnit.SECONDS)` is called to acquire the lock.
4. On success, the target method executes and the lock is released automatically in the `finally` block.
5. On failure, retries are performed according to configuration; after exhausting retries, `IllegalStateException` is thrown.

---

## 环境要求 / Requirements

- Java 8+
- Spring Boot 2.x or 3.x
- Redis（单机 / 哨兵 / 集群均支持，取决于 RedissonClient 配置）
- Redis (standalone / sentinel / cluster, depending on your `RedissonClient` configuration)

---

## License

[Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)
