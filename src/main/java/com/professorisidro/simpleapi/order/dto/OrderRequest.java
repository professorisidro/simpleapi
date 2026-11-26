package com.professorisidro.simpleapi.order.dto;

import com.professorisidro.simpleapi.order.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequest(
        @NotNull Long customerId,
        OrderStatus status, // optional; defaults to PENDING when null
        @NotEmpty @Valid List<OrderItemRequest> items
) {
}
