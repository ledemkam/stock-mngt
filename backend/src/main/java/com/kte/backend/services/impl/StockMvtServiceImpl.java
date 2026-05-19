package com.kte.backend.services.impl;


import com.kte.backend.common.PageReponse;
import com.kte.backend.entities.StockMvt;
import com.kte.backend.mappers.StockMvtMapper;
import com.kte.backend.repositories.ProductRepository;
import com.kte.backend.repositories.StockMvtRepository;
import com.kte.backend.dto.requests.StockMvtRequest;
import com.kte.backend.dto.responses.StockMvtResponse;
import com.kte.backend.services.StockMvtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StockMvtServiceImpl implements StockMvtService {

    private static final String STOCK_MVT_NOT_FOUND = "Stock movement not found";
    private static final String STOCK_MVT_WITH_ID_NOT_FOUND = "stock movement with id {} not found";

    private final StockMvtRepository stockMvtRepository;
    private final StockMvtMapper stockMvtMapper;
    private final ProductRepository productRepository;

    @Override
    public void create(final StockMvtRequest request) {
        checkIfProductExistById(request.getProductId());
        final StockMvt entity = stockMvtMapper.toEntity(request);
        log.info("Saving stock movement: {}", entity);
        stockMvtRepository.save(entity);
    }

    @Override
    public void update(final String id, StockMvtRequest request) {
         stockMvtRepository.findById(id)
                .orElseThrow(() -> {
                    log.debug(STOCK_MVT_WITH_ID_NOT_FOUND, id);
                    return new EntityNotFoundException(STOCK_MVT_NOT_FOUND);
                });
        checkIfProductExistById(request.getProductId());
        final StockMvt stockMvtToUpdate = stockMvtMapper.toEntity(request);
        stockMvtToUpdate.setId(id);
        log.info("Updating stock movement with id {}: {}", id, stockMvtToUpdate);
        stockMvtRepository.save(stockMvtToUpdate);

    }

    @Override
    public PageReponse<StockMvtResponse> findAll(final int page, int size) {
        log.debug("Fetching stock movements - page: {}, size: {}", page, size);
        final PageRequest pageRequest = PageRequest.of(page, size);
        Page <StockMvt> stockMvtPage = stockMvtRepository.findAll(pageRequest);
        Page<StockMvtResponse> stockMvtResponsePage = stockMvtPage.map(stockMvtMapper::toResponse);
        log.debug("Fetched {} stock movements", stockMvtPage.getNumberOfElements());
        return PageReponse.of(stockMvtResponsePage);
    }

    @Override
    public StockMvtResponse findById(final String id) {
        log.debug("Finding stock movement with id: {}", id);
        return stockMvtRepository.findById(id)
                .map(stockMvtMapper::toResponse)
                .orElseThrow(() ->{
                log.info(STOCK_MVT_WITH_ID_NOT_FOUND, id);
                    return new EntityNotFoundException(STOCK_MVT_NOT_FOUND);
                });

    }

    @Override
    public void delete(final String id) {
        final StockMvt stockMvt = stockMvtRepository.findById(id)
                .orElseThrow(() -> {
                    log.debug(STOCK_MVT_WITH_ID_NOT_FOUND, id);
                    return new EntityNotFoundException(STOCK_MVT_NOT_FOUND);
                });
        log.info("Deleting stock movement with id {}: {}", id, stockMvt);
        stockMvtRepository.delete(stockMvt);

    }

    private void checkIfProductExistById(final String productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.debug(STOCK_MVT_WITH_ID_NOT_FOUND, productId);
                    return new EntityNotFoundException("Product not found");
                });
    }




}
