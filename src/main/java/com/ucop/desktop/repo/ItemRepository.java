package com.ucop.desktop.repo;
import com.ucop.desktop.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ItemRepository extends JpaRepository<Item, Integer> {
    List<Item> findByStatusOrderByNameAsc(String status);
    Optional<Item> findBySkuIgnoreCase(String sku);
}
