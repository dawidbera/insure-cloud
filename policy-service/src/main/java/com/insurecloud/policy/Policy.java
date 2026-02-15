package com.insurecloud.policy;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Policy number is mandatory")
    @Column(nullable = false, unique = true)
    private String policyNumber;

    @NotBlank(message = "Customer ID is mandatory")
    @Column(nullable = false)
    private String customerId;

    @NotNull(message = "Start date is mandatory")
    @FutureOrPresent(message = "Start date must be today or in the future")
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is mandatory")
    @Column(nullable = false)
    private LocalDate endDate;

    @NotNull(message = "Premium amount is mandatory")
    @Positive(message = "Premium amount must be positive")
    @Column(nullable = false)
    private BigDecimal premiumAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    public enum PolicyStatus {
        DRAFT, ACTIVE, EXPIRED, CANCELLED
    }
}
