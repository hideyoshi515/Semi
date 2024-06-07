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
import java.util.TreeMap;

@Controller
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }


    @RequestMapping(value = "/adminSalesMenu", method=RequestMethod.GET)
    public String getAdminSalesMenu() {
        return "/sales/adminSalesMenu.html";
    }


    @RequestMapping(value = "/salesDataBaseControllerMenu", method=RequestMethod.GET)
    public String getSalesDataBaseControllerMenu() {
        return "/sales/salesDataBaseControllerMenu.html";
    }

    @RequestMapping(value="createSales", method=RequestMethod.GET)
    public String getCreateSales(Model model) {
        model.addAttribute("salesRequest", new SalesRequest());
        return "/sales/createSales.html";
    }


    @RequestMapping(value="createSales", method=RequestMethod.POST)
    public String postCreateSales(Model model, @ModelAttribute("salesRequest") SalesRequest salesRequest) {
        salesService.save(salesRequest);
        return "/sales/createSales.html";
    }


    @RequestMapping(value="/readSales", method=RequestMethod.GET)
    public String getReadSales(Model model) {
        List<Sales> sales = salesService.findByProcess(1);
        //값 뿌려주기
        model.addAttribute("sales", sales);
        //매출삭제용으로 pk값 받기용
        model.addAttribute("salesRequest", new SalesRequest());
        return "/sales/readSales.html";
    }

    //カテゴリー別の総売上高を返還

    //6월 7일 11시 5분
    //@GetMapping("/total-by-category")
    @RequestMapping(value = "/totalSalesByCategory", method = RequestMethod.GET)
    public String getTotalSalesByCategory(Model model, @RequestParam List<Integer> categoryIds) {
        Map<Integer, Integer> totalByCategory = new HashMap<>();
        for (Integer categoryId : categoryIds) {
            int totalSales = salesService.getTotalSalesByCategory(categoryId, 1);
            totalByCategory.put(categoryId, totalSales);
        }
        model.addAttribute("totalByCategory", totalByCategory);
        return "/sales/totalSalesByCategory.html";
        //return "/sales/totalSalesByCategory.html";
    }

//    @GetMapping("/total-by-category")
//    public String getTotalSalesByCategory(Model model) {
//        Map<Integer, Integer> totalByCategory = salesService.getTotalSalesByCategory(categoryId,1);
//
//        model.addAttribute("totalByCategory", totalByCategory);
//        return "/sales/totalSalesByCategory.html";
//    }

    //기능 확인하고 이름 바꾸겠습니다.

    @GetMapping("/totalSalesbyCategory2")

    public String getTotalSalesByCategory(Model model) {
        Map<Integer, Double> totalByCategory = salesService.getTotalSalesByCategory();
        model.addAttribute("totalByCategory", totalByCategory);
        System.out.println(totalByCategory);
        return "/sales/totalSalesByCategory.html";
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

    //作成中
//    @GetMapping("/total-by-yearandmonth-input")
//    public String getTotalSalesByYearAndMonth(@RequestParam(value = "year", required = false) Integer year, @RequestParam(value="month", required = false) Integer month, Model model) {
//        if (year != null && month != null) {
//            double totalSles = salesService.getTotalSalesByYearAndMonth(year,month);
//            model.addAttribute("year",year);
//            model.addAttribute("month",month);
//            model.addAttribute("totalSles", totalSles);
//        }
//        return "/sales/salestotalbyyearandmonthinput";
//    }

    //    @GetMapping("/total-by-yearandmonth-input")
//    public String getTotalSalesByYearAndMonth(@RequestParam(value = "year", required = false) Integer year,
//                                              @RequestParam(value="month", required = false) Integer month, Model model) {
//        if (year != null && month != null) {
//            double totalSales = salesService.getTotalSalesByYearAndMonth(year, month);
//            model.addAttribute("year", year);
//            model.addAttribute("month", month);
//            model.addAttribute("totalSales", totalSales);
//        }
//
//        return "/sales/salestotalbyyearandmonthinput";
//    }
    //6월 7일 기능추가중
    @GetMapping("/total-by-yearandmonth-input")
    public String getTotalSalesByYearAndMonth(@RequestParam(value = "year", required = false) Integer year,
                                              @RequestParam(value = "month", required = false) Integer month, Model model) {
        if (year != null && month != null) {
            double totalSales = salesService.getTotalSalesByYearAndMonth(year, month);
            double growthRate = salesService.getMonthlySalesGrowthRate(year, month);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("totalSales", totalSales);
            model.addAttribute("growthRate", growthRate);
        }

        return "/sales/salestotalbyyearandmonthinput";
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

    //6월 5일 오후 6시
//    @GetMapping("/total-by-month")
//    public String getMonthlySalesByProcess(Model model) {
//        Map<String, Double> monthlySales = salesService.getMonthlySalesByProcess();
//        model.addAttribute("monthlySales", monthlySales);
//        return "/sales/salestotalbymonth";
//    }
//6월 5일 오후 6시 30분
    @GetMapping("/total-by-month")
    public String getMonthlySalesByProcess(Model model) {
        Map<String, Double> monthlySales = salesService.getMonthlySalesByProcess();
        Map<String, Double> sortedMonthlySales = new TreeMap<>(monthlySales); // TreeMap을 사용하여 정렬
        model.addAttribute("monthlySales", sortedMonthlySales);
        return "/sales/salestotalbymonth";
    }


}








