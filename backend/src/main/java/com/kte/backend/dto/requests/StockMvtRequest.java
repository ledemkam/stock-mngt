package com.kte.backend.dto.requests;

import com.kte.backend.enums.TypeMvt;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMvtRequest {

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be a positive number")
    private Integer quantity;

    @NotNull(message = "Movement type is required")
    private TypeMvt typeMvt;

    @NotNull(message = "Movement date is required")
    @PastOrPresent(message = "Movement date cannot be in the future")
    private LocalDateTime mvtDate;

    private String comment;

    @NotBlank(message = "Product ID is required")
    private String productId;
}