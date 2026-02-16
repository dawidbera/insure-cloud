package com.insurecloud.policy.audit;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Repository for saving {@link AuditLogEntry} objects to DynamoDB.
 * This class abstracts the interaction with DynamoDB for audit logging purposes.
 */
@Repository
@RequiredArgsConstructor
public class AuditLogRepository {

    private final DynamoDBMapper dynamoDBMapper;

    /**
     * Saves an {@link AuditLogEntry} to DynamoDB.
     *
     * @param entry The AuditLogEntry to save. It must have all required fields set.
     */
    public void save(AuditLogEntry entry) {
        dynamoDBMapper.save(entry);
    }
}
