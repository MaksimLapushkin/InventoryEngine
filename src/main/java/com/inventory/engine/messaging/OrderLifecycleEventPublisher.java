package com.inventory.engine.messaging;

public interface OrderLifecycleEventPublisher {

    void publish(OrderLifecycleEvent event);
}
