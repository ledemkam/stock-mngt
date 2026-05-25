package com.kte.backend.repositories;

import com.kte.backend.entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {

    boolean existsByCompagnyCode(String compagnyCode);

    boolean existsByEmail(String email);
}
