package com.deliverytech.delivery_api.actuator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomInfoContributor implements InfoContributor {

    private final Environment environment;

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> envDetails = new HashMap<>();
        envDetails.put("active-profiles", environment.getActiveProfiles());
        envDetails.put("jvm-version", System.getProperty("java.version"));

        Map<String, Object> customData = new HashMap<>();
        customData.put("project", "Delivery API");
        customData.put("institution", "Delivery Tech");

        builder.withDetail("environment", envDetails);
        builder.withDetail("customData", customData);
    }
}
