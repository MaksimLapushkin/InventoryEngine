package com.maxlapushkin.inventory.messaging;

public interface OrderLifecycleEventPublisher {

    void publish(OrderLifecycleEvent event);
}
