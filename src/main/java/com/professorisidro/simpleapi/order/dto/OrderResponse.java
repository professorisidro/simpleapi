package com.professorisidro.simpleapi.order.dto;

import com.professorisidro.simpleapi.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        String customerName,
        LocalDateTime orderDate,
        OrderStatus status,
        BigDecimal total,
        List<OrderItemResponse> items
) {
}
