package com.insurecloud.policy.audit;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuditLogRepository}.
 * These tests ensure that the repository correctly interacts with {@link DynamoDBMapper}
 * to save audit log entries.
 */
@ExtendWith(MockitoExtension.class)
class AuditLogRepositoryTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    @InjectMocks
    private AuditLogRepository auditLogRepository;

    private AuditLogEntry testEntry;

    /**
     * Sets up a mock {@link AuditLogEntry} before each test.
     */
    @BeforeEach
    void setUp() {
        testEntry = AuditLogEntry.builder()
                .eventId("test-event-id")
                .userId("test-user")
                .policyId("test-policy-id")
                .eventType("POLICY_CREATED")
                .timestamp(Instant.now())
                .details(Map.of("key", "value"))
                .build();
    }

    /**
     * Verifies that the {@code save} method of {@link AuditLogRepository}
     * correctly calls the {@code save} method of {@link DynamoDBMapper} with the provided entry.
     */
    @Test
    void save_shouldCallMapperSaveWithCorrectEntry() {
        // When
        auditLogRepository.save(testEntry);

        // Then
        // Verify that dynamoDBMapper.save() was called exactly once with the testEntry
        verify(dynamoDBMapper, times(1)).save(testEntry);
    }
}
