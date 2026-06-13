package com.kte.backend.dto.responses;


import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {



    private String name;

    private String reference;

    private String description;

    private Integer alertThreshold;

    private BigDecimal price;

    private String categoryName;

    private int availableQuantity;

}
