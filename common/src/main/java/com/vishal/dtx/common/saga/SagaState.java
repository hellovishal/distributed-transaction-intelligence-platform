package com.vishal.dtx.common.saga;

public enum SagaState {
    STARTED,
    CREATED,
    INVENTORY_RESERVED,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    INVENTORY_RELEASED,
    COMPLETED,
    FAILED
}