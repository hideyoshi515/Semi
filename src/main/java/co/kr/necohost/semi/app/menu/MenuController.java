package co.kr.necohost.semi.app.menu;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MenuController {
    @GetMapping("/MenuIndex")
    public String MenuIndex(Model model) {
        return "/menu/menuIndex.html";
    }
}
