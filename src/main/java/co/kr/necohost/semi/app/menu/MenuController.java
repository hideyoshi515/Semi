package co.kr.necohost.semi.app.menu;


import co.kr.necohost.semi.domain.service.MenuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MenuController {
    private MenuService menuService;
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/MenuIndex")
    public String MenuIndex(Model model) {
        return "/menu/menuIndex.html";
    }

    @GetMapping("/MenuList")
    public String MenuList(Model model) {
        menuService.getAllMenus();
        return "/menu/menuList.html";
    }
}
