package io.github.ofz.lock.springboot.demo.service;

import io.github.ofz.lock.core.annotation.DistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @DistributedLock(
            key = "#orderId",
            prefix = "order",
            waitTime = 3,
            leaseTime = 10
    )
    public String createOrder(Long orderId) throws InterruptedException {
        log.info("开始创建订单：" + orderId);
        Thread.sleep(5000); // 模拟业务耗时
        return "Order " + orderId + " created";
    }
}
