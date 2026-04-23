package com.maxlapushkin.inventory.messaging;

import java.util.Objects;

public record OrderLinePayload(
        Long productId,
        int quantity
) {
    public OrderLinePayload {
        productId = Objects.requireNonNull(productId, "productId must not be null");
    }
}
