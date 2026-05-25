package com.kte.backend.services.catalog;

import com.kte.backend.dto.requests.ProductRequest;
import com.kte.backend.dto.responses.ProductResponse;
import com.kte.backend.services.CrudServices;

public interface ProductService extends CrudServices<ProductRequest, ProductResponse> {
}
