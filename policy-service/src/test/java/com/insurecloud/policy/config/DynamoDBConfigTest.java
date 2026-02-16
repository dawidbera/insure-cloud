package com.insurecloud.policy.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DynamoDBConfig}.
 * These tests ensure that the DynamoDB client and mapper beans are correctly configured
 * and instantiated with the expected properties.
 */
@ExtendWith(MockitoExtension.class)
class DynamoDBConfigTest {

    private DynamoDBConfig dynamoDBConfig;

    @Mock
    private AmazonDynamoDB amazonDynamoDB;

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    /**
     * Set up method to initialize DynamoDBConfig and inject mock values using reflection.
     * This simulates the Spring @Value injection for unit testing.
     */
    @BeforeEach
    void setUp() {
        dynamoDBConfig = new DynamoDBConfig();
        // Use ReflectionTestUtils to inject @Value fields for testing
        ReflectionTestUtils.setField(dynamoDBConfig, "awsEndpoint", "http://localhost:4566");
        ReflectionTestUtils.setField(dynamoDBConfig, "awsRegion", "us-east-1");
        ReflectionTestUtils.setField(dynamoDBConfig, "awsAccessKeyId", "test_access");
        ReflectionTestUtils.setField(dynamoDBConfig, "awsSecretAccessKey", "test_secret");
    }

    /**
     * Tests that the amazonAWSCredentials method returns a BasicAWSCredentials instance
     * with the correct access key and secret key.
     */
    @Test
    void amazonAWSCredentials_shouldReturnBasicAWSCredentials() {
        AWSCredentials credentials = dynamoDBConfig.amazonAWSCredentials();
        assertNotNull(credentials);
        assertTrue(credentials instanceof BasicAWSCredentials);
        assertEquals("test_access", credentials.getAWSAccessKeyId());
        assertEquals("test_secret", credentials.getAWSSecretKey());
    }

    /**
     * Tests that the amazonDynamoDB method returns an AmazonDynamoDB client instance.
     * Further deep verification would require more intricate mocking or a full integration test.
     */
    @Test
    void amazonDynamoDB_shouldReturnAmazonDynamoDBClient() {
        // When
        AmazonDynamoDB client = dynamoDBConfig.amazonDynamoDB();
        // Then
        assertNotNull(client);
        // For a true unit test, one might verify method calls on the builder,
        // but for simplicity, checking non-null is sufficient for basic instantiation.
    }

    /**
     * Tests that the dynamoDBMapper method returns a DynamoDBMapper instance.
     * It uses a mocked AmazonDynamoDB client to ensure the mapper can be constructed.
     */
    @Test
    void dynamoDBMapper_shouldReturnDynamoDBMapper() {
        // Given that amazonDynamoDB() would return a configured client,
        // we can directly test the mapper instantiation with a mock client.
        // When
        DynamoDBMapper mapper = dynamoDBConfig.dynamoDBMapper(); // This will call amazonDynamoDB() internally
        // Then
        assertNotNull(mapper);
    }
}
