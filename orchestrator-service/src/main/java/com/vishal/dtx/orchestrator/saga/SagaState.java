package com.vishal.dtx.orchestrator.saga;

public enum SagaState {
    STARTED,
    INVENTORY_RESERVED,
    PAYMENT_COMPLETED,
    COMPLETED,
    FAILED
}
