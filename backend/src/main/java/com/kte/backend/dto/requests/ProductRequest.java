package com.kte.backend.dto.requests;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 150, message = "Product name must be between 2 and 150 characters")
    private String name;

    @NotBlank(message = "Reference is required")
    @Size(min = 2, max = 50, message = "Reference must be between 2 and 50 characters")
    private String reference;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Alert threshold is required")
    @Positive(message = "Alert threshold must be a positive number")
    private Integer alertThreshold;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be a positive number")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 digits and 2 decimal places")
    private BigDecimal price;

    @NotBlank(message = "Category ID is required")
    private String categoryId;
}