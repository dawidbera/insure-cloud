package com.insurecloud.policy.audit;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    /**
     * Retrieves all {@link AuditLogEntry} objects from DynamoDB.
     * Use with caution as this performs a full table scan.
     *
     * @return A list of all audit log entries.
     */
    public List<AuditLogEntry> findAll() {
        return dynamoDBMapper.scan(AuditLogEntry.class, new DynamoDBScanExpression());
    }
}
