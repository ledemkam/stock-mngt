package com.kte.backend.controllers;

import com.kte.backend.common.PageReponse;
import com.kte.backend.controllers.uicontrollers.UIStockMvtController;
import com.kte.backend.dto.requests.StockMvtRequest;
import com.kte.backend.dto.responses.StockMvtResponse;
import com.kte.backend.services.stock.StockMvtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/stock-mvts")
public class StockMvtController implements UIStockMvtController {

    private final StockMvtService stockMvtService;

    @Override
    @PostMapping
    public ResponseEntity<Void> createStockMvt(
            @Valid
            @RequestBody
            final StockMvtRequest request) {
        log.info("Received request to create stock movement : {}", request);
        stockMvtService.create(request);
        log.info("Stock movement created successfully: {}", request);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @Override
    @PutMapping(path = "/{stock-mvt-id}")
    public ResponseEntity<Void> updateStockMvt(
            @Valid
            @NotNull(message = "Stock movement ID must not be null")
            @RequestBody
            @PathVariable("stock-mvt-id")
            final String id, final StockMvtRequest request) {
        log.info("Received request to update stock movement with id {}: {}", id, request);
        stockMvtService.update(id, request);
        log.info("Stock movement with id {} updated successfully: {}", id, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    @GetMapping
    public ResponseEntity<PageReponse<StockMvtResponse>> getAllStckMvts(
            @RequestParam(name = "page", defaultValue = "0")
            final int page,
            @RequestParam(name = "size", defaultValue = "10")
            final int size) {
        log.debug("Received request to get all stock movements - page: {}, size: {}", page, size);
         PageReponse<StockMvtResponse> response = stockMvtService.findAll(page, size);
        log.debug("Returning {} stock movements", response.getContent().size());
        return ResponseEntity.ok(response);
    }


    @Override
    @GetMapping(path = "/{stock-mvt-id}")
    public ResponseEntity<StockMvtResponse> getStockMvtById(
            @NotNull(message = "Stock movement ID must not be null")
            @PathVariable("stock-mvt-id")
            final String id) {
        log.debug("Received request to get stock movement with id {}", id);
         StockMvtResponse response = stockMvtService.findById(id);
        log.debug("Returning stock movement with id {}: {}", id, response);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping(path = "/{stock-mvt-id}")
    public ResponseEntity<Void> deleteStockMvt(
            @NotNull(message = "Stock movement ID must not be null")
            @PathVariable("stock-mvt-id")
            final String id) {
        log.info("Received request to delete stock movement with id {}", id);
        stockMvtService.delete(id);
        log.info("Stock movement with id {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}
