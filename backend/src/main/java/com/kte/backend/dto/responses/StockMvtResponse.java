package com.kte.backend.dto.responses;

import com.kte.backend.entities.TypeMvt;
import lombok.*;

import java.time.LocalDateTime;


@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMvtResponse {

    private Integer quantity;

    private TypeMvt typeMvt;

    private LocalDateTime mvtDate;

    private  String comment;
}
