package com.ordertracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OrderTrackingApplication {

    private static final Logger log = LoggerFactory.getLogger(OrderTrackingApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(OrderTrackingApplication.class, args);
        
    }
}
