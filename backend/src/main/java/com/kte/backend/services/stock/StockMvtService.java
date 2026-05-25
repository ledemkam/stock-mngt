package com.kte.backend.services.stock;

import com.kte.backend.dto.requests.StockMvtRequest;
import com.kte.backend.dto.responses.StockMvtResponse;
import com.kte.backend.services.CrudServices;

public  interface StockMvtService extends CrudServices<StockMvtRequest, StockMvtResponse> {
}
