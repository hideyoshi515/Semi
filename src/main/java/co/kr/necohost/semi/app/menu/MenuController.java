package co.kr.necohost.semi.app.menu;


import co.kr.necohost.semi.domain.model.dto.MenuRequest;
import co.kr.necohost.semi.domain.model.dto.MenuWithCategoryRequest;
import co.kr.necohost.semi.domain.model.entity.Category;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.service.CategoryService;
import co.kr.necohost.semi.domain.service.MenuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class MenuController {
    private final MenuService menuService;
    private final CategoryService categoryService;

    public MenuController(MenuService menuService, CategoryService categoryService) {
        this.menuService = menuService;
        this.categoryService = categoryService;
    }

    @GetMapping("/MenuIndex")
    public String MenuIndex(Model model) {
        return "/menu/menuIndex.html";
    }

    @GetMapping("/menuList")
    public String MenuList(Model model) {
        List<Menu> menus = menuService.getAllMenus();
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("menus", menus);
        model.addAttribute("categories", menus);
        return "/menu/menuList.html";
    }

    @GetMapping("/menuCreate")
    public String MenuCreate(Model model) {
        MenuRequest menuRequest = new MenuRequest();
        model.addAttribute("menuRequest", menuRequest);
        return "menu/menuCreate.html";
    }

    @PostMapping("/menuCreate")
    public String MenuCreate(Model model, @ModelAttribute("menuRequest") MenuRequest menuRequest) {
        menuService.saveMenuWithImage(menuRequest, menuRequest.getImage());
        return "redirect:/MenuIndex";
    }

    @GetMapping("/menuUpdate")
    public String MenuUpdate(Model model, @RequestParam Map<String, Object> params) {
        Menu menu = menuService.getMenuById(Long.parseLong(params.get("id").toString()));
        model.addAttribute("menu", menu);
        MenuRequest menuRequest = new MenuRequest();
        model.addAttribute("menuRequest", menuRequest);
        return "/menu/menuUpdate.html";
    }

    @PostMapping("/menuUpdate")
    public String MenuUpdate(Model model, @ModelAttribute("menuRequest") MenuRequest menuRequest) {
        menuService.saveMenuWithImage(menuRequest, menuRequest.getImage());
        return "redirect:/menuList";
    }

    @GetMapping("/menuDetail")
    @ResponseBody
    public MenuWithCategoryRequest detailMenu(Model model, @RequestParam Map<String, Object> params) {
        Menu menu = menuService.getMenuById(Long.parseLong(params.get("id").toString()));
        Category category = categoryService.findById((int) menu.getCategory());
        MenuWithCategoryRequest MWCR = new MenuWithCategoryRequest();
        MWCR.setCategory(category);
        MWCR.setMenu(menu);
        return MWCR;
    }

    @GetMapping("/menuDelete")
    public String menuDelete(Model model, @RequestParam Map<String, Object> params) {
        menuService.deleteMenuById(Long.parseLong(params.get("id").toString()));
        return "redirect:/menuList";
    }

    @GetMapping("/categoryManagement")
    public String CategoryManagement(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "/menu/categoryManagement.html";
    }
}
