package co.kr.necohost.semi.app.sales;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.service.SalesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping
@Controller
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    //main화면
    @GetMapping("/salesMain")
    public String salesMain() {
        return "/sales/SalesMain.html";
    }

    @GetMapping("/createSales")
    public String getSales(Model model) {
        model.addAttribute("salesRequest", new SalesRequest());
        return "/sales/createSales.html";
    }

    @PostMapping("/createSales")
    public String createSales(Model model, @ModelAttribute("salesRequest") SalesRequest salesRequest) {
        salesService.save(salesRequest);
        return "/sales/createSales.html";
    }

    @GetMapping("/readSales")
    public String readSales(Model model) {
        List<Sales> sales = salesService.findAll();
        //값 뿌려주기
        model.addAttribute("sales", sales);
        //매출삭제용으로 pk값 받기용
        model.addAttribute("salesRequest", new SalesRequest());
        return "/sales/Saleslist.html";
    }

    //カテゴリー別の総売上高を返還

    @GetMapping("/total-by-category")
    public String getTotalSalesByCategory(Model model, @RequestParam List<Integer> categoryIds) {
        Map<Integer, Integer> totalByCategory = new HashMap<>();
        for (Integer categoryId : categoryIds) {
            int totalSales = salesService.getTotalSalesByCategory(categoryId);
            totalByCategory.put(categoryId, totalSales);
        }
        model.addAttribute("totalByCategory", totalByCategory);
        return "/sales/SalesTotalByCategory.html";
    }


//    @GetMapping("/total-by-category")
//    public String getTotalSalesByCategory(Model model, @RequestParam int categoryId) {
//        int totalByCategory = salesService.getTotalSalesByCategory(categoryId);
//         model.addAttribute("totalByCategory",salesService.getTotalSalesByCategory(categoryId));
//        return "/sales/SalesTotalByCategory.html";
//    }




}
