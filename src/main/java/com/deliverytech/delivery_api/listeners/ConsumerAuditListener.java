package com.deliverytech.delivery_api.listeners;

import com.deliverytech.delivery_api.events.consumer.ConsumerCreateEvent;
import com.deliverytech.delivery_api.events.consumer.ConsumerDisableEvent;
import com.deliverytech.delivery_api.events.consumer.ConsumerUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumerAuditListener {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @EventListener
    public void onConsumerCreated(ConsumerCreateEvent event) {
        auditLogger.info("CRUD_EVENT; type=CREATE; entity=Consumer; entityId={}; user={}; traceId={}",
                event.getConsumer().getId(),
                event.getUser(),
                MDC.get("traceId")
        );
    }

    @EventListener
    public void onConsumerUpdate(ConsumerUpdateEvent event) {
        auditLogger.info("CRUD_EVENT; type=UPDATE; entity=Consumer; entityId={}; user={}; traceId={}",
                event.getConsumer().getId(),
                event.getUser(),
                MDC.get("traceId")
        );
    }

    @EventListener
    public void onConsumerDisable(ConsumerDisableEvent event) {
        auditLogger.info("CRUD_EVENT; type=DELETE; entity=Consumer; entityId={}; user={}; traceId={}",
                event.getConsumer().getId(),
                event.getUser(),
                MDC.get("traceId")
        );
    }
}
