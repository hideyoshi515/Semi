package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Sales> findByProcess(int process) {
        return orderRepository.findByProcess(process);
    }

    public List<Object[]> findSalesByProcess(int process) {
        return orderRepository.findSalesByProcess(process);
    }

    public Sales findById(int id) {
        return orderRepository.findById(id).orElse(null);
    }

    public List<Object[]> findSalesById(long orderId) {
        return orderRepository.findSalesById(orderId);
    }

    public void updateDenialProcess(long orderId) {
        orderRepository.updateDenialProcess(orderId);
    }
}