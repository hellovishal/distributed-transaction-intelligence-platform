package com.vishal.dtx.common.saga;

public enum SagaState {
    STARTED,
    CREATED,
    INVENTORY_RESERVED,
    PAYMENT_COMPLETED,
    COMPLETED,
    FAILED
}
