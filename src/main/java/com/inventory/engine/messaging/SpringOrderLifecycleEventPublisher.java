package com.inventory.engine.messaging;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringOrderLifecycleEventPublisher implements OrderLifecycleEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringOrderLifecycleEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(OrderLifecycleEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
