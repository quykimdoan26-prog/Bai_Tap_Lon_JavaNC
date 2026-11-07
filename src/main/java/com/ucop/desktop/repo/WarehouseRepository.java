package com.ucop.desktop.repo;

import com.ucop.desktop.domain.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {
    java.util.Optional<Warehouse> findByCodeIgnoreCase(String code);
}
