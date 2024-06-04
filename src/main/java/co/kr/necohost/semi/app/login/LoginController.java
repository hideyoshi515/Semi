package co.kr.necohost.semi.app.login;

import co.kr.necohost.semi.domain.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class LoginController {
    CategoryService categoryService;

    public LoginController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register(Model model, HttpSession session, @RequestParam Map<String, Object> params) {
        model.addAttribute("session", session);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "login/register.html";
    }
}
