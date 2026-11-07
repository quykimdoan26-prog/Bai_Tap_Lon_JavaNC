package com.ucop.desktop.repo;
import com.ucop.desktop.domain.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
public interface OrderRepository extends JpaRepository<CustomerOrder, Integer> {
    List<CustomerOrder> findByStatusOrderByCreatedAtDesc(String status);
    List<CustomerOrder> findByAccountOrderByCreatedAtDesc(com.ucop.desktop.domain.Account account);
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
    List<CustomerOrder> findByCreatedAtBetweenAndStatusNot(
            LocalDateTime from, LocalDateTime to, String status);
}
