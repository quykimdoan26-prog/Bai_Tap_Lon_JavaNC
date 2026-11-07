package com.ucop.desktop.repo;
import com.ucop.desktop.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoryRepository extends JpaRepository<Category, Integer> {}
