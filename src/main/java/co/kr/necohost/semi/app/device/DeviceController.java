package co.kr.necohost.semi.app.device;

import co.kr.necohost.semi.domain.model.dto.AccountRequest;
import co.kr.necohost.semi.domain.model.dto.DeviceRequest;
import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.*;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.repository.OrderNumRepository;
import co.kr.necohost.semi.domain.repository.OrderRepository;
import co.kr.necohost.semi.domain.service.*;
import co.kr.necohost.semi.websocket.OrderWebSocketHandler;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DeviceController {
	private final MenuService menuService;
	private final DeviceService deviceService;
	private final SalesService salesService;
	private final MenuRepository menuRepository;
	private final AccountService accountService;
	private final CategoryService categoryService;
	private final OrderRepository orderRepository;
	private final OrderNumRepository orderNumRepository;
	private final OrderWebSocketHandler orderWebSocketHandler;
	private final CouponService couponService;

	public DeviceController(MenuService menuService, DeviceService deviceService, SalesService salesService, MenuRepository menuRepository, AccountService accountService, CategoryService categoryService, OrderRepository orderRepository, OrderNumRepository orderNumRepository, OrderWebSocketHandler orderWebSocketHandler, CouponService couponService) {
		this.menuService = menuService;
		this.deviceService = deviceService;
		this.salesService = salesService;
		this.menuRepository = menuRepository;
		this.accountService = accountService;
		this.categoryService = categoryService;
		this.orderRepository = orderRepository;
		this.orderNumRepository = orderNumRepository;
		this.orderWebSocketHandler = orderWebSocketHandler;
		this.couponService = couponService;
	}

	private int calculatePoints(long price) {
		return (int) (price * 0.01);
	}

	private Map<Menu, Integer> getSessionOrders(HttpSession session) {
		return (Map<Menu, Integer>) session.getAttribute("orders");
	}

	private long calculateTotalPrice(Map<Menu, Integer> orders) {
		return orders.entrySet().stream()
				.mapToLong(entry -> (long) entry.getKey().getPrice() * entry.getValue())
				.sum();
	}

	@RequestMapping(value = "/order", method = RequestMethod.GET)
	public String getOrder(Model model) {
		model.addAttribute("deviceRequest", new DeviceRequest());
		List<Device> device = deviceService.findAll();
		model.addAttribute("devices", device);
		return "order/orderOption.html";
	}

	@RequestMapping(value = "/orderStart", method = RequestMethod.POST)
	public String postOrder(DeviceRequest deviceRequest, Model model, @RequestParam Map<String, Object> params) {
		model.addAttribute("deviceRequest", deviceRequest);
		Map<Long, List<Menu>> categorizedMenus = menuService.getCategorizedMenus();
		model.addAttribute("categorizedMenus", categorizedMenus);
		List<Menu> menus = menuService.getAllMenus();
		List<Category> categories = categoryService.getAllCategories();

		int categoryCount = 0;
		List<List<Menu>> menusList = new ArrayList<>();
		for (Category category : categories) {
			categoryCount++;
			menusList.add(menuService.getMenuByCategory((int) category.getId()));
		}

		int index = 0;
		for (List<Menu> listItems : menusList) {
			model.addAttribute("section" + (index + 1) + "Items", listItems.get(index));
		}

		model.addAttribute("menus", menus);
		model.addAttribute("categories", categories);
		model.addAttribute("categoryCount", categoryCount);
		model.addAttribute("menusList", menusList);
		return "order/orderMenuSelect.html";
	}

	@RequestMapping(value = "/orderPayment", method = RequestMethod.POST)
	public String postOrderPayment(HttpSession session, Model model, DeviceRequest deviceRequest) {
		Map<Menu, Integer> orders = getSessionOrders(session);
		if (orders == null || orders.isEmpty()) {
			return "order/orderMenuSelect.html"; // 주문이 없는 경우
		}

		long totalPrice = calculateTotalPrice(orders);
		model.addAttribute("orderedItems", orders);
		model.addAttribute("totalPrice", totalPrice);

		return "order/orderPaymentSelect.html";
	}

	@RequestMapping(value = "/orderPayment", method = RequestMethod.GET)
	public String getOrderPayment(Model model, DeviceRequest deviceRequest, HttpSession session) {
		Map<Menu, Integer> orders = getSessionOrders(session);
		if (orders == null || orders.isEmpty()) {
			return "order/orderMenuSelect.html"; // 주문이 없는 경우
		}

		long totalPrice = calculateTotalPrice(orders);
		model.addAttribute("orderedItems", orders);
		model.addAttribute("totalPrice", totalPrice);





		return "order/orderPaymentSelect.html";
	}

	@RequestMapping(value = "/orderPaymentSelect", method = RequestMethod.POST)
	public String OrderPaymentSelect(HttpSession session, Model model,@RequestParam String paymentMethod, AccountRequest accountRequest,@RequestParam Map<String, Object> params) {
		Map<Menu, Integer> orders = getSessionOrders(session);

		DeviceRequest deviceRequest = createDefaultDeviceRequest(orders);

		long totalPrice = processOrder(orders, deviceRequest, paymentMethod, accountRequest);

		model.addAttribute("orderedItems", orders);
		model.addAttribute("totalPrice", totalPrice);


		int salePrice = 0;

		LocalDateTime localDate = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formattedDateTime = localDate.format(formatter);
		OrderNum orderNum = orderNumRepository.save(new OrderNum());

		String couponNum = (String) params.get("couponNum");

		Coupon coupon = couponService.findByCouponNum(couponNum);

		for (Map.Entry<Menu, Integer> entry : orders.entrySet()) {
			Menu menu = entry.getKey();

			int quantity = entry.getValue();

			SalesRequest sales = new SalesRequest();

			salePrice = menu.getPrice();

			if (couponNum != null && !couponNum.isEmpty()) {

				if (coupon != null) {

					if (coupon.getProcess() == 0) {
						salePrice *= 0.9;  // 10% 할인 적용

					}
				}
			}

			sales.setDate(LocalDateTime.parse(formattedDateTime, formatter));
			sales.setCategory(menu.getCategory());
			sales.setMenu(menu.getId());
			sales.setPrice(salePrice);
			sales.setQuantity(quantity);
			sales.setDevice(2);
			sales.setDeviceNum(1);
			sales.setOrderNum(orderNum.getOrderNum());
			sales.setProcess(0);

			salesService.save(sales);

			menu.setStock(menu.getStock() - quantity);

			menuRepository.save(menu);

		}

		if (coupon != null) {
			if (coupon.getProcess() == 0) {
				coupon.setProcess(1);  // 쿠폰 사용 처리
				couponService.save(coupon);
			}
		}

		orders.clear();

		if (params.get("phone") != null) {
			String phoneNum = (String) params.get("phone");
			Account account = accountService.getAccountByPhone(phoneNum);
			account.setMsPoint((int) (account.getMsPoint() + (salePrice * 0.01)));
			accountService.save(account);
		}

		// WebSocket 메시지 전송
        orderWebSocketHandler.sendMessageToAllSessions(new TextMessage("Order"));


        // WebSocket을 통해 새로운 주문 알림 전송
		String message = "New order created: " + deviceRequest.getOrderNum();
		for (WebSocketSession webSocketSession : orderWebSocketHandler.getSessions()) {
			try {
				webSocketSession.sendMessage(new TextMessage(message));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return switch (paymentMethod) {
			case "CASH", "CARD", "PAY" -> "order/orderPayment.html";
			default -> {
				model.addAttribute("error", "유효하지 않은 결제방법입니다.");
				yield "order/orderPaymentSelect.html";
			}
		};
	}
	@RequestMapping(value = "/orderCall", method = RequestMethod.GET)
	public String orderCall(Model model) {
		return "order/orderCall.html";
	}
	@RequestMapping(value = "/orderCall", method = RequestMethod.POST)
	public String orderCall2(Model model) {
		return "order/orderCall2.html";
	}

	private DeviceRequest createDefaultDeviceRequest(Map<Menu, Integer> orders) {
		DeviceRequest deviceRequest = new DeviceRequest();

		Map<Long, Integer> quantities = new HashMap<>();
		for (Map.Entry<Menu, Integer> entry : orders.entrySet()) {
			quantities.put(entry.getKey().getId(), entry.getValue());
		}
		deviceRequest.setQuantities(quantities);
		deviceRequest.setPhoneNum("defaultPhone");
		deviceRequest.setPass("defaultPass");

		return deviceRequest;
	}

	private long processOrder(Map<Menu, Integer> orders, DeviceRequest deviceRequest, String paymentMethod, AccountRequest accountRequest) {
		long totalPrice = 0;
		String phone = deviceRequest.getPhoneNum();
		String pass = deviceRequest.getPass();

		OrderNum orderNum = orderNumRepository.save(new OrderNum());

		for (Map.Entry<Long, Integer> entry : deviceRequest.getQuantities().entrySet()) {
			Long menuId = entry.getKey();
			Integer quantity = entry.getValue();
			Menu menu = menuService.getMenuById(menuId);
			Account account = accountService.getAccountByPhone(phone);

			if (quantity != null && quantity > 0 && quantity <= menu.getStock()) {
				totalPrice += (long) menu.getPrice() * quantity;
				LocalDateTime localDate = LocalDateTime.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				String formattedDateTime = localDate.format(formatter);

				SalesRequest salesRequest = new SalesRequest();
				salesRequest.setCategory(menu.getCategory());
				salesRequest.setDate(LocalDateTime.parse(formattedDateTime, formatter));
				salesRequest.setMenu(menu.getId());
				salesRequest.setQuantity(quantity);
				salesRequest.setDevice(1);
				salesRequest.setDeviceNum(0);
				salesRequest.setOrderNum(deviceRequest.getOrderNum());
				salesRequest.setProcess(0);
				salesRequest.setOrderNum(orderNum.getOrderNum());

				try {
//					salesService.save(salesRequest);
				} catch (Exception e) {
					e.printStackTrace();
				}

				menu.setStock(menu.getStock() - quantity);
				menuService.saveMenu(menu);
			}
		}
		return totalPrice;
	}

	@ResponseBody
	@RequestMapping(value = "/order/cart", method = RequestMethod.GET)
	public Map<Menu, Integer> getOrderCart(HttpSession session, Model model) {
		return getSessionOrders(session);
	}

	@PostMapping("/addToSession")
	@ResponseBody
	public String addToSession(@RequestBody Map<String, Object> data, HttpSession session) {
		String menuIdStr = (String) data.get("menuId");
		Integer quantity = (Integer) data.get("quantity");

		if (menuIdStr == null || menuIdStr.isEmpty()) {
			return "error: menuId is null or empty";
		}

		Long menuId;
		try {
			menuId = Long.valueOf(menuIdStr);
		} catch (NumberFormatException e) {
			return "error: invalid menuId";
		}

		Menu menu = menuRepository.findById(menuId).orElse(null);
		if (menu == null) {
			return "error: menu not found";
		}

		Map<Menu, Integer> orders = getSessionOrders(session);
		if (orders == null) {
			orders = new HashMap<>();
		}

		orders.put(menu, quantity);
		session.setAttribute("orders", orders);

		return "success";
	}

	@GetMapping("/getCartItems")
	@ResponseBody
	public Map<String, Object> getCartItems(HttpSession session) {
		Map<Menu, Integer> orders = getSessionOrders(session);
		Map<String, Object> response = new HashMap<>();
		if (orders != null) {
			for (Map.Entry<Menu, Integer> entry : orders.entrySet()) {
				Map<String, Object> item = new HashMap<>();
				item.put("name", entry.getKey().getName());
				item.put("price", entry.getKey().getPrice());
				item.put("quantity", entry.getValue());
				item.put("stock", entry.getKey().getStock());
				response.put(String.valueOf(entry.getKey().getId()), item);
			}
		}
		return response;
	}

	@PostMapping("/updateCartQuantity")
	@ResponseBody
	public String updateCartQuantity(@RequestBody Map<String, Object> data, HttpSession session) {
		Long menuId = ((Number) data.get("menuId")).longValue();
		Integer quantity = (Integer) data.get("quantity");

		Menu menu = menuRepository.findById(menuId).orElse(null);
		if (menu == null) {
			return "error: menu not found";
		}

		Map<Menu, Integer> orders = getSessionOrders(session);
		if (orders == null) {
			orders = new HashMap<>();
		}

		if (quantity > 0) {
			orders.put(menu, quantity);
		} else {
			orders.remove(menu);
		}

		session.setAttribute("orders", orders);

		return "success";
	}

	@PostMapping("deleteCartItem")
	@ResponseBody
	public String deleteCartItem(@RequestBody Map<String, Object> data, HttpSession session) {
		Long menuId = ((Number) data.get("menuId")).longValue();
		Menu menu = menuRepository.findById(menuId).orElse(null);
		if (menu == null) {
			return "error: menu not found";
		}
		Map<Menu, Integer> orders = getSessionOrders(session);
		if (orders == null) {
			orders = new HashMap<>();
		}
		orders.remove(menu);
		session.setAttribute("orders", orders);
		return "success";
	}

	@RequestMapping(value = "/order/kiosk", method = RequestMethod.GET)
	public String getKiosk() {
		return "/order/orderKiosk.html";
	}

	@RequestMapping(value = "/order/kiosk/menu", method = RequestMethod.GET)
	public String getKioskMenu(@ModelAttribute DeviceRequest deviceRequest, @RequestParam Map<String, Object> params, HttpSession session, Model model) {
		model.addAttribute("deviceRequest", deviceRequest);
		List<Menu> menus = menuService.getAllMenus();
		List<Category> categories = categoryService.getAllCategories();
		model.addAttribute("menus", menus);
		model.addAttribute("categories", categories);
		return "/order/orderKioskMenu.html";
	}

	@RequestMapping(value = "/order/kiosk/pay", method = RequestMethod.GET)
	public String getKioskPayment() {
		return "redirect:/order/kiosk/menu";
	}

	@RequestMapping(value = "/order/kiosk/pay", method = RequestMethod.POST)
	public String postKioskPayment(Model model, DeviceRequest deviceRequest, HttpSession session) {
		Map<Menu, Integer> orders = getSessionOrders(session);
		if (orders == null || orders.isEmpty()) {
			return "redirect:/order/kiosk/menu"; // 주문이 없는 경우
		}

		long totalPrice = calculateTotalPrice(orders);
		model.addAttribute("orderedItems", orders);
		model.addAttribute("totalPrice", totalPrice);

		return "order/orderKioskPay.html";
	}

	@ResponseBody
	@RequestMapping("/order/totalPrice")
	public long getTotalPrice(HttpSession session) {
		Map<Menu, Integer> orders = getSessionOrders(session);
		if (orders == null || orders.isEmpty()) {
			return 0;
		}

		return calculateTotalPrice(orders);
	}

	@RequestMapping(value = "/order/kiosk/payment", method = RequestMethod.POST)
	public String postKioskPayment(Model model, HttpSession session, @RequestParam Map<String, Object> params) {
		Map<Menu, Integer> orders = getSessionOrders(session);
		if (orders == null || orders.isEmpty()) {
			return "redirect:/order/kiosk/menu"; // 주문이 없는 경우
		}

		int salePrice = 0;

		LocalDateTime localDate = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formattedDateTime = localDate.format(formatter);
		OrderNum orderNum = orderNumRepository.save(new OrderNum());

		String couponNum = (String) params.get("couponNum");

		Coupon coupon = couponService.findByCouponNum(couponNum);

		for (Map.Entry<Menu, Integer> entry : orders.entrySet()) {
			Menu menu = entry.getKey();

			int quantity = entry.getValue();

			SalesRequest sales = new SalesRequest();

			salePrice = menu.getPrice();

			if (couponNum != null && !couponNum.isEmpty()) {
				if (coupon != null) {
					if (coupon.getProcess() == 0) {
						salePrice *= 0.9;  // 10% 할인 적용
					}
				}
			}

			sales.setDate(LocalDateTime.parse(formattedDateTime, formatter));
			sales.setCategory(menu.getCategory());
			sales.setMenu(menu.getId());
			sales.setPrice(salePrice);
			sales.setQuantity(quantity);
			sales.setDevice(2);
			sales.setDeviceNum(1);
			sales.setOrderNum(orderNum.getOrderNum());
			sales.setProcess(0);

			salesService.save(sales);

			menu.setStock(menu.getStock() - quantity);

			menuRepository.save(menu);

		}

		if (coupon != null) {
			if (coupon.getProcess() == 0) {
				coupon.setProcess(1);  // 쿠폰 사용 처리
				couponService.save(coupon);
			}
		}

		orders.clear();

		if (params.get("phone") != null) {
			String phoneNum = (String) params.get("phone");
			Account account = accountService.getAccountByPhone(phoneNum);
			account.setMsPoint((int) (account.getMsPoint() + (salePrice * 0.01)));
			accountService.save(account);
		}

		// WebSocket 메시지 전송
        orderWebSocketHandler.sendMessageToAllSessions(new TextMessage("Order"));

        return "redirect:/order/kiosk/success";
	}

	@GetMapping("/connected-sessions")
	public String getConnectedSessions(Model model) {
		List<WebSocketSession> sessions = orderWebSocketHandler.getSessions();
		List<String> sessionIds = sessions.stream()
				.map(WebSocketSession::getId)
				.collect(Collectors.toList());
		model.addAttribute("sessionIds", sessionIds);
		orderWebSocketHandler.sendMessageToAllSessions(new TextMessage("Pong"));
		return "connected-sessions.html";
	}

	@RequestMapping(value = "/order/kiosk/success", method = RequestMethod.GET)
	public String getSuccess() {
		return "/order/orderKioskSuccess.html";
	}
}
