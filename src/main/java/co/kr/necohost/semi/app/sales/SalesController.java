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
        return "/sales/salesmain.html";
    }

    @GetMapping("/createSales")
    public String getSales(Model model) {
        model.addAttribute("salesRequest", new SalesRequest());
        return "/sales/createsales.html";
    }

    @PostMapping("/createSales")
    public String createSales(Model model, @ModelAttribute("salesRequest") SalesRequest salesRequest) {
        salesService.save(salesRequest);
        return "/sales/createsales.html";
    }

    @GetMapping("/readSales")
    public String readSales(Model model) {
        List<Sales> sales = salesService.findByProcess(1);
        //값 뿌려주기
        model.addAttribute("sales", sales);
        //매출삭제용으로 pk값 받기용
        model.addAttribute("salesRequest", new SalesRequest());
        return "/sales/saleslist.html";
    }

    //カテゴリー別の総売上高を返還

    @GetMapping("/total-by-category")
    public String getTotalSalesByCategory(Model model, @RequestParam List<Integer> categoryIds) {
        Map<Integer, Integer> totalByCategory = new HashMap<>();
        for (Integer categoryId : categoryIds) {
            int totalSales = salesService.getTotalSalesByCategory(categoryId, 1);
            totalByCategory.put(categoryId, totalSales);
        }
        model.addAttribute("totalByCategory", totalByCategory);
        return "/sales/salestotalbycategory.html";
    }

    @GetMapping("/total-by-year")
    public String getYearlySalesByProcess(Model model) {
        Map<Integer, Double> yearlySales = salesService.getYearlySalesByProcess();
        model.addAttribute("yearlySales", yearlySales);
        System.out.println(yearlySales);
        return "/sales/salestotalbyyear.html";
    }

//    @GetMapping("/total-by-year")
//    public String getTotalSalesByYear(@RequestParam(value = "year", required = false) Integer year, Model model) {
//        if (year != null) {
//            double totalSales = salesService.getTotalSalesByYear(year);
//            model.addAttribute("year", year);
//            model.addAttribute("totalSales", totalSales);
//        }
//        return "/sales/salestotalbyyear";
//    }

//    @GetMapping("/total-by-year-input")
//    public String getTotalSalesByYear(@RequestParam("year") int year, Model model) {
//        double totalSales = salesService.getTotalSalesByYear(year);
//        model.addAttribute("year", year);
//        model.addAttribute("totalSales", totalSales);
//        return "/sales/salestotalbyyearinput";
//    }

    @GetMapping("/total-by-year-input")
    public String getTotalSalesByYear(@RequestParam(value = "year", required = false) Integer year, Model model) {
        if (year != null) {
            double totalSales = salesService.getTotalSalesByYear(year);
            model.addAttribute("year", year);
            model.addAttribute("totalSales", totalSales);
        }
        return "/sales/salestotalbyyearinput";
    }

    @GetMapping("/total-by-year-and-category-input")
    public String getTotalSalesByYearAndCategory(@RequestParam(value = "year", required = false) Integer year,
                                                 @RequestParam(value = "category", required = false) Integer category, Model model) {
        if (year != null && category != null) {
            double totalSales = salesService.getTotalSalesByYearAndCategory(year, category);
            model.addAttribute("year", year);
            model.addAttribute("category", category);
            model.addAttribute("totalSales", totalSales);
        }
        return "/sales/salestotalbyyearandcategoryinput";
    }









}








