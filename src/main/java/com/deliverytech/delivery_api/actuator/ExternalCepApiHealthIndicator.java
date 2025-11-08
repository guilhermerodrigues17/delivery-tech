package com.deliverytech.delivery_api.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class ExternalCepApiHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {

        boolean isServiceUp = checkExternalService();

        if (!isServiceUp) {
            return Health.down()
                    .withDetail("service", "ViaCEP API service")
                    .withDetail("error", "DOWN")
                    .build();
        }

        return Health.up()
                .withDetail("service", "ViaCEP API service")
                .withDetail("status", "UP")
                .build();
    }

    /*
        Generate a random number between 0 - 10
        Simulate randomness in the availability of the external service
     */
    private boolean checkExternalService() {
        var random = new Random();
        int number = random.nextInt(11);

        return number % 2 == 0;
    }
}