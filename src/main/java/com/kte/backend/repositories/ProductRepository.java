package com.kte.backend.repositories;

import com.kte.backend.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByNameIgnoreCase(String name);
}
