package com.deliverytech.delivery_api.listeners;

import com.deliverytech.delivery_api.events.product.ProductCreateEvent;
import com.deliverytech.delivery_api.events.product.ProductDeleteEvent;
import com.deliverytech.delivery_api.events.product.ProductUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ProductAuditListener {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @EventListener
    public void onProductCreated(ProductCreateEvent event) {
        auditLogger.info("CRUD_EVENT; type=CREATE; entity=Product; entityId={}; user={}; traceId={}",
                event.getProduct().getId(),
                event.getUser(),
                MDC.get("traceId")
        );
    }

    @EventListener
    public void onProductUpdate(ProductUpdateEvent event) {
        auditLogger.info("CRUD_EVENT; type=UPDATE; entity=Product; entityId={}; user={}; traceId={}",
                event.getProduct().getId(),
                event.getUser(),
                MDC.get("traceId")
        );
    }

    @EventListener
    public void onProductDelete(ProductDeleteEvent event) {
        auditLogger.info("CRUD_EVENT; type=DELETE; entity=Product; entityId={}; user={}; traceId={}",
                event.getProduct().getId(),
                event.getUser(),
                MDC.get("traceId")
        );
    }
}
