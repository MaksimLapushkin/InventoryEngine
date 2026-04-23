package com.maxlapushkin.inventory.service;

import com.maxlapushkin.inventory.dto.CreateOrderLineRequest;
import com.maxlapushkin.inventory.dto.CreateOrderRequest;
import com.maxlapushkin.inventory.dto.OrderResponse;
import com.maxlapushkin.inventory.exception.NotEnoughStockException;
import com.maxlapushkin.inventory.messaging.OrderLifecycleEvent;
import com.maxlapushkin.inventory.messaging.OrderLifecycleEventPublisher;
import com.maxlapushkin.inventory.messaging.OrderLifecycleEventType;
import com.maxlapushkin.inventory.messaging.OrderLifecyclePayload;
import com.maxlapushkin.inventory.messaging.OrderLinePayload;
import com.maxlapushkin.inventory.model.Order;
import com.maxlapushkin.inventory.model.OrderLine;
import com.maxlapushkin.inventory.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
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

    private static final String CUSTOMER_NAME = "Jane Smith";
    private static final String DELIVERY_ADDRESS = "123 Main Street";
    private static final String DELIVERY_CITY = "Budapest";
    private static final String DELIVERY_POSTAL_CODE = "1051";
    private static final String CUSTOMER_PHONE = "+36123456789";

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

        OrderResponse response = orderService.createOrder(createOrderRequest(7L, 2));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(repository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getCustomerName()).isEqualTo(CUSTOMER_NAME);
        assertThat(savedOrder.getDeliveryAddress()).isEqualTo(DELIVERY_ADDRESS);
        assertThat(savedOrder.getDeliveryCity()).isEqualTo(DELIVERY_CITY);
        assertThat(savedOrder.getDeliveryPostalCode()).isEqualTo(DELIVERY_POSTAL_CODE);
        assertThat(savedOrder.getCustomerPhone()).isEqualTo(CUSTOMER_PHONE);
        assertThat(response.getCustomerName()).isEqualTo(CUSTOMER_NAME);
        assertThat(response.getDeliveryAddress()).isEqualTo(DELIVERY_ADDRESS);
        assertThat(response.getDeliveryCity()).isEqualTo(DELIVERY_CITY);
        assertThat(response.getDeliveryPostalCode()).isEqualTo(DELIVERY_POSTAL_CODE);
        assertThat(response.getCustomerPhone()).isEqualTo(CUSTOMER_PHONE);

        OrderLifecycleEvent event = capturePublishedEvent();
        assertEnvelope(event, 11L, OrderLifecycleEventType.ORDER_CREATED);
        assertThat(event.payload()).isEqualTo(lifecyclePayload(11L, "CREATED", null, 7L, 2));
    }

    @Test
    void shouldPublishOrderReservedEvent() {
        Order order = withId(order(7L, 2), 12L);
        when(repository.findById(12L)).thenReturn(Optional.of(order));
        doAnswer(invocation -> {
            invocation.<Order>getArgument(1).reserve(3L);
            return null;
        }).when(stockService).reserveOrderAtomically(eq(3L), any(Order.class));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.reserve(12L, 3L);

        OrderLifecycleEvent event = capturePublishedEvent();
        assertEnvelope(event, 12L, OrderLifecycleEventType.ORDER_RESERVED);
        assertThat(event.payload()).isEqualTo(lifecyclePayload(12L, "RESERVED", 3L, 7L, 2));
    }

    @Test
    void shouldPublishOrderReleasedEvent() {
        Order order = withId(order(7L, 2), 13L);
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
        assertThat(event.payload()).isEqualTo(lifecyclePayload(13L, "CREATED", null, 7L, 2));
    }

    @Test
    void shouldFulfillOrderAndPublishFulfilledEvent() {
        Order order = withId(order(7L, 2), 18L);
        order.reserve(3L);
        when(repository.findById(18L)).thenReturn(Optional.of(order));
        doAnswer(invocation -> {
            invocation.<Order>getArgument(1).fulfill();
            return null;
        }).when(stockService).fulfillOrder(eq(3L), any(Order.class));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.fulfill(18L);

        verify(stockService).fulfillOrder(eq(3L), eq(order));
        OrderLifecycleEvent event = capturePublishedEvent();
        assertEnvelope(event, 18L, OrderLifecycleEventType.ORDER_FULFILLED);
        assertThat(event.payload()).isEqualTo(lifecyclePayload(18L, "FULFILLED", 3L, 7L, 2));
    }

    @Test
    void shouldPublishOrderCancelledEventForCreatedOrder() {
        Order order = withId(order(7L, 2), 14L);
        when(repository.findById(14L)).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.cancel(14L);

        OrderLifecycleEvent event = capturePublishedEvent();
        assertEnvelope(event, 14L, OrderLifecycleEventType.ORDER_CANCELLED);
        assertThat(event.payload()).isEqualTo(lifecyclePayload(14L, "CANCELLED", null, 7L, 2));
    }

    @Test
    void shouldPublishOnlyOrderCancelledWhenCancellingReservedOrder() {
        Order order = withId(order(7L, 2), 15L);
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
        assertThat(event.payload()).isEqualTo(lifecyclePayload(15L, "CANCELLED", null, 7L, 2));
        verify(orderLifecycleEventPublisher, times(1)).publish(any(OrderLifecycleEvent.class));
    }

    @Test
    void shouldUseSameCorrelationIdForEventsInSameOrderLifecycle() {
        List<Order> savedOrders = new ArrayList<>();
        when(repository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = withId(invocation.getArgument(0), 21L);
            savedOrders.add(order);
            return order;
        });

        orderService.createOrder(createOrderRequest(7L, 2));

        Order createdOrder = savedOrders.getFirst();
        when(repository.findById(21L)).thenReturn(Optional.of(createdOrder));
        doAnswer(invocation -> {
            invocation.<Order>getArgument(1).reserve(3L);
            return null;
        }).when(stockService).reserveOrderAtomically(eq(3L), eq(createdOrder));

        orderService.reserve(21L, 3L);

        ArgumentCaptor<OrderLifecycleEvent> eventCaptor = ArgumentCaptor.forClass(OrderLifecycleEvent.class);
        verify(orderLifecycleEventPublisher, times(2)).publish(eventCaptor.capture());

        assertThat(eventCaptor.getAllValues())
                .extracting(OrderLifecycleEvent::correlationId)
                .containsExactly("order-21", "order-21");
        assertThat(eventCaptor.getAllValues())
                .extracting(OrderLifecycleEvent::eventTypeEnum)
                .containsExactly(
                        OrderLifecycleEventType.ORDER_CREATED,
                        OrderLifecycleEventType.ORDER_RESERVED
                );
    }

    @Test
    void shouldNotPublishWhenReserveFails() {
        Order order = withId(order(7L, 2), 16L);
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
        Order order = withId(order(7L, 2), 17L);
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
        assertThat(event.correlationId()).isEqualTo("order-" + aggregateId);
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.eventType()).isEqualTo(eventType.name());
        assertThat(event.eventTypeEnum()).isEqualTo(eventType);
    }

    private Order withId(Order order, Long id) {
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }

    private Order order(Long productId, int quantity) {
        return new Order(
                List.of(new OrderLine(productId, quantity)),
                CUSTOMER_NAME,
                DELIVERY_ADDRESS,
                DELIVERY_CITY,
                DELIVERY_POSTAL_CODE,
                CUSTOMER_PHONE
        );
    }

    private OrderLifecyclePayload lifecyclePayload(
            Long orderId,
            String status,
            Long warehouseId,
            Long productId,
            int quantity
    ) {
        return new OrderLifecyclePayload(
                orderId,
                status,
                warehouseId,
                CUSTOMER_NAME,
                DELIVERY_ADDRESS,
                DELIVERY_CITY,
                DELIVERY_POSTAL_CODE,
                CUSTOMER_PHONE,
                List.of(new OrderLinePayload(productId, quantity))
        );
    }

    private CreateOrderRequest createOrderRequest(Long productId, int quantity) {
        CreateOrderLineRequest line = new CreateOrderLineRequest();
        line.setProductId(productId);
        line.setQuantity(quantity);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName(CUSTOMER_NAME);
        request.setDeliveryAddress(DELIVERY_ADDRESS);
        request.setDeliveryCity(DELIVERY_CITY);
        request.setDeliveryPostalCode(DELIVERY_POSTAL_CODE);
        request.setCustomerPhone(CUSTOMER_PHONE);
        request.setLines(List.of(line));
        return request;
    }
}
