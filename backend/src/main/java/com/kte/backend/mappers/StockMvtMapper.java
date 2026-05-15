package com.kte.backend.mappers;

import com.kte.backend.entities.Product;
import com.kte.backend.entities.StockMvt;
import com.kte.backend.requests.StockMvtRequest;
import com.kte.backend.responses.StockMvtResponse;
import org.springframework.stereotype.Component;

@Component
public class StockMvtMapper {

    public StockMvt toEntity(final StockMvtRequest request) {
        return StockMvt.builder()
                .mvtDate(request.getMvtDate())
                .comment(request.getComment())
                .typeMvt(request.getTypeMvt())
                .quantity(request.getQuantity())
                .product(Product.builder()
                        .id(request.getProductId())
                        .build())
                .build();
    }

    public StockMvtResponse toResponse(final StockMvt entity) {
        return StockMvtResponse.builder()
                .mvtDate(entity.getMvtDate())
                .comment(entity.getComment())
                .typeMvt(entity.getTypeMvt())
                .quantity(entity.getQuantity())
                .build();
    }
}
