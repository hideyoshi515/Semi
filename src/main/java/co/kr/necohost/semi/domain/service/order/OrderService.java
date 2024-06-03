package co.kr.necohost.semi.domain.service.order;

import co.kr.necohost.semi.domain.repository.order.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    OrderRepository orderRepository;
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
