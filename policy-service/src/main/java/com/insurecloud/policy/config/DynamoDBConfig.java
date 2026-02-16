package com.insurecloud.policy.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for AWS DynamoDB client to connect to LocalStack.
 * This class provides beans for AmazonDynamoDB and DynamoDBMapper,
 * allowing the application to interact with DynamoDB for audit logging.
 */
@Configuration
public class DynamoDBConfig {

    @Value("${spring.cloud.aws.endpoint:http://localhost:4566}")
    private String awsEndpoint;

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.access-key-id:test}")
    private String awsAccessKeyId;

    @Value("${aws.secret-access-key:test}")
    private String awsSecretAccessKey;

    /**
     * Provides AWS credentials for connecting to DynamoDB.
     * These credentials are typically for local development with LocalStack.
     * @return AWSCredentials instance.
     */
    public AWSCredentials amazonAWSCredentials() {
        return new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
    }

    /**
     * Configures and provides the AmazonDynamoDB client.
     * It uses the endpoint and region specified in application properties,
     * along with the provided AWS credentials.
     * @return Configured AmazonDynamoDB client.
     */
    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsEndpoint, awsRegion))
                .withCredentials(new AWSStaticCredentialsProvider(amazonAWSCredentials()))
                .build();
    }

    /**
     * Provides the DynamoDBMapper bean, which is used for object-to-table mapping
     * for DynamoDB entities like AuditLogEntry.
     * @return DynamoDBMapper instance.
     */
    @Bean
    public DynamoDBMapper dynamoDBMapper() {
        return new DynamoDBMapper(amazonDynamoDB());
    }
}
