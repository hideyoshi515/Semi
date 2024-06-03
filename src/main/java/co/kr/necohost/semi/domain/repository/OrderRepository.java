package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
