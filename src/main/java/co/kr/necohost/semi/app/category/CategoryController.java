package co.kr.necohost.semi.app.category;

import co.kr.necohost.semi.domain.model.dto.CategoryRequest;
import co.kr.necohost.semi.domain.model.entity.Category;
import co.kr.necohost.semi.domain.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // 카테고리 관리 페이지를 반환
    @RequestMapping(value = "/categoryList", method = RequestMethod.GET)
    public String getCategoryList(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        CategoryRequest categoryRequest = new CategoryRequest();
        model.addAttribute("categoryRequest", categoryRequest);
        return "/category/categoryList.html";
    }

    // 카테고리 추가 페이지 반환
    @RequestMapping(value = "/categoryCreate", method = RequestMethod.GET)
    public String getCategoryCreate(Model model) {
        CategoryRequest categoryRequest = new CategoryRequest();
        model.addAttribute("categoryRequest", categoryRequest);
        return "category/categoryCreate.html";
    }

    // 카테고리 추가 작업 후 리스트 반환
    @RequestMapping(value = "/categoryCreate", method = RequestMethod.POST)
    public String postCategoryCreate(Model model, @ModelAttribute("categoryRequest") CategoryRequest categoryRequest) {
        categoryService.saveCategory(categoryRequest);
        return "redirect:/categoryList";
    }

    // 카테고리 수정을 위해 pk와 새 카테고리명을 받아서 작업 후 ajax 응답 보냄
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
