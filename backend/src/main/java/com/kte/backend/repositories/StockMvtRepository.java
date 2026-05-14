package com.kte.backend.repositories;

import com.kte.backend.entities.StockMvt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMvtRepository extends JpaRepository<StockMvt, String> {
}
