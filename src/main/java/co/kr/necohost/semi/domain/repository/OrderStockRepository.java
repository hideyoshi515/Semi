package co.kr.necohost.semi.domain.repository;


import co.kr.necohost.semi.domain.model.entity.OrderStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStockRepository extends JpaRepository<OrderStock, Integer> {
}