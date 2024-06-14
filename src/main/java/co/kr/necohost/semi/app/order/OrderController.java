package co.kr.necohost.semi.app.order;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class OrderController {
	private final CategoryService categoryService;
	private final MenuService menuService;
	private final OrderService orderService;

	private final DiscordBotService discordBotService;

	public OrderController(CategoryService categoryService, MenuService menuService, OrderService orderService, DiscordBotService discordBotService) {
		this.categoryService = categoryService;
		this.menuService = menuService;
		this.orderService = orderService;
		this.discordBotService = discordBotService;
	}

	@RequestMapping(value = "/orderList", method = RequestMethod.GET)
	public String getProductOredr(Model model) {
		List<Object[]> orderList = orderService.findSalesByProcess(0);

		List<Sales> sales = orderList.stream().map(objects -> (Sales) objects[0]).collect(Collectors.toList());

		List<String> menuNames = orderList.stream().map(objects -> (String) objects[1]).collect(Collectors.toList());

		List<String> categoryNames = orderList.stream().map(objects -> (String) objects[2]).collect(Collectors.toList());

		List<Integer> menuStocks = orderList.stream().map(objects -> (Integer) objects[3]).collect(Collectors.toList());

		model.addAttribute("menuStock", menuStocks);
		model.addAttribute("orderList", sales);
		model.addAttribute("menuName", menuNames);
		model.addAttribute("categoryName", categoryNames);
		model.addAttribute("orderRequest", new SalesRequest());

		return "order/orderList.html";
	}

	@RequestMapping(value = "/orderDetail", method = RequestMethod.GET)
	public String getOrderDetail(Model model, @RequestParam Map<String, Object> params) {
		long ordertID = Long.parseLong(params.get("orderID").toString());

		List<Object[]> productOrder = orderService.findSalesById(ordertID);

		List<Sales> sales = productOrder.stream().map(objects -> (Sales) objects[0]).collect(Collectors.toList());

		List<String> menuNames = productOrder.stream().map(objects -> (String) objects[1]).collect(Collectors.toList());

		List<String> categoryNames = productOrder.stream().map(objects -> (String) objects[2]).collect(Collectors.toList());

		List<Integer> menuStocks = productOrder.stream().map(objects -> (Integer) objects[3]).collect(Collectors.toList());

		model.addAttribute("menuStock", menuStocks);
		model.addAttribute("orderList", sales);
		model.addAttribute("menuName", menuNames);
		model.addAttribute("categoryName", categoryNames);
		model.addAttribute("orderRequest", new SalesRequest());

		return "order/orderDetail.html";
	}

	@RequestMapping(value = "/orderApproval", method = RequestMethod.GET)
	public String getOrderApproval(Model model, @RequestParam Map<String, Object> params) {
		long orderID = Long.parseLong(params.get("orderID").toString());
		int orderQuantity = Integer.parseInt(params.get("orderQuantity").toString());
		long menuID = Long.parseLong(params.get("menuID").toString());

		NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
		List<Object[]> orderDetail = orderService.findByIdAndShowDeviceName(orderID);

		Object[] order = orderDetail.get(0);
		Sales sales = (Sales) order[0];

		String formattedPrice = numberFormat.format(sales.getPrice() * sales.getQuantity());

		String message = "주문 번호 " + sales.getOrderNum() + "이/가 승인되었습니다.\n" +
				"===================================\n" +
				"            주문번호 " + sales.getOrderNum() + "\n" +
				"===================================\n";
		if (sales.getDevice() == 3) {
			message += "주문 기기 " + order[2] + "\t수량\t\t" + "가격\n" +
					"테이블 번호 " + sales.getDeviceNum() + "\n";
		} else {
			message += "주문 기기 " + order[2] + "\t수량\t\t" + "가격\n";
		}

		message += "-----------------------------------\n" +
				"주문 메뉴\n" +
				order[1] + "\t\t\t" + sales.getQuantity() + "개\t\t" + formattedPrice + "원\n" +
				"-----------------------------------\n" +
				"주문 시간 " + sales.getDate() + "\n" +
				"총 가격 " + formattedPrice + "원\n" +
				"===================================";

		orderService.approveOrder(orderID, message);
		orderService.updateOrderApproval(orderID, orderQuantity, menuID);

		System.out.println("Order " + sales.getOrderNum() + ": " + sales + ", " + Arrays.toString(Arrays.copyOfRange(order, 1, order.length)));

		return ("redirect:/orderList");
	}

	@RequestMapping(value = "/orderDenial", method = RequestMethod.GET)
	public String getOrderDenial(Model model, @RequestParam Map<String, Object> params) {
		long orderID = Long.parseLong(params.get("orderID").toString());

		String message = "주문 번호 " + orderID + "가 취소되었습니다.";

		orderService.approveOrder(orderID, message);
		orderService.updateDenialByProcess(orderID);

		return ("redirect:/orderList");
	}

	@RequestMapping(value = "/orderTable", method = RequestMethod.GET)
	public String getOrderTable(Model model, @RequestParam Map<String, Object> params) {
		List<Object[]> orderDetail = orderService.findSalesByProcessAndDevice(0);

		NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

		Set<String> tableIDList = new LinkedHashSet<String>();

		Map<String, List<Map<String, Object>>> tableOrders = new HashMap<>();

		for (int i = 0; i < orderDetail.size(); i++) {
			Object[] order = orderDetail.get(i);
			Sales sales = (Sales) order[0];
			System.out.println("Order " + i + ": " + sales + ", " + Arrays.toString(Arrays.copyOfRange(order, 1, order.length)));
		}

		for (int i = 0; i < 8; i++) {
			tableIDList.add("TB" + (i + 1));
		}

		Map<String, Object> orderDetails;

		for (int i = 0; i < orderDetail.size(); i++) {
			Object[] order = orderDetail.get(i);
			orderDetails = new HashMap<>();

			Sales sales = (Sales) order[0];

			orderDetails.put("id", sales.getId());
			orderDetails.put("category", sales.getCategory());
			orderDetails.put("date", sales.getDate());
			orderDetails.put("device", sales.getDevice());
			orderDetails.put("deviceNum", sales.getDeviceNum());
			orderDetails.put("menu", sales.getMenu());
			orderDetails.put("orderNum", sales.getOrderNum());
			orderDetails.put("price", sales.getPrice());
			orderDetails.put("process", sales.getProcess());
			orderDetails.put("quantity", sales.getQuantity());
			orderDetails.put("totalPrice", numberFormat.format(sales.getPrice() * sales.getQuantity()));
			orderDetails.put("menuName", order[1]);
			orderDetails.put("categoryName", order[2]);
			orderDetails.put("stock", order[3]);

			String tableKey = "TB" + sales.getDeviceNum();
			tableOrders.computeIfAbsent(tableKey, k -> new ArrayList<>()).add(orderDetails);

			tableIDList.add(tableKey);
		}

		model.addAttribute("tableIDList", tableIDList);
		model.addAttribute("tableOrders", tableOrders);

		model.addAttribute("orderRequest", new SalesRequest());

		return ("order/orderTable.html");
	}

	@RequestMapping(value = "/orderDetailTable", method = RequestMethod.GET)
	public String getOrderDetailTable(Model model, @RequestParam Map<String, Object> params) {
		long ordertID = Long.parseLong(params.get("orderID").toString());

		List<Object[]> productOrder = orderService.findSalesById(ordertID);

		List<Sales> sales = productOrder.stream().map(objects -> (Sales) objects[0]).collect(Collectors.toList());

		List<String> menuNames = productOrder.stream().map(objects -> (String) objects[1]).collect(Collectors.toList());

		List<String> categoryNames = productOrder.stream().map(objects -> (String) objects[2]).collect(Collectors.toList());

		List<Integer> menuStocks = productOrder.stream().map(objects -> (Integer) objects[3]).collect(Collectors.toList());

		model.addAttribute("menuStock", menuStocks);
		model.addAttribute("orderList", sales);
		model.addAttribute("menuName", menuNames);
		model.addAttribute("categoryName", categoryNames);
		model.addAttribute("orderRequest", new SalesRequest());

		return "order/orderDetailTable.html";
	}

	@RequestMapping(value = "/orderApprovalTable", method = RequestMethod.GET)
	public String getOrderApprovalTable(Model model, @RequestParam Map<String, Object> params) {
		long orderID = Long.parseLong(params.get("orderID").toString());
		int orderQuantity = Integer.parseInt(params.get("orderQuantity").toString());
		long menuID = Long.parseLong(params.get("menuID").toString());

		String message = "주문 번호 " + orderID + "가 승인되었습니다.";

		orderService.approveOrder(orderID, message);
		orderService.updateOrderApproval(orderID, orderQuantity, menuID);

		return ("redirect:/orderTable");
	}

	@RequestMapping(value = "/orderDenialTable", method = RequestMethod.GET)
	public String getOrderDenialTable(Model model, @RequestParam Map<String, Object> params) {
		long orderID = Long.parseLong(params.get("orderID").toString());

		String message = "주문 번호 " + orderID + "가 취소되었습니다.";

		orderService.approveOrder(orderID, message);
		orderService.updateDenialByProcess(orderID);

		return ("redirect:/orderTable");
	}
}
