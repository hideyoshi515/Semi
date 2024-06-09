package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private MenuRepository menuRepository;
    private final DiscordNotificationService discordNotificationService;

    public OrderService(OrderRepository orderRepository, MenuRepository menuRepository, DiscordNotificationService discordNotificationService) {
        this.orderRepository = orderRepository;
        this.menuRepository = menuRepository;
        this.discordNotificationService = discordNotificationService;
    }

    public List<Sales> findByProcess(int process) {
        return orderRepository.findByProcess(process);
    }

    public List<Object[]> findSalesByProcess(int process) {
        return orderRepository.findSalesByProcess(process);
    }

    public Sales findById(int id) {
        return orderRepository.findById(id);
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

    @Transactional
    public void approveOrder(long orderId) {
        Sales order = orderRepository.findById(orderId);
        if (order != null) {
            discordNotificationService.sendOrderNotification(orderId);
        }
    }
}