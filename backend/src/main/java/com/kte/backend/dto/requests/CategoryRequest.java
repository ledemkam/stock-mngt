package com.kte.backend.dto.requests;


import jakarta.persistence.Entity;
import lombok.*;


@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    private String name;

    private String description;
}
