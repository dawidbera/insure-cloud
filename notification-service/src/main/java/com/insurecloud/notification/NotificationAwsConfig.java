package com.insurecloud.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import io.awspring.cloud.ses.SimpleEmailServiceMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesAsyncClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Configuration
public class NotificationAwsConfig {

    @Value("${spring.cloud.aws.endpoint:http://localhost:4566}")
    private String awsEndpoint;

    private static final Region REGION = Region.US_EAST_1;

    /**
     * Configures the asynchronous SQS client with endpoint override and static credentials.
     *
     * @return A configured SqsAsyncClient bean.
     */
    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .endpointOverride(URI.create(awsEndpoint))
                .region(REGION)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .build();
    }

    /**
     * Configures the synchronous SES client for use with MailSender.
     *
     * @return A configured SesClient bean.
     */
    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .endpointOverride(URI.create(awsEndpoint))
                .region(REGION)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .build();
    }

    /**
     * Configures the asynchronous SES client.
     *
     * @return A configured SesAsyncClient bean.
     */
    @Bean
    public SesAsyncClient sesAsyncClient() {
        return SesAsyncClient.builder()
                .endpointOverride(URI.create(awsEndpoint))
                .region(REGION)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .build();
    }
}
