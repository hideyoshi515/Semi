package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByPhone(String phone);
}
