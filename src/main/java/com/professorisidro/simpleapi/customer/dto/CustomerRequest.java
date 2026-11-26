package com.professorisidro.simpleapi.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email String email,
        @Size(max = 20) String phone
) {
}
