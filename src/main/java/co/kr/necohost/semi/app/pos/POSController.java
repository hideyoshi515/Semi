package co.kr.necohost.semi.app.pos;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.*;
import co.kr.necohost.semi.domain.repository.CouponRepository;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.repository.OrderNumRepository;
import co.kr.necohost.semi.domain.repository.OrderRepository;
import co.kr.necohost.semi.domain.service.*;
import co.kr.necohost.semi.websocket.OrderWebSocketHandler;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class POSController {
	private final MenuService menuService;
	private final CategoryService categoryService;
	private final SalesService salesService;
	private final MenuRepository menuRepository;
	private final OrderNumRepository orderNumRepository;
	private final OrderRepository orderRepository;
	private final OrderService orderService;
	private final OrderWebSocketHandler orderWebSocketHandler;
	private final CouponService couponService;
	private final DiscordBotService discordBotService;
	private final StaffService staffService;
	private final TimeCardService timeCardService;

	public POSController(MenuService menuService, CategoryService categoryService, SalesService salesService, MenuRepository menuRepository, OrderNumRepository orderNumRepository, OrderRepository orderRepository, OrderService orderService, OrderWebSocketHandler orderWebSocketHandler, CouponService couponService, DiscordBotService discordBotService, StaffService staffService, TimeCardService timeCardService) {
		this.menuService = menuService;
		this.categoryService = categoryService;
		this.salesService = salesService;
		this.menuRepository = menuRepository;
		this.orderNumRepository = orderNumRepository;
		this.orderRepository = orderRepository;
		this.orderService = orderService;
		this.orderWebSocketHandler = orderWebSocketHandler;
		this.couponService = couponService;
		this.discordBotService = discordBotService;
		this.staffService = staffService;
		this.timeCardService = timeCardService;
	}

	// POS 페이지のGETリクエストを処理するメソッド
	@RequestMapping(value = "/pos", method = RequestMethod.GET)
	public String getPOS(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
		List<Category> categories = categoryService.getAllCategories();

		model.addAttribute("session", session);
		model.addAttribute("categories", categories);

		return "pos/index.html";
	}

	// 特定の日付の注文番号リストを取得するメソッド
	@RequestMapping(value = "/pos/orderList/getOrderNum", method = RequestMethod.GET)
	@ResponseBody
	public List<Integer> getOrderNumByDate(@RequestParam Map<String, Object> params) {
		LocalDate inputDate = params.get("date") == null ? LocalDate.now() : LocalDate.parse(params.get("date").toString());
		List<Integer> orderNums = orderRepository.findDistinctOrderNumByDateAndProcess(0, inputDate);
		return orderNums;
	}

	// 特定の注文番号の注文リストを取得するメソッド
	@RequestMapping(value = "/pos/orderList/getOrder", method = RequestMethod.GET)
	@ResponseBody
	public List<Sales> getOrders(@RequestParam Map<String, Object> params) {
		int orderNum = Integer.parseInt(params.get("orderNum").toString());
		LocalDate inputDate = params.get("date") == null ? LocalDate.now() : LocalDate.parse(params.get("date").toString());
		List<Sales> orders = orderRepository.findSalesByOrderNumAndDate(orderNum, inputDate);
		return orders;
	}

	// POS注文リストページのGETリクエストを処理するメソッド
	@RequestMapping(value = "/pos/orderList", method = RequestMethod.GET)
	public String getPOSOrderList(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
		List<String> orderDate = orderRepository.findDistinctDateByProcess(0);
		model.addAttribute("session", session);
		model.addAttribute("orderDate", orderDate);
		return "pos/orderList.html";
	}

	// 注文を確認するメソッド
	@RequestMapping(value = "/pos/orderList/confirmOrder", method = RequestMethod.POST)
	@ResponseBody
	public String confirmOrder(@RequestParam Map<String, Object> params) {
		int pk = params.get("pk") == null ? 0 : Integer.parseInt(params.get("pk").toString());
		int menuId = params.get("menuId") == null ? 0 : Integer.parseInt(params.get("menuId").toString());
		int orderQuantity = params.get("quantity") == null ? 0 : Integer.parseInt(params.get("quantity").toString());

		NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
		List<Object[]> orderDetail = orderService.findByIdAndShowDeviceName(pk);

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
				order[1] + sales.getQuantity() + "개\t\t" + formattedPrice + "원\n" +
				"-----------------------------------\n" +
				"주문 시간 " + sales.getDate() + "\n" +
				"총 가격 " + formattedPrice + "원\n" +
				"===================================";

		orderService.approveOrder(pk, message);
		orderRepository.updateSalesProcess(pk);
		orderRepository.updateMenuStock(menuId, orderQuantity);

		return "success";
	}

	// 特定のカテゴリのメニューリストを取得するメソッド
	@RequestMapping(value = "/pos/getMenu", method = RequestMethod.GET)
	@ResponseBody
	public List<Menu> getMenusByCategory(@RequestParam Map<String, Object> params, HttpSession session) {
		List<Menu> menus = menuService.getMenuByCategory(Integer.parseInt(params.get("category").toString()));
		return menus;
	}

	// 注文日リストを取得するメソッド
	@RequestMapping(value = "/pos/orderList/getOrderDates", method = RequestMethod.GET)
	@ResponseBody
	public List<String> getOrderDates() {
		List<String> dates = orderRepository.findDistinctDateByProcess(0);
		return dates;
	}

	// POS注文を処理するメソッド
	@RequestMapping(value = "/pos/order", method = RequestMethod.POST)
	@ResponseBody
	public String postPOSOrder(@RequestBody List<POSOrder> orderItems, HttpSession session) throws IOException {
		LocalDateTime localDate = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formattedDateTime = localDate.format(formatter);
		OrderNum orderNum = orderNumRepository.save(new OrderNum());
		NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

		String message = "주문 번호 " + orderNum.getOrderNum() + "이/가 승인되었습니다.\n" +
				"===============================================\n" +
				"                   주문번호 " + orderNum.getOrderNum() + "\n" +
				"===============================================\n" +
				String.format("%-21s %7s %11s\n", "주문 기기 POS", "수량", "가격") +
				"-----------------------------------------------\n";

		LocalDateTime localDateTime = LocalDateTime.now();
		int sum = 0;
		String formatSum = "";

		for (POSOrder order : orderItems) {
			SalesRequest sales = new SalesRequest();

			Menu menu = menuService.getMenuById(order.getId());

			String menuName = menu.getName();
			int quantity = order.getQuantity();
			int totalPrice = menu.getPrice() * order.getQuantity();
			String formattedPrice = numberFormat.format(menu.getPrice() * order.getQuantity());
			localDateTime = LocalDateTime.parse(formattedDateTime, formatter);
			sum += totalPrice;
			formatSum = numberFormat.format(sum);

			message += String.format("%-24s %4d개 %10s원\n", menuName, quantity, formattedPrice);

			sales.setOrderNum(orderNum.getOrderNum());
			sales.setDate(localDateTime);
			sales.setCategory(menu.getCategory());
			sales.setMenu(menu.getId());
			sales.setPrice(menu.getPrice());
			sales.setQuantity(quantity);
			sales.setDevice(2);
			sales.setDeviceNum(2);
			sales.setProcess(1);

			System.out.println(menuName + " " + order.getQuantity() + "개" + formattedPrice + " " + formatSum + "원");

			salesService.save(sales);

			menu.setStock(menu.getStock() - quantity);

			menuRepository.save(menu);
		}

		message += "-----------------------------------------------\n" +
				String.format("주문 시간 %38s\n", localDateTime.toString()) +
				String.format("총 가격 %38s원\n", formatSum) +
				"===============================================";

		discordBotService.sendOrderNotification(message);

		return "注文が成功しました";
	}

	@RequestMapping(value = "/pos/staff",method = RequestMethod.GET)
	public String getStaff(Model model, HttpSession session){
		List<Staff> staffList = staffService.getAllStaff();
		model.addAttribute("staffList", staffList);
		return "pos/staff.html";
	}

	@RequestMapping(value = "/pos/staffTimeCard", method = RequestMethod.GET)
	@ResponseBody
	public List<TimeCard> getStaffTimecard(Model model, HttpSession session, @RequestParam Map<String, Object> params){
		Staff staff = staffService.getStaff(Long.valueOf(params.get("staffId").toString()));
		List<TimeCard> timeCards = timeCardService.getTimeCardByStaff(staff);
		return timeCards;
	}

	@RequestMapping(value = "/pos/staffTimeCheck", method = RequestMethod.GET)
	@ResponseBody
	public TimeCard getStaffTimeCheck(Model model, HttpSession session, @RequestParam Map<String, Object> params){
		Staff staff = staffService.getStaff(Long.valueOf(params.get("staffId").toString()));
		TimeCard timeCard = timeCardService.getTimeCardByStaffAndStart(staff, params.get("date").toString());
		return timeCard;
	}

	// クーポンを作成するメソッド
	@RequestMapping(value = "/pos/makeCoupon", method = RequestMethod.POST)
	@ResponseBody
	public String makeCoupon() {
		String coupon = generateUniqueCoupon();

		couponService.saveCoupon(coupon);
		couponService.deleteOldCoupons();

		discordBotService.sendOrderNotification("クーポンが発行されました : " + coupon);

		return coupon;
	}

	//　クーポンの重複チェック
	private String generateUniqueCoupon() {
		String coupon;

		do {
			coupon = generateCouponCode();
		} while (couponService.isCouponExists(coupon));

		return coupon;
	}

	// クーポンの発行
	private String generateCouponCode() {
		StringBuilder couponCode = new StringBuilder();
		Random random = new Random();

		for (int i = 0; i < 16; i++) {
			couponCode.append(random.nextInt(10)); // 0-9からの数字を追加
			if ((i + 1) % 4 == 0 && i != 15) {
				couponCode.append('-'); // 4桁ごとに '-' を追加
			}
		}

		return couponCode.toString();
	}

	@RequestMapping(value = "/pos/applyCoupon", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> applyCoupon(@RequestBody Map<String, String> request) {
		String couponNum = request.get("couponNum");

		Coupon coupon = couponService.findByCouponNum(couponNum);

		Map<String, Object> response = new HashMap<>();

		if (coupon != null && coupon.getProcess() == 0) {
			response.put("valid", "not-used");
		} else if (coupon != null && coupon.getProcess() == 1) {
			response.put("valid", "used");
		} else {
			response.put("valid", "none");
		}

		return response;
	}
}
