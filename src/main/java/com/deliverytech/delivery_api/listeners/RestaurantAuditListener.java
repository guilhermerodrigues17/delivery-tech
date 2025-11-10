package com.deliverytech.delivery_api.listeners;

import com.deliverytech.delivery_api.events.restaurant.RestaurantCreatedEvent;
import com.deliverytech.delivery_api.events.restaurant.RestaurantDisableEvent;
import com.deliverytech.delivery_api.events.restaurant.RestaurantUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RestaurantAuditListener {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @EventListener
    public void onRestaurantCreated(RestaurantCreatedEvent event) {
        auditLogger.info("CRUD_EVENT; type=CREATE; entity=Restaurant; entityId={}; user={}; traceId={}",
                event.getRestaurant().getId(),
                event.getUser(),
                MDC.get("traceId")
        );
    }

    @EventListener
    public void onRestaurantUpdate(RestaurantUpdateEvent event) {
        auditLogger.info("CRUD_EVENT; type=UPDATE; entity=Restaurant; entityId={}; user={}; traceId={}",
                event.getRestaurant().getId(),
                event.getUser(),
                MDC.get("traceId")
        );
    }

    @EventListener
    public void onRestaurantDisable(RestaurantDisableEvent event) {
        auditLogger.info("CRUD_EVENT; type=DISABLE; entity=Restaurant; entityId={}; user={}; traceId={}",
                event.getRestaurant().getId(),
                event.getUser(),
                MDC.get("traceId")
        );
    }
}
