package com.insurecloud.notification;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationListener {

    private final MailSender mailSender;

    /**
     * SQS Listener that handles policy issued events to trigger customer notifications.
     *
     * @param event The policy issued event received from the queue.
     */
    @SqsListener("notification-queue")
    public void onPolicyIssued(PolicyIssuedEvent event) {
        log.info("Received policy issued event: {}", event);
        sendEmail(event);
    }

    /**
     * Sends an email notification to the customer using AWS SES.
     * Note: In a real-world scenario, you'd fetch the customer's email from a Customer Service.
     * For LocalStack, the sender/recipient can be any address, but they might need verification.
     *
     * @param event The policy details for the notification.
     */
    private void sendEmail(PolicyIssuedEvent event) {
        log.info("Sending real email via AWS SES to customer {} regarding policy {}", 
                event.customerId(), event.policyNumber());
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("notifications@insurecloud.com");
            message.setTo(event.customerId() + "@example.com"); // Dummy email based on customer ID
            message.setSubject("New Policy Issued: " + event.policyNumber());
            message.setText("Dear Customer,\n\nYour new insurance policy " + event.policyNumber() + 
                    " has been successfully issued for a premium of $" + event.premiumAmount() + ".\n\n" +
                    "Thank you for choosing InsureCloud!");

            mailSender.send(message);
            log.info("Successfully sent email notification for policy: {}", event.policyNumber());
        } catch (Exception e) {
            log.error("Failed to send email notification for policy: {}", event.policyNumber(), e);
        }
    }
}
