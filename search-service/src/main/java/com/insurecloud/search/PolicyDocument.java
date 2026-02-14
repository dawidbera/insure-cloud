package com.insurecloud.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;

/**
 * Elasticsearch document representing a policy for search purposes.
 */
@Document(indexName = "policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String policyNumber;

    @Field(type = FieldType.Keyword)
    private String customerId;

    @Field(type = FieldType.Double)
    private BigDecimal premiumAmount;
}
