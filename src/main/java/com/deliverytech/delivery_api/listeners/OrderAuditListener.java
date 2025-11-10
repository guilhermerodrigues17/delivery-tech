package com.deliverytech.delivery_api.listeners;

import com.deliverytech.delivery_api.events.order.OrderCancelEvent;
import com.deliverytech.delivery_api.events.order.OrderCreatedEvent;
import com.deliverytech.delivery_api.events.order.OrderStatusUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderAuditListener {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        auditLogger.info("CRUD_EVENT; type=CREATE; entity=Order; entityId={}; user={}; traceId={}",
                event.getOrder().getId(),
                event.getUser(),
                MDC.get("traceId")
        );
    }

    @EventListener
    public void onOrderStatusUpdate(OrderStatusUpdateEvent event) {
        auditLogger.info("CRUD_EVENT; type=UPDATE; entity=Order; entityId={}; user={}; traceId={}",
                event.getOrder().getId(),
                event.getUser(),
                MDC.get("traceId")
        );
    }

    @EventListener
    public void onOrderCancel(OrderCancelEvent event) {
        auditLogger.info("CRUD_EVENT; type=CANCEL; entity=Order; entityId={}; user={}; traceId={}",
                event.getOrder().getId(),
                event.getUser(),
                MDC.get("traceId")
        );
    }
}
