package co.kr.necohost.semi.domain.repository.order;

import co.kr.necohost.semi.domain.model.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
