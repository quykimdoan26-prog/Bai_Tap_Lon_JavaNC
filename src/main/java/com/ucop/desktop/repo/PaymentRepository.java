package com.ucop.desktop.repo;
import com.ucop.desktop.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ucop.desktop.domain.CustomerOrder;
import java.util.Optional;
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByOrder(CustomerOrder order);
}
