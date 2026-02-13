package com.insurecloud.notification;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationListener {

    @SqsListener("notification-queue")
    public void onPolicyIssued(PolicyIssuedEvent event) {
        log.info("Received policy issued event: {}", event);
        sendEmail(event);
    }

    private void sendEmail(PolicyIssuedEvent event) {
        log.info("SENDED EMAIL to customer {} regarding policy {}", 
                event.customerId(), event.policyNumber());
        // Here we would integrate with an email provider (SES, SendGrid, etc.)
    }
}
