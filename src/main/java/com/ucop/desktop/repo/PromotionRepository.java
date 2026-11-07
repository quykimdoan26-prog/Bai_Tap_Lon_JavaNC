package com.ucop.desktop.repo;
import com.ucop.desktop.domain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    Optional<Promotion> findByCodeIgnoreCase(String code);
}
