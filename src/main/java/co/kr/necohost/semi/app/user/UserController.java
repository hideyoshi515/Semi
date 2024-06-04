package co.kr.necohost.semi.app.user;

import co.kr.necohost.semi.domain.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController {
    CategoryService categoryService;

    public UserController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("categories",categoryService.getAllCategories());
        return "user/home.html";
    }
}
