package com.professorisidro.simpleapi.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String sku,
        BigDecimal price,
        Integer stockQuantity
) {
}
