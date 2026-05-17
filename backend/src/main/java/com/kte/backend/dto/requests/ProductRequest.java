package com.kte.backend.dto.requests;

import lombok.*;

import java.math.BigDecimal;


@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {


    private String name;


    private String reference;


    private String description;


    private Integer alertThreshold;


    private BigDecimal price;

    private String categoryId;


}
