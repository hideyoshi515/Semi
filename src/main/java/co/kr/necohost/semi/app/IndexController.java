package co.kr.necohost.semi.app;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.*;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.repository.OrderNumRepository;
import co.kr.necohost.semi.domain.service.CategoryService;
import co.kr.necohost.semi.domain.service.MenuService;
import co.kr.necohost.semi.domain.service.SalesService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    private final SalesService salesService;
    private final CategoryService categoryService;
    private final MenuService menuService;
    private final MenuRepository menuRepository;
    private final OrderNumRepository orderNumRepository;

    public IndexController(SalesService salesService, CategoryService categoryService, MenuService menuService, MenuRepository menuRepository, OrderNumRepository orderNumRepository) {
        this.salesService = salesService;
        this.categoryService = categoryService;
        this.menuService = menuService;
        this.menuRepository = menuRepository;
        this.orderNumRepository = orderNumRepository;
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String getIndex(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
        model.addAttribute("session", session);
        return "redirect:/";
    }

    @RequestMapping(value = "/pos", method = RequestMethod.GET)
    public String getPOS(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("session", session);
        model.addAttribute("categories", categories);
        return "pos/index.html";
    }

    @RequestMapping(value = "/pos/getMenu", method = RequestMethod.GET)
    @ResponseBody
    public List<Menu> getMenusByCategory(@RequestParam Map<String, Object> params, HttpSession session){
        List<Menu> menus = menuService.getMenuByCategory(Integer.parseInt(params.get("category").toString()));
        return menus;
    }

    @RequestMapping(value = "/pos/order", method = RequestMethod.POST)
    @ResponseBody
    public String postPOSOrder(@RequestBody List<POSOrder> orderItems, HttpSession session){
            LocalDateTime localDate = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = localDate.format(formatter);
            OrderNum orderNum = orderNumRepository.save(new OrderNum());

            for (POSOrder order : orderItems) {
                Menu menu = menuService.getMenuById(order.getId());
                int quantity = order.getQuantity();
                SalesRequest sales = new SalesRequest();
                sales.setDate(LocalDateTime.parse(formattedDateTime, formatter));
                sales.setCategory(menu.getCategory());
                sales.setMenu(menu.getId());
                sales.setPrice(menu.getPrice());
                sales.setQuantity(quantity);
                sales.setDevice(2);
                sales.setDeviceNum(2);
                sales.setOrderNum(orderNum.getOrderNum());
                sales.setProcess(1);
                salesService.save(sales);
                menu.setStock(menu.getStock() - quantity);
                menuRepository.save(menu);
            }
            return "注文が成功しました";
    }

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String getAdmin(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
        Map<String, Long> todaySales = salesService.findSalesByToday();
        model.addAttribute("todaySales", todaySales);
        model.addAttribute("session", session);
        model.addAttribute("lang", lang);
        return "index.html";
    }
}
