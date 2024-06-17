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

	// 注文を承認
	@Transactional
	public void approveOrder(long orderId, String message) {
		var order = orderRepository.findById((int) orderId).orElse(null);

		if (order != null) {
			System.out.println("주문승인");
			discordBotService.sendOrderNotification(message);
		}
	}

	// プロセス別に注文を検索
	public List<Sales> findByProcess(int process) {
		return orderRepository.findByProcess(process);
	}

	// プロセス別に売上を検索
	public List<Object[]> findSalesByProcess(int process) {
		return orderRepository.findSalesByProcess(process);
	}

	// プロセスとデバイス別に売上を検索
	public List<Object[]> findSalesByProcessAndDevice(int process) {
		return orderRepository.findSalesByProcessAndDevice(process);
	}

	// 注文IDでデバイス名を表示して検索
	public List<Object[]> findByIdAndShowDeviceName(long orderId) {
		return orderRepository.findByIdAndShowDeviceName(orderId);
	}

	// 注文IDで注文を検索
	public Sales findById(int id) {
		return orderRepository.findById(id).orElse(null);
	}

	// 注文IDで売上を検索
	public List<Object[]> findSalesById(long orderId) {
		return orderRepository.findSalesById(orderId);
	}

	// プロセスで否定を更新
	public void updateDenialByProcess(long orderId) {
		orderRepository.updateDenialByProcess(orderId);
	}

	// 注文承認を更新
	public void updateOrderApproval(long orderId, int orderQuantity, long menuId) {
		orderRepository.updateSalesProcess(orderId);
		orderRepository.updateMenuStock(menuId, orderQuantity);
	}
}
