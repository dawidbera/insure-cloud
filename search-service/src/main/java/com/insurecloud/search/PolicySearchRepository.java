package com.insurecloud.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PolicyDocument in Elasticsearch.
 */
@Repository
public interface PolicySearchRepository extends ElasticsearchRepository<PolicyDocument, String> {
    
    List<PolicyDocument> findByCustomerId(String customerId);
    
    List<PolicyDocument> findByPolicyNumber(String policyNumber);
}
