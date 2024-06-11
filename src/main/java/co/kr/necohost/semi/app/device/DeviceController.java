package co.kr.necohost.semi.app.device;

import co.kr.necohost.semi.domain.model.dto.AccountRequest;
import co.kr.necohost.semi.domain.model.dto.DeviceRequest;
import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.*;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.repository.OrderNumRepository;
import co.kr.necohost.semi.domain.repository.OrderRepository;
import co.kr.necohost.semi.domain.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public DeviceController(MenuService menuService, DeviceService deviceService, SalesService salesService, MenuRepository menuRepository, AccountService accountService, CategoryService categoryService, OrderRepository orderRepository, OrderNumRepository orderNumRepository) {
        this.menuService = menuService;
        this.deviceService = deviceService;
        this.salesService = salesService;
        this.menuRepository = menuRepository;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.orderRepository = orderRepository;
        this.orderNumRepository = orderNumRepository;
    }

    private int calculatePoints(long price) {
        return (int) (price * 0.01);
    }

    @RequestMapping(value = "/order", method = RequestMethod.GET)
    public String getOrder(Model model) {
        model.addAttribute("deviceRequest", new DeviceRequest());
        List<Device> device = deviceService.findAll();
        model.addAttribute("devices", device);
        return "order/orderOption.html";
    }

    @RequestMapping(value = "/order", method = RequestMethod.POST)
    public String postOrder(DeviceRequest deviceRequest, Model model, @RequestParam Map<String, Object> params) {
        model.addAttribute("deviceRequest", deviceRequest);
        Map<Long, List<Menu>> categorizedMenus = menuService.getCategorizedMenus();
        model.addAttribute("categorizedMenus", categorizedMenus);
        List<Menu> menus = menuService.getAllMenus();
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("menus", menus);
        model.addAttribute("categories", categories);
        return "order/orderMenuSelect.html";
    }

    @RequestMapping(value = "/orderPayment", method = RequestMethod.POST)
    public String postOrderPayment(HttpSession session, Model model, DeviceRequest deviceRequest) {
        long totalPrice = 0;


        // 세션에서 주문 데이터를 가져옴
        Map<Menu, Integer> orders = (Map<Menu, Integer>) session.getAttribute("orders");
        if (orders == null || orders.isEmpty()) {
            return "order/orderMenuSelect.html"; // 주문이 없는 경우
        }

        // totalPrice 계산
        for (Map.Entry<Menu, Integer> entry : orders.entrySet()) {
            Menu menu = entry.getKey();
            int quantity = entry.getValue();
            totalPrice += (long) menu.getPrice() * quantity;
        }

        model.addAttribute("orderedItems", orders);
        model.addAttribute("totalPrice", totalPrice);

        return "order/orderPaymentSelect.html";
    }

    @RequestMapping(value = "/orderPayment", method = RequestMethod.GET)
    public String getOrderPayment(Model model, DeviceRequest deviceRequest, HttpSession session) {
        long totalPrice = 0;

        Map<Menu, Integer> orders = (Map<Menu, Integer>) session.getAttribute("orders");
        if (orders == null || orders.isEmpty()) {
            return "order/orderMenuSelect.html"; // 주문이 없는 경우
        }


        for (Map.Entry<Menu, Integer> entry : orders.entrySet()) {
            Menu menu = entry.getKey();
            int quantity = entry.getValue();
            totalPrice += (long) menu.getPrice() * quantity;
        }

        model.addAttribute("orderedItems", orders);
        model.addAttribute("totalPrice", totalPrice);

        return "order/orderPaymentSelect.html";
    }

    @RequestMapping(value = "/orderPaymentSelect", method = RequestMethod.POST)
    public String OrderPaymentSelect(@ModelAttribute DeviceRequest deviceRequest, @RequestParam String paymentMethod, Model model, SalesRequest salesRequest, AccountRequest accountRequest) {
        System.out.println("DeviceRequest: " + deviceRequest);
        Map<Long, Integer> quantities = deviceRequest.getQuantities();
        long totalPrice = 0;
        String phone = deviceRequest.getPhoneNum();
        String pass = deviceRequest.getPass();

        OrderNum orderNum = orderNumRepository.save(new OrderNum());

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Long menuId = entry.getKey();
            Integer quantity = entry.getValue();
            Menu menu = menuService.getMenuById(menuId);
            Account account = accountService.getAccountByPhone(phone);

            if (quantity != null && quantity > 0 && quantity <= menu.getStock()) {
                totalPrice += (long) menu.getPrice() * quantity;
                LocalDateTime localDate = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = localDate.format(formatter);

                accountRequest = new AccountRequest();
                salesRequest = new SalesRequest();
                salesRequest.setCategory((int) menu.getCategory());
                salesRequest.setDate(LocalDateTime.parse(formattedDateTime, formatter));
                salesRequest.setMenu((int) menu.getId());
                salesRequest.setQuantity(quantity);
                salesRequest.setDevice((int) deviceRequest.getDevice());
                salesRequest.setDeviceNum((int) deviceRequest.getDeviceNum());
                salesRequest.setOrderNum((int) deviceRequest.getOrderNum());
                salesRequest.setProcess(paymentMethod.equals("CASH") ? 1 : paymentMethod.equals("CARD") ? 2 : 3);
                salesRequest.setOrderNum((int) orderNum.getOrderNum());

                salesService.save(salesRequest);

                menu.setStock(menu.getStock() - quantity);
                menuService.saveMenu(menu);

                int pointsEarned = calculatePoints((long) menu.getPrice() * quantity);
                if (phone.equals(phone)) {
                    account.setMsPoint(account.getMsPoint() + pointsEarned);
                    accountService.save(accountRequest);
                } else {
                    return "order/orderPoint.html";
                }
            }
        }
        model.addAttribute("orderedItems", quantities);
        model.addAttribute("totalPrice", totalPrice);

        switch (paymentMethod) {
            case "CASH":
                return "order/orderPayment.html";
            case "CARD":
                return "order/orderPayment.html";
            case "PAY":
                return "order/orderPayment.html";
            default:
                model.addAttribute("error", "유효하지 않은 결제방법입니다.");
                return "order/orderPaymentSelect.html";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/order/cart", method = RequestMethod.GET)
    public Map<Menu, Integer> getOrderCart(HttpSession session, Model model) {
        Map<Menu, Integer> orders = (Map<Menu, Integer>) session.getAttribute("orders");
        return orders;
    }

    @PostMapping("/addToSession")
    @ResponseBody
    public String addToSession(@RequestBody Map<String, Object> data, HttpSession session) {
        String menuId = (String) data.get("menuId");
        Integer quantity = (Integer) data.get("quantity");

        Menu menu = menuRepository.findById(Long.valueOf(menuId)).orElse(null);
        if (menu == null) {
            return "error: menu not found";
        }

        // 세션에서 현재 주문을 가져옴
        Map<Menu, Integer> orders = (Map<Menu, Integer>) session.getAttribute("orders");
        if (orders == null) {
            orders = new HashMap<>();
        }

        // 기존 주문이 있는지 확인하고, 있으면 수량을 덮어쓰기
        orders.put(menu, quantity);

        // 세션에 업데이트된 주문 저장
        session.setAttribute("orders", orders);

        return "success";
    }

    @GetMapping("/getCartItems")
    @ResponseBody
    public Map<String, Object> getCartItems(HttpSession session) {
        Map<Menu, Integer> orders = (Map<Menu, Integer>) session.getAttribute("orders");
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

        // 세션에서 현재 주문을 가져옴
        Map<Menu, Integer> orders = (Map<Menu, Integer>) session.getAttribute("orders");
        if (orders == null) {
            orders = new HashMap<>();
        }

        // 수량을 업데이트
        if (quantity > 0) {
            orders.put(menu, quantity);
        } else {
            orders.remove(menu);
        }

        // 세션에 업데이트된 주문 저장
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
        Map<Menu, Integer> orders = (Map<Menu, Integer>) session.getAttribute("orders");
        if (orders == null) {
            orders = new HashMap<>();
        }
        orders.remove(menu);
        session.setAttribute("orders", orders);
        return "success";

    }

    @RequestMapping(value = "/order/kiosk", method = RequestMethod.GET)
    public String getKiosk(@ModelAttribute DeviceRequest deviceRequest, @RequestParam Map<String, Object> params, HttpSession session, Model model) {
        model.addAttribute("deviceRequest", deviceRequest);
        Map<Long, List<Menu>> categorizedMenus = menuService.getCategorizedMenus();
        model.addAttribute("categorizedMenus", categorizedMenus);
        List<Menu> menus = menuService.getAllMenus();
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("menus", menus);
        model.addAttribute("categories", categories);
        return "/order/orderKiosk.html";
    }

    @RequestMapping(value = "/order/kiosk/pay", method = RequestMethod.GET)
    public String getKioskPayment() {
        return "redirect:/order/kiosk";
    }

    @RequestMapping(value = "/order/kiosk/pay", method = RequestMethod.POST)
    public String postKioskPayment(Model model, DeviceRequest deviceRequest, HttpSession session) {
        long totalPrice = 0;

        Map<Menu, Integer> orders = (Map<Menu, Integer>) session.getAttribute("orders");
        if (orders == null || orders.isEmpty()) {
            return "redirect:/order/kiosk"; // 주문이 없는 경우
        }


        for (Map.Entry<Menu, Integer> entry : orders.entrySet()) {
            Menu menu = entry.getKey();
            int quantity = entry.getValue();
            totalPrice += (long) menu.getPrice() * quantity;
        }

        model.addAttribute("orderedItems", orders);
        model.addAttribute("totalPrice", totalPrice);

        return "order/orderKioskPay.html";
    }

    @ResponseBody
    @RequestMapping("/order/totalPrice")
    public long getTotalPrice(HttpSession session) {
        long totalPrice = 0;

        Map<Menu, Integer> orders = (Map<Menu, Integer>) session.getAttribute("orders");
        if (orders == null || orders.isEmpty()) {
            return 0;
        }

        for (Map.Entry<Menu, Integer> entry : orders.entrySet()) {
            Menu menu = entry.getKey();
            int quantity = entry.getValue();
            totalPrice += (long) menu.getPrice() * quantity;
        }
        return totalPrice;
    }


    @RequestMapping(value = "/order/kiosk/payment", method = RequestMethod.POST)
    public String postKioskPayment(Model model, HttpSession session, @RequestParam Map<String, Object> params) {
        long totalPrice = 0L;

        Map<Menu, Integer> orders = (Map<Menu, Integer>) session.getAttribute("orders");
        if (orders == null || orders.isEmpty()) {
            return "redirect:/order/kiosk"; // 주문이 없는 경우
        }

        LocalDateTime localDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = localDate.format(formatter);
        OrderNum orderNum = orderNumRepository.save(new OrderNum());

        for (Map.Entry<Menu, Integer> entry : orders.entrySet()) {
            Menu menu = entry.getKey();
            int quantity = entry.getValue();
            totalPrice += (long) menu.getPrice() * quantity;
            SalesRequest sales = new SalesRequest();
            sales.setDate(LocalDateTime.parse(formattedDateTime, formatter));
            sales.setCategory(menu.getCategory());
            sales.setMenu(menu.getId());
            sales.setPrice(menu.getPrice());
            sales.setQuantity(quantity);
            sales.setDevice(2);
            sales.setDeviceNum(1);
            sales.setOrderNum(orderNum.getOrderNum());
            sales.setProcess(1);
            salesService.save(sales);
        }
        if (params.get("phone") != null) {
            String phoneNum = (String) params.get("phone");
            Account account = accountService.getAccountByPhone(phoneNum);
            account.setMsPoint((int) (account.getMsPoint() + (totalPrice * 0.01)));
            accountService.save(account);
        }
        return "redirect:/order/kiosk/success";
    }

    @RequestMapping(value = "/order/kiosk/success",method = RequestMethod.GET)
    public String getSuccess(){
        return "/order/orderKioskSuccess.html";
    }

}
