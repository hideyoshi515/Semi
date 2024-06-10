package co.kr.necohost.semi.app.order;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
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

        String message = "주문 번호 " + orderID + "가 승인되었습니다.";

        orderService.approveOrder(orderID, message);
        orderService.updateOrderApproval(orderID, orderQuantity, menuID);

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
}
