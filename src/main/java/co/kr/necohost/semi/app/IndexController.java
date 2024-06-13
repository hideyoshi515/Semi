package co.kr.necohost.semi.app;

import co.kr.necohost.semi.domain.service.SalesService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class IndexController {

    private final SalesService salesService;

    public IndexController(SalesService salesService) {
        this.salesService = salesService;
    }

    @GetMapping("/index")
    public String getIndex(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
        model.addAttribute("session", session);
        return "redirect:/";
    }

    @GetMapping("/admin")
    public String getAdmin(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
        Map<String, Long> todaySales = salesService.findSalesByToday();
        model.addAttribute("todaySales", todaySales);
        model.addAttribute("session", session);
        model.addAttribute("lang", lang);
        return "index.html";
    }
}
