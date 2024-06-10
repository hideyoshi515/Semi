package co.kr.necohost.semi.app.device;

import co.kr.necohost.semi.domain.model.dto.AccountRequest;
import co.kr.necohost.semi.domain.model.dto.DeviceRequest;
import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Account;
import co.kr.necohost.semi.domain.model.entity.Category;
import co.kr.necohost.semi.domain.model.entity.Device;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public DeviceController(MenuService menuService, DeviceService deviceService, SalesService salesService, MenuRepository menuRepository, AccountService accountService, CategoryService categoryService) {
        this.menuService = menuService;
        this.deviceService = deviceService;
        this.salesService = salesService;
        this.menuRepository = menuRepository;
        this.accountService = accountService;
        this.categoryService = categoryService;
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
            totalPrice += menu.getPrice() * quantity;
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


}
