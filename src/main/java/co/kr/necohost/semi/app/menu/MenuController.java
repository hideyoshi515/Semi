package co.kr.necohost.semi.app.menu;


import co.kr.necohost.semi.domain.model.dto.MenuRequest;
import co.kr.necohost.semi.domain.model.dto.MenuWithCategoryRequest;
import co.kr.necohost.semi.domain.model.entity.Category;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.service.CategoryService;
import co.kr.necohost.semi.domain.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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

    // 메뉴 인덱스 페이지를 반환
    @GetMapping("/menuIndex")
    public String MenuIndex(Model model) {
        return "/menu/menuIndex.html";
    }

    // 메뉴 리스트를 가져와서 보여줌
    @GetMapping("/menuList")
    public String MenuList(Model model, @RequestParam Map<String, String> params) {
        List<Menu> menus;
        List<Category> categories = categoryService.getAllCategories();
        if (params.get("category") != null) {
            menus = menuService.getMenuByCategory(Integer.parseInt(params.get("category")));
        } else {
            menus = menuService.getAllMenus();
        }
        model.addAttribute("menus", menus);
        model.addAttribute("categories", categories);
        return "/menu/menuList.html";
    }

    // 메뉴 생성 페이지를 반환
    @GetMapping("/menuCreate")
    public String MenuCreate(Model model, HttpServletRequest request, HttpSession session) {
        MenuRequest menuRequest = new MenuRequest();
        String uri = request.getHeader("referer");
        session.setAttribute("prevpage", uri);
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("menuRequest", menuRequest);
        return "menu/menuCreate.html";
    }

    // 메뉴 생성 요청을 처리
    @PostMapping("/menuCreate")
    public String MenuCreate(Model model, @ModelAttribute("menuRequest") MenuRequest menuRequest, HttpSession session) {
        menuService.saveMenuWithImage(menuRequest, menuRequest.getImage());
        return "redirect:" + session.getAttribute("prevpage");
    }

    // 메뉴 업데이트 페이지를 반환
    @GetMapping("/menuUpdate")
    public String MenuUpdate(Model model, @RequestParam Map<String, Object> params) {
        Menu menu = menuService.getMenuById(Long.parseLong(params.get("id").toString()));
        model.addAttribute("menu", menu);
        MenuRequest menuRequest = new MenuRequest();
        model.addAttribute("menuRequest", menuRequest);
        return "/menu/menuUpdate.html";
    }

    // 메뉴 업데이트 요청을 처리
    @PostMapping("/menuUpdate")
    public String MenuUpdate(Model model, @ModelAttribute("menuRequest") MenuRequest menuRequest) {
        menuService.saveMenuWithImage(menuRequest, menuRequest.getImage());
        return "redirect:/menuList";
    }

    // 메뉴 상세 정보를 반환
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

    // 메뉴 삭제 요청을 처리
    @GetMapping("/menuDelete")
    public String menuDelete(Model model, @RequestParam Map<String, Object> params) {
        menuService.deleteMenuById(Long.parseLong(params.get("id").toString()));
        return "redirect:/menuList";
    }

    // 카테고리 관리 페이지를 반환
    @GetMapping("/categoryManagement")
    public String CategoryManagement(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "/menu/categoryManagement.html";
    }
}
