package com.deliverytech.delivery_api.model.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public enum CepZonesDistance {
    SHORT_DISTANCE,
    MEDIUM_DISTANCE,
    LONG_DISTANCE;

    private static final Map<CepZonesDistance, Set<String>> cepZonesDistance = new HashMap<>();

    static {
        cepZonesDistance.put(SHORT_DISTANCE, Set.of("0640", "0641", "0642", "0643"));
        cepZonesDistance.put(MEDIUM_DISTANCE, Set.of("0644", "0645", "0646"));
        cepZonesDistance.put(LONG_DISTANCE, Set.of("0647", "0648", "0649"));
    }

    public static Optional<CepZonesDistance> getCepZoneDistance(String cep) {
        return cepZonesDistance.entrySet().stream()
                .filter(entry -> entry.getValue().contains(cep.substring(0,4)))
                .map(Map.Entry::getKey)
                .findFirst();
    }
}
