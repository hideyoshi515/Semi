package co.kr.necohost.semi.app.order;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.service.CategoryService;
import co.kr.necohost.semi.domain.service.MenuService;
import co.kr.necohost.semi.domain.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class OrderController {
    private CategoryService categoryService;
    private MenuService menuService;
    private OrderService orderService;

    public OrderController(CategoryService categoryService, MenuService menuService, OrderService orderService) {
        this.categoryService = categoryService;
        this.menuService = menuService;
        this.orderService = orderService;
    }

    @GetMapping("orderList")
    public String getProductOredr(Model model) {
        List<Object[]> orderList = orderService.findSalesByProcess(0);

        List<Sales> sales = orderList.stream()
                .map(objects -> (Sales) objects[0])
                .collect(Collectors.toList());

        List<String> menuNames = orderList.stream()
                .map(objects -> (String) objects[1])
                .collect(Collectors.toList());

        List<String> categoryNames = orderList.stream()
                .map(objects -> (String) objects[2])
                .collect(Collectors.toList());

        List<Integer> menuStocks = orderList.stream()
                .map(objects -> (Integer) objects[3])
                .collect(Collectors.toList());

        model.addAttribute("menuStock", menuStocks);
        model.addAttribute("orderList", sales);
        model.addAttribute("menuName", menuNames);
        model.addAttribute("categoryName", categoryNames);
        model.addAttribute("orderRequest", new SalesRequest());

        return "/order/orderList.html";
    }

    @GetMapping("orderDetail")
    public String getOrderDetail(Model model, @RequestParam Map<String, Object> params) {
        long ordertID = Long.parseLong(params.get("orderID").toString());

        List<Object[]> productOrder = orderService.findSalesById(ordertID);

        List<Sales> sales = productOrder.stream()
                .map(objects -> (Sales) objects[0])
                .collect(Collectors.toList());

        List<String> menuNames = productOrder.stream()
                .map(objects -> (String) objects[1])
                .collect(Collectors.toList());

        List<String> categoryNames = productOrder.stream()
                .map(objects -> (String) objects[2])
                .collect(Collectors.toList());

        List<Integer> menuStocks = productOrder.stream()
                .map(objects -> (Integer) objects[3])
                .collect(Collectors.toList());

        model.addAttribute("menuStock", menuStocks);
        model.addAttribute("orderList", sales);
        model.addAttribute("menuName", menuNames);
        model.addAttribute("categoryName", categoryNames);
        model.addAttribute("orderRequest", new SalesRequest());

        return "/order/orderDetail.html";
    }

    @GetMapping("orderApproval")
    public String getOrderApproval(Model model, @RequestParam Map<String, Object> params) {
        long orderID = Long.parseLong(params.get("orderID").toString());
        int orderQuantity = Integer.parseInt(params.get("orderQuantity").toString());
        long menuID = Long.parseLong(params.get("menuID").toString());

        orderService.updateOrderApproval(orderID, orderQuantity, menuID);

        return ("redirect:/orderList");
    }

    @GetMapping("orderDenial")
    public String getOrderDenial(Model model, @RequestParam Map<String, Object> params) {
        long orderID = Long.parseLong(params.get("orderID").toString());

        orderService.updateDenialByProcess(orderID);

        return ("redirect:/orderList");
    }
}
