package co.kr.necohost.semi.app.menu;


import co.kr.necohost.semi.domain.model.dto.CategoryRequest;
import co.kr.necohost.semi.domain.model.dto.MenuRequest;
import co.kr.necohost.semi.domain.model.dto.MenuWithCategoryRequest;
import co.kr.necohost.semi.domain.model.entity.Category;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.repository.SalesRepository;
import co.kr.necohost.semi.domain.service.CategoryService;
import co.kr.necohost.semi.domain.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class MenuController {
    private final MenuService menuService;
    private final CategoryService categoryService;
    private final SalesRepository salesRepository;


    public MenuController(MenuService menuService, CategoryService categoryService, SalesRepository salesRepository) {
        this.menuService = menuService;
        this.categoryService = categoryService;
        this.salesRepository = salesRepository;
    }

    // 메뉴 인덱스 페이지를 반환
    @RequestMapping(value = "/menuIndex", method = RequestMethod.GET)
    public String getMenuIndex(Model model) {
        return "/menu/menuIndex.html";
    }

    // 메뉴 리스트를 가져와서 보여줌
    @RequestMapping(value = "/menuList", method = RequestMethod.GET)
    public String getMenuList(Model model, @RequestParam Map<String, String> params, @ModelAttribute("successMessage") String successMessage, @ModelAttribute("errorMessage") String errorMessage) {
        List<Menu> menus;
        List<Category> categories = categoryService.getAllCategories();
        if (params.get("category") != null) {
            menus = menuService.getMenuByCategory(Integer.parseInt(params.get("category")));
        } else {
            menus = menuService.getAllMenus();
        }

        //최근 days일 간의 판매량
        int days = 7;
        Map<Menu, Integer> salesCount = menus.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        m -> salesRepository.getCountByMenuAfterDaysAgo(m.getId(), days)
                ));

        model.addAttribute("salesDays", days);
        model.addAttribute("salesCount", salesCount);
        model.addAttribute("menus", menus);
        model.addAttribute("categories", categories);
        model.addAttribute("successMessage", successMessage);
        model.addAttribute("errorMessage", errorMessage);

        return "/menu/menuList.html";
    }

    // 메뉴 생성 페이지를 반환
    @RequestMapping(value = "/menuCreate", method = RequestMethod.GET)
    public String getMenuCreate(Model model, HttpServletRequest request, HttpSession session) {
        MenuRequest menuRequest = new MenuRequest();
        String uri = request.getHeader("referer");
        session.setAttribute("prevpage", uri);
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("menuRequest", menuRequest);
        return "menu/menuCreate.html";
    }

    // 메뉴 생성 요청을 처리
    @RequestMapping(value = "/menuCreate", method = RequestMethod.POST)
    public String postMenuCreate(Model model, @ModelAttribute("menuRequest") MenuRequest menuRequest, HttpSession session) {
        menuService.saveMenuWithImage(menuRequest, menuRequest.getImage());
        return "redirect:" + session.getAttribute("prevpage");
    }

    // 메뉴 업데이트 페이지를 반환
    @RequestMapping(value = "/menuUpdate", method = RequestMethod.GET)
    public String getMenuUpdate(Model model, @RequestParam Map<String, Object> params) {
        Menu menu = menuService.getMenuById(Long.parseLong(params.get("id").toString()));
        model.addAttribute("menu", menu);
        MenuRequest menuRequest = new MenuRequest();
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("menuRequest", menuRequest);
        return "/menu/menuUpdate.html";
    }

    // 메뉴 업데이트 요청을 처리
    @RequestMapping(value = "/menuUpdate", method = RequestMethod.POST)
    public String postMenuUpdate(Model model, @ModelAttribute("menuRequest") MenuRequest menuRequest, RedirectAttributes redirectAttributes) {
        try {
            menuService.saveMenuWithImage(menuRequest, menuRequest.getImage());
            redirectAttributes.addFlashAttribute("successMessage", "メニューがアップデートされました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "メニューのアップデートが失敗しました。");
        }
        return "redirect:/menuList";
    }

    // 메뉴 삭제 요청을 처리
    @RequestMapping(value = "/menuDelete", method = RequestMethod.GET)
    public String getMenuDelete(Model model, @RequestParam Map<String, Object> params, RedirectAttributes redirectAttributes) {
        try {
            menuService.deleteMenuById(Long.parseLong(params.get("id").toString()));
            redirectAttributes.addFlashAttribute("successMessage", "メニューが削除されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "メニューの削除が失敗しました。");
        }
        return "redirect:/menuList";
    }

    // 메뉴 상세 정보를 반환. JSON 형태로 반환받기 위해 ResponseBody 사용
//    @RequestMapping(value = "/menuDetail", method = RequestMethod.GET)
//    @ResponseBody
//    public MenuWithCategoryRequest getDetailMenu(Model model, @RequestParam Map<String, Object> params) {
//        Menu menu = menuService.getMenuById(Long.parseLong(params.get("id").toString()));
//        Category category = categoryService.findById((int) menu.getCategory());
//        MenuWithCategoryRequest MWCR = new MenuWithCategoryRequest();
//        MWCR.setCategory(category);
//        MWCR.setMenu(menu);
//        return MWCR;
//    }

    // ResponseEntity 이용해보기
    @RequestMapping(value = "/menuDetail", method = RequestMethod.GET)
    public ResponseEntity<MenuWithCategoryRequest> getDetailMenu(Model model, @RequestParam Map<String, Object> params) {
        try {
        Menu menu = menuService.getMenuById(Long.parseLong(params.get("id").toString()));
        Category category = categoryService.findById((int) menu.getCategory());
        MenuWithCategoryRequest MWCR = new MenuWithCategoryRequest();
        MWCR.setCategory(category);
        MWCR.setMenu(menu);
        return ResponseEntity.ok(MWCR);
        } catch (Exception e) {
            MenuWithCategoryRequest MWCR = new MenuWithCategoryRequest();
            return new ResponseEntity<>(MWCR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 입력받은 발주량 값을 메뉴 속성에 추가
    @RequestMapping(value = "/orderStock", method = RequestMethod.POST)
    public ResponseEntity<?> orderStock(@RequestBody Map<String, Object> payload) {
        long id = ((Number) payload.get("id")).longValue();
        int amount = Integer.parseInt(payload.get("amount").toString());
        menuService.addStockOrder(id, amount);
        return ResponseEntity.ok().build();
    }

    // 발주량을 실제 재고에 추가하고 초기화
    @RequestMapping(value = "/updateStockAndOrder", method = RequestMethod.POST)
    public ResponseEntity<?> updateStockAndOrder(@RequestBody Map<String, Object> payload) {
        long id = ((Number) payload.get("id")).longValue();
        menuService.updateStockAndOrder(id);
        return ResponseEntity.ok().build();
    }

    // 발주량 0으로 초기화
    @RequestMapping(value = "/cancelUpdateStockAndOrder", method = RequestMethod.POST)
    public ResponseEntity<?> cancelUpdateStockAndOrder(@RequestBody Map<String, Object> payload) {
        long id = ((Number) payload.get("id")).longValue();
        menuService.cancelUpdateStockAndOrder(id);
        return ResponseEntity.ok().build();
    }

    // 카테고리 관리 페이지를 반환
    @RequestMapping(value = "/categoryList", method = RequestMethod.GET)
    public String getCategoryList(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        CategoryRequest categoryRequest = new CategoryRequest();
        model.addAttribute("categoryRequest", categoryRequest);
        return "/menu/categoryList.html";
    }

    // 카테고리 추가 페이지 반환
    @RequestMapping(value = "/categoryCreate", method = RequestMethod.GET)
    public String getCategoryCreate(Model model) {
        CategoryRequest categoryRequest = new CategoryRequest();
        model.addAttribute("categoryRequest", categoryRequest);
        return "menu/categoryCreate.html";
    }

    // 카테고리 추가 작업 후 리스트 반환
    @RequestMapping(value = "/categoryCreate", method = RequestMethod.POST)
    public String postCategoryCreate(Model model, @ModelAttribute("categoryRequest") CategoryRequest categoryRequest) {
        categoryService.saveCategory(categoryRequest);
        return "redirect:/categoryList";
    }

    // 카테고리 수정을 위해 pk와 새 카테고리명을 받아서 작업 후 리스트 반환
    @RequestMapping(value = "/categoryUpdate", method = RequestMethod.POST)
    @ResponseBody
    public void postCategoryUpdate(Model model, @RequestParam Map<String, Object> params) {
        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName((String) params.get("name"));
        categoryRequest.setId(Long.parseLong((String) params.get("id")));
        categoryService.saveCategory(categoryRequest);
    }

    @RequestMapping(value = "/CategoryDelete", method = RequestMethod.POST)
    @ResponseBody
    public void getCategoryDelete(Model model, @RequestParam Map<String, Object> params) {
        categoryService.deleteCategory(Integer.parseInt((String) params.get("id")));
    }
}
