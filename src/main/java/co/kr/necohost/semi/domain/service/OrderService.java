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
	private final DiscordBotService discordBotService;
	private final MenuRepository menuRepository;

    public OrderService(OrderRepository orderRepository, MenuRepository menuRepository, DiscordBotService discordBotService) {
        this.orderRepository = orderRepository;
        this.menuRepository = menuRepository;
        this.discordBotService = discordBotService;
    }

    public List<Sales> findByProcess(int process) {
        return orderRepository.findByProcess(process);
    }

    public List<Object[]> findSalesByProcess(int process) {
        return orderRepository.findSalesByProcess(process);
    }

    public List<Object[]> findSalesByProcessAndDevice(int process) {
        return orderRepository.findSalesByProcessAndDevice(process);
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

    @Transactional
    public void approveOrder(long orderId, String message) {
        Sales order = orderRepository.findById((int) orderId).orElse(null);

        if (order != null) {
            System.out.println("주문승인");
            discordBotService.sendOrderNotification(message);
        }
    }
}