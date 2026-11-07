package com.ucop.desktop.repo;
import com.ucop.desktop.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByUsername(String username);
    java.util.List<Account> findAllByOrderByUsernameAsc();
}
