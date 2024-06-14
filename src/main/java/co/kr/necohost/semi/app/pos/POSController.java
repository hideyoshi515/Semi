package co.kr.necohost.semi.app.pos;

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

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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

    public POSController(MenuService menuService, CategoryService categoryService, SalesService salesService, MenuRepository menuRepository, OrderNumRepository orderNumRepository, OrderRepository orderRepository, OrderService orderService, OrderWebSocketHandler orderWebSocketHandler) {
        this.menuService = menuService;
        this.categoryService = categoryService;
        this.salesService = salesService;
        this.menuRepository = menuRepository;
        this.orderNumRepository = orderNumRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.orderWebSocketHandler = orderWebSocketHandler;
    }

    @RequestMapping(value = "/pos", method = RequestMethod.GET)
    public String getPOS(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("session", session);
        model.addAttribute("categories", categories);
        return "pos/index.html";
    }

    @RequestMapping(value = "/pos/orderList/getOrderNum",method = RequestMethod.GET)
    @ResponseBody
    public List<Integer> getOrderNumByDate(@RequestParam Map<String, Object> params){
        LocalDate inputDate = params.get("date") == null ? LocalDate.now() : LocalDate.parse(params.get("date").toString());
        List<Integer> orderNums = orderRepository.findDistinctOrderNumByDateAndProcess(0, inputDate);
        return orderNums;
    }

    @RequestMapping(value = "/pos/orderList/getOrder",method = RequestMethod.GET)
    @ResponseBody
    public List<Sales> getOrders(@RequestParam Map<String, Object> params){
        int orderNum = Integer.parseInt(params.get("orderNum").toString());
        LocalDate inputDate = params.get("date") == null ? LocalDate.now() : LocalDate.parse(params.get("date").toString());
        List<Sales> orders = orderRepository.findSalesByOrderNumAndDate(orderNum, inputDate);
        return orders;
    }

    @RequestMapping(value = "/pos/orderList", method = RequestMethod.GET)
    public String getPOSOrderList(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
        List<String> orderDate = orderRepository.findDistinctDateByProcess(0);
        model.addAttribute("session", session);
        model.addAttribute("orderDate", orderDate);
        return "pos/orderList.html";
    }

    @RequestMapping(value = "/pos/orderList/confirm",method = RequestMethod.POST)
    @ResponseBody
    public String confirmOrder(@RequestParam Map<String, Object> params) {
        int pk = params.get("pk") == null ? 0 : Integer.parseInt(params.get("pk").toString());
        int menuId = params.get("menuId") == null ? 0 : Integer.parseInt(params.get("menuId").toString());
        int orderQuantity = params.get("quantity") == null ? 0 : Integer.parseInt(params.get("quantity").toString());
        orderRepository.updateSalesProcess(pk);
        orderRepository.updateMenuStock(menuId, orderQuantity);
        return "success";
    }

    @RequestMapping(value = "/pos/getMenu", method = RequestMethod.GET)
    @ResponseBody
    public List<Menu> getMenusByCategory(@RequestParam Map<String, Object> params, HttpSession session){
        List<Menu> menus = menuService.getMenuByCategory(Integer.parseInt(params.get("category").toString()));
        return menus;
    }

    @RequestMapping(value = "/pos/orderList/getOrderDates", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getOrderDates() {
        List<String> dates = orderRepository.findDistinctDateByProcess(0);
        return dates;
    }

    @RequestMapping(value = "/pos/order", method = RequestMethod.POST)
    @ResponseBody
    public String postPOSOrder(@RequestBody List<POSOrder> orderItems, HttpSession session) throws IOException {
        LocalDateTime localDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = localDate.format(formatter);
        OrderNum orderNum = orderNumRepository.save(new OrderNum());

        for (POSOrder order : orderItems) {
            Menu menu = menuService.getMenuById(order.getId());
            int quantity = order.getQuantity();
            SalesRequest sales = new SalesRequest();
            sales.setDate(LocalDateTime.parse(formattedDateTime, formatter));
            sales.setCategory(menu.getCategory());
            sales.setMenu(menu.getId());
            sales.setPrice(menu.getPrice());
            sales.setQuantity(quantity);
            sales.setDevice(2);
            sales.setDeviceNum(2);
            sales.setOrderNum(orderNum.getOrderNum());
            sales.setProcess(1);
            salesService.save(sales);
            menu.setStock(menu.getStock() - quantity);
            menuRepository.save(menu);
        }

        // WebSocket을 통해 클라이언트에 알림 전송
        String message = "새로운 주문이 들어왔습니다";
        orderWebSocketHandler.sendMessageToAll(message);

        return "注文が成功しました";
    }
}
