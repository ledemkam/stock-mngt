package com.kte.backend.dto.requests;

import com.kte.backend.entities.TypeMvt;
import lombok.*;

import java.time.LocalDateTime;


@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMvtRequest {

    private Integer quantity;

    private TypeMvt typeMvt;

    private LocalDateTime mvtDate;

    private String comment;

    private String ProductId;
}
