package co.kr.necohost.semi.app;

import co.kr.necohost.semi.domain.model.dto.OrderRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderController {
    @GetMapping("/order")
    public String order(Model model) {
        model.addAttribute("orderRequest", new OrderRequest());
        return "order/orderInput/html";
    }
}
