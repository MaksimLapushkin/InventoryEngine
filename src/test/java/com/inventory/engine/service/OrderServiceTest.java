package com.inventory.engine.service;

import com.inventory.engine.dto.CreateOrderLineRequest;
import com.inventory.engine.dto.CreateOrderRequest;
import com.inventory.engine.exception.NotEnoughStockException;
import com.inventory.engine.messaging.OrderLifecycleEvent;
import com.inventory.engine.messaging.OrderLifecycleEventPublisher;
import com.inventory.engine.messaging.OrderLifecycleEventType;
import com.inventory.engine.messaging.OrderLifecyclePayload;
import com.inventory.engine.messaging.OrderLinePayload;
import com.inventory.engine.model.Order;
import com.inventory.engine.model.OrderLine;
import com.inventory.engine.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private StockService stockService;

    @Mock
    private OrderLifecycleEventPublisher orderLifecycleEventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldPublishOrderCreatedEvent() {
        when(repository.save(any(Order.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 11L));

        orderService.createOrder(createOrderRequest(7L, 2));

        OrderLifecycleEvent event = capturePublishedEvent();
        assertEnvelope(event, 11L, OrderLifecycleEventType.ORDER_CREATED);
        assertThat(event.payload()).isEqualTo(new OrderLifecyclePayload(
                11L,
                "CREATED",
                null,
                List.of(new OrderLinePayload(7L, 2))
        ));
    }

    @Test
    void shouldPublishOrderReservedEvent() {
        Order order = withId(new Order(List.of(new OrderLine(7L, 2))), 12L);
        when(repository.findById(12L)).thenReturn(Optional.of(order));
        doAnswer(invocation -> {
            invocation.<Order>getArgument(1).reserve(3L);
            return null;
        }).when(stockService).reserveOrderAtomically(eq(3L), any(Order.class));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.reserve(12L, 3L);

        OrderLifecycleEvent event = capturePublishedEvent();
        assertEnvelope(event, 12L, OrderLifecycleEventType.ORDER_RESERVED);
        assertThat(event.payload()).isEqualTo(new OrderLifecyclePayload(
                12L,
                "RESERVED",
                3L,
                List.of(new OrderLinePayload(7L, 2))
        ));
    }

    @Test
    void shouldPublishOrderReleasedEvent() {
        Order order = withId(new Order(List.of(new OrderLine(7L, 2))), 13L);
        order.reserve(3L);
        when(repository.findById(13L)).thenReturn(Optional.of(order));
        doAnswer(invocation -> {
            invocation.<Order>getArgument(1).releaseReservation();
            return null;
        }).when(stockService).releaseOrderReservation(eq(3L), any(Order.class));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.releaseReservation(13L);

        OrderLifecycleEvent event = capturePublishedEvent();
        assertEnvelope(event, 13L, OrderLifecycleEventType.ORDER_RELEASED);
        assertThat(event.payload()).isEqualTo(new OrderLifecyclePayload(
                13L,
                "CREATED",
                null,
                List.of(new OrderLinePayload(7L, 2))
        ));
    }

    @Test
    void shouldPublishOrderCancelledEventForCreatedOrder() {
        Order order = withId(new Order(List.of(new OrderLine(7L, 2))), 14L);
        when(repository.findById(14L)).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.cancel(14L);

        OrderLifecycleEvent event = capturePublishedEvent();
        assertEnvelope(event, 14L, OrderLifecycleEventType.ORDER_CANCELLED);
        assertThat(event.payload()).isEqualTo(new OrderLifecyclePayload(
                14L,
                "CANCELLED",
                null,
                List.of(new OrderLinePayload(7L, 2))
        ));
    }

    @Test
    void shouldPublishOnlyOrderCancelledWhenCancellingReservedOrder() {
        Order order = withId(new Order(List.of(new OrderLine(7L, 2))), 15L);
        order.reserve(3L);
        when(repository.findById(15L)).thenReturn(Optional.of(order));
        doAnswer(invocation -> {
            invocation.<Order>getArgument(1).releaseReservation();
            return null;
        }).when(stockService).releaseOrderReservation(eq(3L), any(Order.class));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.cancel(15L);

        OrderLifecycleEvent event = capturePublishedEvent();
        assertEnvelope(event, 15L, OrderLifecycleEventType.ORDER_CANCELLED);
        assertThat(event.payload()).isEqualTo(new OrderLifecyclePayload(
                15L,
                "CANCELLED",
                null,
                List.of(new OrderLinePayload(7L, 2))
        ));
        verify(orderLifecycleEventPublisher, times(1)).publish(any(OrderLifecycleEvent.class));
    }

    @Test
    void shouldNotPublishWhenReserveFails() {
        Order order = withId(new Order(List.of(new OrderLine(7L, 2))), 16L);
        when(repository.findById(16L)).thenReturn(Optional.of(order));
        doThrow(new NotEnoughStockException(7L, 3L))
                .when(stockService).reserveOrderAtomically(3L, order);

        assertThatThrownBy(() -> orderService.reserve(16L, 3L))
                .isInstanceOf(NotEnoughStockException.class);

        verify(repository, never()).save(any(Order.class));
        verify(orderLifecycleEventPublisher, never()).publish(any(OrderLifecycleEvent.class));
    }

    @Test
    void shouldNotPublishWhenReleaseFails() {
        Order order = withId(new Order(List.of(new OrderLine(7L, 2))), 17L);
        when(repository.findById(17L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.releaseReservation(17L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESERVED");

        verify(repository, never()).save(any(Order.class));
        verify(orderLifecycleEventPublisher, never()).publish(any(OrderLifecycleEvent.class));
    }

    private OrderLifecycleEvent capturePublishedEvent() {
        ArgumentCaptor<OrderLifecycleEvent> eventCaptor = ArgumentCaptor.forClass(OrderLifecycleEvent.class);
        verify(orderLifecycleEventPublisher).publish(eventCaptor.capture());
        return eventCaptor.getValue();
    }

    private void assertEnvelope(OrderLifecycleEvent event, Long aggregateId, OrderLifecycleEventType eventType) {
        assertThat(event.eventId()).isNotNull();
        assertThat(event.aggregateId()).isEqualTo(aggregateId);
        assertThat(event.correlationId()).isNotBlank();
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.eventType()).isEqualTo(eventType.name());
        assertThat(event.eventTypeEnum()).isEqualTo(eventType);
    }

    private Order withId(Order order, Long id) {
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }

    private CreateOrderRequest createOrderRequest(Long productId, int quantity) {
        CreateOrderLineRequest line = new CreateOrderLineRequest();
        line.setProductId(productId);
        line.setQuantity(quantity);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setLines(List.of(line));
        return request;
    }
}
