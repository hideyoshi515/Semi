package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private MenuRepository menuRepository;

    public OrderService(OrderRepository orderRepository, MenuRepository menuRepository) {
        this.orderRepository = orderRepository;
        this.menuRepository = menuRepository;
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

    public void updateDenialByProcess(long orderId) {
        orderRepository.updateDenialByProcess(orderId);
    }

    public void updateOrderApproval(long orderId, int orderQuantity, long menuId) {
        orderRepository.updateSalesProcess(orderId);
        orderRepository.updateMenuStock(menuId, orderQuantity);
    }
}