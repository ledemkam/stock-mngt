package com.kte.backend.services.impl;


import com.kte.backend.common.PageReponse;
import com.kte.backend.entities.Product;
import com.kte.backend.entities.StockMvt;
import com.kte.backend.mappers.StockMvtMapper;
import com.kte.backend.repositories.ProductRepository;
import com.kte.backend.repositories.StockMvtRepository;
import com.kte.backend.requests.StockMvtRequest;
import com.kte.backend.responses.StockMvtResponse;
import com.kte.backend.services.StockMvtService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockMvtServiceImpl implements StockMvtService {

    private final StockMvtRepository stockMvtRepository;
    private final StockMvtMapper stockMvtMapper;
    private final ProductRepository productRepository;

    @Override
    public void create(final StockMvtRequest request) {
        scheckIfProductExistById(request.getProductId());
        final StockMvt entity = stockMvtMapper.toEntity(request);
        log.info("Saving stock movement: {}", entity);
        stockMvtRepository.save(entity);
    }

    @Override
    public void update(final String id, StockMvtRequest request) {
        final StockMvt existingStockMvt = stockMvtRepository.findById(id)
                .orElseThrow(() -> {
                    log.debug("Stock movement with id {} not found", id);
                    return new EntityNotFoundException("Stock movement not found");
                });
        scheckIfProductExistById(request.getProductId());
        final StockMvt stockMvtToUpdate = stockMvtMapper.toEntity(request);
        stockMvtToUpdate.setId(id);
        log.info("Updating stock movement with id {}: {}", id, stockMvtToUpdate);
        stockMvtRepository.save(stockMvtToUpdate);

    }

    @Override
    public PageReponse<StockMvtResponse> findAll(final int page, int size) {
        return null;
    }

    @Override
    public StockMvtResponse findById(final String id) {
        return null;
    }

    @Override
    public void delete(final String id) {

    }

    private  void scheckIfProductExistById(final String productId){
        final Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.debug("Product with id {} not found", productId);
                    return new EntityNotFoundException("Product not found");
                });
    }




}
