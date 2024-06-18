package co.kr.necohost.semi.app.sales;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.service.MenuService;
import co.kr.necohost.semi.domain.service.SalesService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SalesController {
    private final MenuService menuService;
    private final SalesService salesService;
    private final MenuRepository menuRepository;

    public SalesController(MenuService menuService, SalesService salesService, MenuRepository menuRepository) {
        this.salesService = salesService;
        this.menuService = menuService;
        this.menuRepository = menuRepository;
    }

    @RequestMapping(value = "/adminSalesMainHome", method = RequestMethod.GET)
    public String getAdminSalesMainHome(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);

        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.with(LocalTime.MAX);

        Map<LocalDateTime, Double> hourlySales = salesService.getHourlySalesByDay(startOfDay, endOfDay);
        Map<String, Double> formattedHourlySales = salesService.getFormattedHourlySales(hourlySales);
        String formattedTotalSalesToday = salesService.getFormattedTotalSalesUntilNow(now);
        Map<String, Long> todaySales = salesService.findSalesByToday();

        model.addAttribute("currentTime", formattedNow);
        model.addAttribute("totalSalesToday", formattedTotalSalesToday);
        model.addAttribute("hourlySales", formattedHourlySales);
        model.addAttribute("todaySales", todaySales);
        model.addAttribute("session", session);
        model.addAttribute("lang", lang);

        return "sales/adminSalesMainHome";
    }

    @RequestMapping(value = "/adminSalesMenu", method = RequestMethod.GET)
    public String getOldAdminSalesMenu() {
        return "/sales/adminSalesMenu.html";
    }

    @RequestMapping(value = "/adminSalesMainMenu", method = RequestMethod.GET)
    public String getAdminSalesMainMenu() {
        return "/sales/adminSalesMainMenu.html";
    }

    @RequestMapping(value = "/adminSalesMainHome2", method = RequestMethod.GET)
    public String getAdminSalesMainHome2(Model model) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);

        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.with(LocalTime.MAX);

        Map<LocalDateTime, Double> hourlySales = salesService.getHourlySalesByDay(startOfDay, endOfDay);

        double totalSalesToday = salesService.getTotalSalesUntilNow(now);
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        String formattedTotalSalesToday = numberFormat.format(totalSalesToday);

        model.addAttribute("currentTime", formattedNow);
        model.addAttribute("totalSalesToday", formattedTotalSalesToday);
        model.addAttribute("hourlySales", hourlySales);

        return "/sales/adminSalesMainHome2.html";
    }

    @RequestMapping(value = "/salesDataBaseControllerMenu", method = RequestMethod.GET)
    public String getSalesDataBaseControllerMenu() {
        return "/sales/salesDataBaseControllerMenu.html";
    }

    @RequestMapping(value = "/readSales", method = RequestMethod.GET)
    public String getReadSales(Model model) {
        List<Sales> sales = salesService.findByProcess(1);
        model.addAttribute("sales", sales);
        model.addAttribute("salesRequest", new SalesRequest());
        return "/sales/readSales.html";
    }

    @RequestMapping(value = "/totalSalesByYear", method = RequestMethod.GET)
    public String getTotalSalesByYear(Model model) {
        Map<Integer, Double> yearlySales = salesService.getYearlySalesByProcess();
        Map<String, Double> yearlySalesStringKey = salesService.getFormattedYearlySales(yearlySales);

        model.addAttribute("yearlySales", yearlySalesStringKey);

        return "sales/totalSalesByYear";
    }

    @RequestMapping(value = "/totalSalesByMonth", method = RequestMethod.GET)
    public String getTotalSalesByMonth(Model model) {
        Map<String, Double> monthlySales = salesService.getMonthlySalesByProcess();
        model.addAttribute("monthlySales", monthlySales);
        return "sales/totalSalesByMonth";
    }

    @RequestMapping(value = "/totalSalesByDayInput", method = RequestMethod.GET)
    public String getTotalSalesByDay(@RequestParam(value = "year", required = false) Integer year,
                                     @RequestParam(value = "month", required = false) Integer month,
                                     @RequestParam(value = "day", required = false) Integer day,
                                     Model model) {

        if (year != null && month != null && day != null) {
            double totalSales = salesService.getTotalSalesByDay(year, month, day);
            DecimalFormat decimalFormat = new DecimalFormat("#,##0");
            String formattedTotalSales = decimalFormat.format(totalSales);

            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("day", day);
            model.addAttribute("totalSales", formattedTotalSales);
        }
        return "/sales/totalSalesByDayInput.html";
    }

    @RequestMapping(value = "/totalWeeklySalesByDayInput", method = RequestMethod.GET)
    public String getTotalWeeklySalesByDay(@RequestParam(value = "year", required = false) Integer year,
                                           @RequestParam(value = "month", required = false) Integer month,
                                           @RequestParam(value = "day", required = false) Integer day,
                                           Model model) {

        if (year != null && month != null && day != null) {
            Map<LocalDate, Double> weeklySales = salesService.getWeeklySalesByDay(year, month, day);
            double totalWeeklySales = weeklySales.values().stream().mapToDouble(Double::doubleValue).sum();

            DecimalFormat decimalFormat = new DecimalFormat("#,###");

            Map<LocalDate, String> formattedWeeklySales = weeklySales.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> decimalFormat.format(entry.getValue()),
                            (oldValue, newValue) -> oldValue,
                            LinkedHashMap::new // LinkedHashMap을 사용하여 순서를 유지
                    ));

            String formattedTotalWeeklySales = decimalFormat.format(totalWeeklySales);

            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("day", day);
            model.addAttribute("weeklySales", formattedWeeklySales);
            model.addAttribute("totalWeeklySales", formattedTotalWeeklySales);
        }
        return "/sales/totalWeeklySalesByDayInput";
    }

    @RequestMapping(value = "/totalSalesbyCategoryAndPeriodInput", method = RequestMethod.GET)
    public String getTotalSalesByCategoryAndPeriodInput(Model model) {
        return "/sales/totalSalesbyCategoryAndPeriodInput";
    }

    @RequestMapping(value = "/totalSalesbyCategoryAndPeriodInput", method = RequestMethod.POST)
    public String postTotalSalesByCategoryAndPeriodInput(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                         @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                         Model model) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Map<String, Double> salesByCategory = salesService.getSalesByCategoryInRange(startDateTime, endDateTime);
        Map<String, Integer> quantityByCategory = salesService.getQuantityByCategoryInRange(startDateTime, endDateTime);

        model.addAttribute("salesByCategory", salesByCategory);
        model.addAttribute("quantityByCategory", quantityByCategory);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        model.addAttribute("formattedStartDate", startDate.format(formatter));
        model.addAttribute("formattedEndDate", endDate.format(formatter));

        return "sales/totalSalesbyCategoryAndPeriodInput";
    }

    @RequestMapping(value = "/totalSalesbyMenuAndPeriodInput", method = RequestMethod.GET)
    public String getTotalSalesByMenuAndPeriodInput(Model model) {
        return "/sales/totalSalesbyMenuAndPeriodInput";
    }

    @RequestMapping(value = "/totalSalesbyMenuAndPeriodInput", method = RequestMethod.POST)
    public String postTotalSalesByMenuAndPeriodInput(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                     @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                     Model model) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Map<String, Double> salesByMenu = salesService.getSalesByMenuInRange(startDateTime, endDateTime);
        Map<String, Integer> quantityByMenu = salesService.getQuantityByMenuInRange(startDateTime, endDateTime);

        Map<String, Double> sortedSalesByMenu = salesByMenu.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        model.addAttribute("salesByMenu", sortedSalesByMenu);
        model.addAttribute("quantityByMenu", quantityByMenu);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        model.addAttribute("formattedStartDate", startDate.format(formatter));
        model.addAttribute("formattedEndDate", endDate.format(formatter));

        return "sales/totalSalesbyMenuAndPeriodInput";
    }

    @RequestMapping(value = "/totalSalesByYearInput", method = RequestMethod.GET)
    public String getTotalSalesByYearInput(@RequestParam(value = "year", required = false) Integer year, Model model) {
        if (year != null) {
            double totalSales = salesService.getTotalSalesByYear(year);
            model.addAttribute("year", year);
            model.addAttribute("totalSales", totalSales);
        }
        return "/sales/totalSalesByYearInput";
    }

    @RequestMapping(value = "/totalSalesByYearAndMonthInput", method = RequestMethod.GET)
    public String getTotalSalesByYearAndMonthInput(@RequestParam(value = "year", required = false) Integer year,
                                                   @RequestParam(value = "month", required = false) Integer month, Model model) {
        if (year != null && month != null) {
            double totalSales = salesService.getTotalSalesByYearAndMonth(year, month);
            double growthRate = salesService.getMonthlySalesGrowthRate(year, month);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("totalSales", totalSales);
            model.addAttribute("growthRate", growthRate);
        }
        return "/sales/totalSalesByYearAndMonthInput";
    }

    @RequestMapping(value = "/totalSalesByYearAndCategoryInput", method = RequestMethod.GET)
    public String getTotalSalesByYearAndCategoryInput(@RequestParam(value = "year", required = false) Integer year,
                                                      @RequestParam(value = "category", required = false) Integer category, Model model) {
        if (year != null && category != null) {
            double totalSales = salesService.getTotalSalesByYearAndCategory(year, category);
            model.addAttribute("year", year);
            model.addAttribute("category", category);
            model.addAttribute("totalSales", totalSales);
        }
        return "sales/totalSalesByYearAndCategoryInput";
    }

    @RequestMapping(value = "/salesAnalysisByMenuInput", method = RequestMethod.GET)
    public String getSalesAnalysisByMenuInput(@RequestParam Map<String, Object> params, Model model) {
        Long menuId = Long.parseLong(params.get("menuId").toString());

        Menu menu = menuService.getMenuById(menuId);
        List<Sales> salesData = salesService.getAllSalesForMenu(menuId);

        LocalDateTime now = LocalDateTime.now();
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        DecimalFormat df = new DecimalFormat("#.##");


        double totalSalesAmount = salesData.stream().mapToInt(s-> s.getQuantity()*s.getPrice()).sum();
        int totalQuantity = salesData.stream().mapToInt(Sales::getQuantity).sum();

        model.addAttribute("menu", menu);
        model.addAttribute("totalQuantity", decimalFormat.format(totalQuantity));
        model.addAttribute("totalSalesAmount", decimalFormat.format(totalSalesAmount));
        model.addAttribute("quantityPercentage", df.format(calculatePercentage(salesData.size(), salesData.size())));
        model.addAttribute("salesPercentage", df.format(calculatePercentage(totalSalesAmount, totalSalesAmount)));
        double profitRate = ((menu.getPrice() - menu.getCost()) / (double) menu.getPrice()) * 100;
        model.addAttribute("profitRate", df.format(profitRate));

        addTimePeriodDataToModel(model, "today", filterSalesByDateRange(salesData, now.toLocalDate().atStartOfDay(), now),
                df, decimalFormat);

        addTimePeriodDataToModel(model, "week", filterSalesByDateRange(salesData, now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).toLocalDate().atStartOfDay(), now),
                df, decimalFormat);

        addTimePeriodDataToModel(model, "month", filterSalesByDateRange(salesData, now.withDayOfMonth(1).toLocalDate().atStartOfDay(), now),
                df, decimalFormat);

        addTimePeriodDataToModel(model, "quarter", filterSalesByDateRange(salesData, now.with(now.getMonth().firstMonthOfQuarter()).withDayOfMonth(1).toLocalDate().atStartOfDay(), now),
                df, decimalFormat);

        addTimePeriodDataToModel(model, "year", filterSalesByDateRange(salesData, now.withDayOfYear(1).toLocalDate().atStartOfDay(), now),
                df, decimalFormat);

        return "sales/salesAnalysisByMenuInput";
    }

    private void addTimePeriodDataToModel(Model model, String period, List<Sales> sales, DecimalFormat df, DecimalFormat decimalFormat) {
        int quantity = sales.stream().mapToInt(Sales::getQuantity).sum();
        double salesAmount = sales.stream().mapToDouble(sale -> sale.getPrice() * sale.getQuantity()).sum();

        model.addAttribute(period + "Quantity", decimalFormat.format(quantity));
        model.addAttribute(period + "SalesAmount", decimalFormat.format(salesAmount));

        // Assuming totalQuantity and totalSalesAmount are added to the model elsewhere
        double totalQuantity = Double.parseDouble(model.getAttribute("totalQuantity").toString().replaceAll(",", ""));
        double totalSalesAmount = Double.parseDouble(model.getAttribute("totalSalesAmount").toString().replaceAll(",", ""));

        double quantityPercentage = calculatePercentage(quantity, totalQuantity);
        double salesPercentage = calculatePercentage(salesAmount, totalSalesAmount);

        model.addAttribute(period + "QuantityPercentage", df.format(quantityPercentage));
        model.addAttribute(period + "SalesPercentage", df.format(salesPercentage));
    }

    private List<Sales> filterSalesByDateRange(List<Sales> sales, LocalDateTime start, LocalDateTime end) {
        return sales.stream()
                .filter(sale -> !sale.getDate().isBefore(start) && !sale.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    private double calculatePercentage(double value, double total) {
        return total != 0 ? (value / total) * 100 : 0;
    }


    @GetMapping("/salesAnalysisDesignExample")
    public String getSalesAnalysisDesignExample() {
        return "sales/salesAnalysisDesignExample";
    }
}
