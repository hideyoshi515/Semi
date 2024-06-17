package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.entity.OrderStock;
import co.kr.necohost.semi.domain.repository.OrderStockRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderStockService {
    private final OrderStockRepository orderStockRepository;

    public OrderStockService(OrderStockRepository orderStockRepository) {
        this.orderStockRepository = orderStockRepository;
    }

    // すべての注文在庫を取得
    public List<OrderStock> getAllOrderStocks() {
        return orderStockRepository.findAll();
    }
}
