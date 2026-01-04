package com.vishal.dtx.orchestrator.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "saga_instance")
public class SagaInstance {

    @Id
    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Setter
    @Column(name = "state", nullable = false)
    private String state;

    @Setter
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Required by JPA
    protected SagaInstance() {
    }

    public SagaInstance(String transactionId, String state, Instant updatedAt) {
        this.transactionId = transactionId;
        this.state = state;
        this.updatedAt = updatedAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getState() {
        return state;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

}
