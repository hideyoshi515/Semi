package co.kr.necohost.semi.app.order;

import co.kr.necohost.semi.domain.model.entity.order.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderController {
    @GetMapping("/order")
    public String order(Model model) {
        model.addAttribute("order", new Order());
        return "order/orderInput/html";
    }
}
