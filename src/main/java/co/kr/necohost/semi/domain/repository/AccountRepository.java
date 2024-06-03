package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
