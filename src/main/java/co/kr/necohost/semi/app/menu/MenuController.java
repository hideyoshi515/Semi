package co.kr.necohost.semi.app.menu;


import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.service.MenuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MenuController {
    private final MenuService menuService;
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/MenuIndex")
    public String MenuIndex(Model model) {
        return "/menu/menuIndex.html";
    }

    @GetMapping("/MenuList")
    public String MenuList(Model model) {
        List<Menu> menus = menuService.getAllMenus();
        model.addAttribute("menus", menus);
        return "/menu/menuList.html";
    }
}
