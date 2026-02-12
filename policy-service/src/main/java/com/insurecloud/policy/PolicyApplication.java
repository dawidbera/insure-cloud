package com.insurecloud.policy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
    io.awspring.cloud.autoconfigure.sns.SnsAutoConfiguration.class,
    io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration.class
})
public class PolicyApplication {
    public static void main(String[] args) {
        SpringApplication.run(PolicyApplication.class, args);
    }
}
