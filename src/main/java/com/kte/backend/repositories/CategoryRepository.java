package com.kte.backend.repositories;


import com.kte.backend.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {


    Optional<Category> findByNameIgnoreCase(String name);
}
