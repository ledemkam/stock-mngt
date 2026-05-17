package com.kte.backend.responses;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {


    private String name;

    private String description;

    private int nbProducts;

}