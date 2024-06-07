package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.OrderNum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderNumRepository extends JpaRepository<OrderNum, Integer> {
}
