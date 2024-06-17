package co.kr.necohost.semi.app.sales;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.service.MenuService;
import co.kr.necohost.semi.domain.service.SalesService;
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

    public SalesController(MenuService menuService,SalesService salesService, MenuRepository menuRepository) {
        this.salesService = salesService;
        this.menuService = menuService;
        this.menuRepository = menuRepository;
    }

    //관리자 페이지 - 홈 페이지(오늘의 현재 시간까지의 매출)를 반환하는 메서드(css있음)
    @RequestMapping(value = "/adminSalesMainHome", method = RequestMethod.GET)
    public String getAdminSalesMainHome(Model model) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);
        model.addAttribute("currentTime", formattedNow);

        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59);

        // 서비스에서 시간별 매출 데이터를 가져옴
        Map<LocalDateTime, Double> hourlySales = salesService.getHourlySalesByDay(startOfDay, endOfDay);
        Map<String, Double> formattedHourlySales = salesService.getFormattedHourlySales(hourlySales);
        String formattedTotalSalesToday = salesService.getFormattedTotalSalesUntilNow(now);

        model.addAttribute("totalSalesToday", formattedTotalSalesToday);
        model.addAttribute("hourlySales", formattedHourlySales);

        return "sales/adminSalesMainHome";
    }


    // 구버전 관리자 판매 메뉴 페이지를 반환하는 메서드(css없음)
    @RequestMapping(value = "/adminSalesMenu", method=RequestMethod.GET)
    public String getOldAdminSalesMenu() {
        return "/sales/adminSalesMenu.html";
    }

    // 신버전 관리자 페이지 - 판매 관리 페이지를 반환하는 메서드(css있음)
    @RequestMapping(value = "/adminSalesMainMenu", method=RequestMethod.GET)
    public String getAdminSalesMainMenu() {
        return "/sales/adminSalesMainMenu.html";
    }




    @RequestMapping(value = "/adminSalesMainHome2", method=RequestMethod.GET)
    public String getAdmininSalesMainHome2(Model model){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);
        model.addAttribute("currentTime", formattedNow);

        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59);

        Map<LocalDateTime, Double> hourlySales = salesService.getHourlySalesByDay(startOfDay, endOfDay);

        double totalSalesToday = salesService.getTotalSalesUntilNow(now);
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        String formattedTotalSalesToday = numberFormat.format(totalSalesToday);
        model.addAttribute("totalSalesToday", formattedTotalSalesToday);
        model.addAttribute("hourlySales", hourlySales);

        return "/sales/adminSalesMainHome2.html";

    }

    // 판매 데이터베이스 컨트롤러 메뉴 페이지를 반환하는 메서드
    @RequestMapping(value = "/salesDataBaseControllerMenu", method=RequestMethod.GET)
    public String getSalesDataBaseControllerMenu() {
        return "/sales/salesDataBaseControllerMenu.html";
    }

    // 판매 데이터를 읽어오는 메서드
    @RequestMapping(value="/readSales", method=RequestMethod.GET)
    public String getReadSales(Model model) {
        List<Sales> sales = salesService.findByProcess(1);
        model.addAttribute("sales", sales);
        model.addAttribute("salesRequest", new SalesRequest());
        return "/sales/readSales.html";
    }

    //연도별 총 판매액을 반환하는 메서드 1번째 버튼 年度別総売上高
    @RequestMapping(value = "/totalSalesByYear", method = RequestMethod.GET)
    public String getTotalSalesByYear(Model model) {
        Map<Integer, Double> yearlySales = salesService.getYearlySalesByProcess();
        Map<String, Double> yearlySalesStringKey = salesService.getFormattedYearlySales(yearlySales);

        model.addAttribute("yearlySales", yearlySalesStringKey);

        return "sales/totalSalesByYear";
    }

    //월별 총 판매액을 반환하는 메서드/2번째 버튼 月別総売上高
    @RequestMapping(value = "/totalSalesByMonth", method = RequestMethod.GET)
    public String getTotalSalesByMonth(Model model) {
        Map<String, Double> monthlySales = salesService.getMonthlySalesByProcess();
        model.addAttribute("monthlySales", monthlySales);
        return "sales/totalSalesByMonth";
    }

    //입력된 날짜(연-월-일)의 총 판매액을 반환하는 메서드//3번째 버튼 日商/週間売上
    @RequestMapping(value = "/totalSalesByDayInput", method = RequestMethod.GET)
    public String getTotalSalesByDay(@RequestParam(value="year", required = false) Integer year,
                                     @RequestParam(value="month", required = false) Integer month,
                                     @RequestParam(value="day", required = false) Integer day,
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


    // 입력된 날짜(연-월-일)가 속한 주의 주별 매출(1주간 총매출, 요일별 매출)을 반환하는 메서드//3번째 버튼 日商/週間売上
    @RequestMapping(value = "/totalWeeklySalesByDayInput", method = RequestMethod.GET)
    public String getTotalWeeklySalesByDay(@RequestParam(value="year", required = false) Integer year,
                                           @RequestParam(value="month", required = false) Integer month,
                                           @RequestParam(value="day", required = false) Integer day,
                                           Model model) {

        if (year != null && month != null && day != null) {
            Map<LocalDate, Double> weeklySales = salesService.getWeeklySalesByDay(year, month, day);
            double totalWeeklySales = weeklySales.values().stream().mapToDouble(Double::doubleValue).sum();

            // 매출 금액 형식 지정
            DecimalFormat decimalFormat = new DecimalFormat("#,###");

            // 천단위 콤마 추가 및 소수점 생략
            Map<LocalDate, String> formattedWeeklySales = new TreeMap<>();
            for (Map.Entry<LocalDate, Double> entry : weeklySales.entrySet()) {
                formattedWeeklySales.put(entry.getKey(), decimalFormat.format(entry.getValue()));
            }
            String formattedTotalWeeklySales = decimalFormat.format(totalWeeklySales);

            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("day", day);
            model.addAttribute("weeklySales", formattedWeeklySales);
            model.addAttribute("totalWeeklySales", formattedTotalWeeklySales);
        }
        return "/sales/totalWeeklySalesByDayInput";
    }


    //카테고리별 판매액/판매량을 기간 검색하여 반환하는 메서드. 4번째 버튼 カテゴリ別売上販売の現状
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
        String formattedStartDate = startDate.format(formatter);
        String formattedEndDate = endDate.format(formatter);

        model.addAttribute("formattedStartDate", formattedStartDate);
        model.addAttribute("formattedEndDate", formattedEndDate);

        return "sales/totalSalesbyCategoryAndPeriodInput";
    }

    //메뉴별 판매액/판매량을 기간 검색하여 반환하는 메서드.  5번째 버튼 メニュー別売上販売の現状
    @RequestMapping(value = "/totalSalesbyMenuAndPeriodInput", method = RequestMethod.GET)
    public String getTotalSalesByMenuAndPeriodInput(Model model) {
        return "/sales/totalSalesbyMenuAndPeriodInput";
    }
    //내림차순 시도중 6월 17일 11시 10분
    @RequestMapping(value = "/totalSalesbyMenuAndPeriodInput", method = RequestMethod.POST)
    public String postTotalSalesbyMenuAndPeriodInput(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                     @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                     Model model) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Map<String, Double> salesByMenu = salesService.getSalesByMenuInRange(startDateTime, endDateTime);
        Map<String, Integer> quantityByMenu = salesService.getQuantityByMenuInRange(startDateTime, endDateTime);

        // salesByMenu를 내림차순으로 정렬
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
        String formattedStartDate = startDate.format(formatter);
        String formattedEndDate = endDate.format(formatter);

        model.addAttribute("formattedStartDate", formattedStartDate);
        model.addAttribute("formattedEndDate", formattedEndDate);

        return "sales/totalSalesbyMenuAndPeriodInput";
    }

    // 입력된 연도의 총 판매액을 반환하는 메서드
    @RequestMapping(value= "/totalSalesByYearInput", method = RequestMethod.GET)
    public String getTotalSalesByYearInput(@RequestParam(value = "year", required = false) Integer year, Model model) {
        if (year != null) {
            double totalSales = salesService.getTotalSalesByYear(year);
            model.addAttribute("year", year);
            model.addAttribute("totalSales", totalSales);
        }
        return "/sales/totalSalesByYearInput";
    }

    // 입력된 연도와 월의 총 판매액 및 성장률을 반환하는 메서드
    @RequestMapping(value="/totalSalesByYearAndMonthInput" , method = RequestMethod.GET)
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








    // 입력된 연도와 카테고리의 총 판매액을 반환하는 메서드
    @RequestMapping(value="/totalSalesByYearAndCategoryInput" , method = RequestMethod.GET)
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

        Map<String, Object> salesData = salesService.calculateMenuSalesData(menuId);
        model.addAllAttributes(salesData);

        Map<String, Double> totalSalesAndQuantityByProcess = salesService.getTotalSalesAndQuantityByProcess(1);
        double totalSalesAmountProcess1 = totalSalesAndQuantityByProcess.get("totalSalesAmount");
        double totalQuantityProcess1 = totalSalesAndQuantityByProcess.get("totalQuantity");

        model.addAttribute("totalQuantityProcess1", (int) totalQuantityProcess1);
        model.addAttribute("totalSalesAmountProcess1", totalSalesAmountProcess1);

        double totalSalesAmount = (double) salesData.get("totalSalesAmount");
        int totalQuantity = (int) salesData.get("totalQuantity");

        double salesPercentage = calculatePercentage(totalSalesAmount, totalSalesAmountProcess1);
        double quantityPercentage = calculatePercentage(totalQuantity, totalQuantityProcess1);

        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        DecimalFormat df = new DecimalFormat("#.##");
        //累積売り上げシェア
        model.addAttribute("salesPercentage", df.format(salesPercentage));
        //累積販売量シェア
        model.addAttribute("quantityPercentage", df.format(quantityPercentage));
        //累積販売量
        model.addAttribute("totalQuantity", decimalFormat.format(totalQuantity));
        //累積売り上げ
        model.addAttribute("totalSalesAmount", decimalFormat.format(totalSalesAmount));
//        model.addAttribute("totalSalesAmount", (long) totalSalesAmount);

        double totalCostByProcess = salesService.calculateTotalCostByProcess(1);

        model.addAttribute("totalCostByProcess", decimalFormat.format(totalCostByProcess));
        Menu menu = (Menu) salesData.get("menu");

        int totalMenuCost = totalQuantity * menu.getCost();
        int menuPrice =   menu.getPrice();
        int menuCost = menu.getCost();



        model.addAttribute("menuprice", decimalFormat.format(menuPrice));
        model.addAttribute("menucost", decimalFormat.format(menuCost));

        double costPercentage = calculatePercentage(totalQuantity * menu.getCost(), totalCostByProcess);
        //累積原価
        model.addAttribute("totalMenuCost", decimalFormat.format(totalMenuCost));
        //累積シェア
        model.addAttribute("costPercentage", df.format(costPercentage));

        LocalDateTime now = LocalDateTime.now();

        // 오늘의 총 판매량과 판매액을 계산
        Map<String, Double> todayTotalSalesAndQuantity = salesService.getTotalSalesAndQuantityToday();
        double totalSalesAmountToday = todayTotalSalesAndQuantity.get("totalSalesAmount");
        double totalQuantityToday = todayTotalSalesAndQuantity.get("totalQuantity");

        // 이번 주의 총 판매량과 판매액을 계산 (일요일 시작, 토요일 끝)
        LocalDateTime startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).toLocalDate().atStartOfDay();
        LocalDateTime endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).toLocalDate().atTime(23, 59, 59);

        Map<String, Double> weekTotalSalesAndQuantity = salesService.getTotalSalesAndQuantityByDateRange(startOfWeek, endOfWeek);
        double totalSalesAmountWeek = weekTotalSalesAndQuantity.get("totalSalesAmount");
        double totalQuantityWeek = weekTotalSalesAndQuantity.get("totalQuantity");

        // 이번 달의 총 판매량과 판매액을 계산
        Map<String, Double> monthTotalSalesAndQuantity = salesService.getTotalSalesAndQuantityThisMonth();
        double totalSalesAmountMonth = monthTotalSalesAndQuantity.get("totalSalesAmount");
        double totalQuantityMonth = monthTotalSalesAndQuantity.get("totalQuantity");

        // 이번 분기의 총 판매량과 판매액을 계산
        Map<String, Double> quarterTotalSalesAndQuantity = salesService.getTotalSalesAndQuantityThisQuarter();
        double totalSalesAmountQuarter = quarterTotalSalesAndQuantity.get("totalSalesAmount");
        double totalQuantityQuarter = quarterTotalSalesAndQuantity.get("totalQuantity");

        // 금년도의 총 판매량과 판매액을 계산
        Map<String, Double> yearTotalSalesAndQuantity = salesService.getTotalSalesAndQuantityThisYear();
        double totalSalesAmountYear = yearTotalSalesAndQuantity.get("totalSalesAmount");
        double totalQuantityYear = yearTotalSalesAndQuantity.get("totalQuantity");

        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime startOfQuarter = now.with(now.getMonth().firstMonthOfQuarter()).withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime startOfYear = now.withDayOfYear(1).toLocalDate().atStartOfDay();

        int todayQuantity = salesService.getTotalQuantityByDateRange(startOfToday, now, menuId);
        double todaySalesAmount = salesService.getTotalSalesByDateRange(startOfToday, now, menuId);

        int weekQuantity = salesService.getTotalQuantityByDateRange(startOfWeek, now, menuId);
        double weekSalesAmount = salesService.getTotalSalesByDateRange(startOfWeek, now, menuId);

        int monthQuantity = salesService.getTotalQuantityByDateRange(startOfMonth, now, menuId);
        double monthSalesAmount = salesService.getTotalSalesByDateRange(startOfMonth, now, menuId);

        int quarterQuantity = salesService.getTotalQuantityByDateRange(startOfQuarter, now, menuId);
        double quarterSalesAmount = salesService.getTotalSalesByDateRange(startOfQuarter, now, menuId);

        int yearQuantity = salesService.getTotalQuantityByDateRange(startOfYear, now, menuId);
        double yearSalesAmount = salesService.getTotalSalesByDateRange(startOfYear, now, menuId);

        double todayQuantityPercentage = calculatePercentage(todayQuantity, totalQuantityToday);
        double todaySalesPercentage = calculatePercentage(todaySalesAmount, totalSalesAmountToday);

        double weekQuantityPercentage = calculatePercentage(weekQuantity, totalQuantityWeek);
        double weekSalesPercentage = calculatePercentage(weekSalesAmount, totalSalesAmountWeek);

        double monthQuantityPercentage = calculatePercentage(monthQuantity, totalQuantityMonth);
        double monthSalesPercentage = calculatePercentage(monthSalesAmount, totalSalesAmountMonth);

        double quarterQuantityPercentage = calculatePercentage(quarterQuantity, totalQuantityQuarter);
        double quarterSalesPercentage = calculatePercentage(quarterSalesAmount, totalSalesAmountQuarter);

        double yearQuantityPercentage = calculatePercentage(yearQuantity, totalQuantityYear);
        double yearSalesPercentage = calculatePercentage(yearSalesAmount, totalSalesAmountYear);

        model.addAttribute("todayQuantity", decimalFormat.format(todayQuantity));
        model.addAttribute("todaySalesAmount", decimalFormat.format(todaySalesAmount));
        model.addAttribute("todayQuantityPercentage", df.format(todayQuantityPercentage));
        model.addAttribute("todaySalesPercentage", df.format(todaySalesPercentage));

        model.addAttribute("weekQuantity", decimalFormat.format(weekQuantity));
        model.addAttribute("weekSalesAmount", decimalFormat.format(weekSalesAmount));
        model.addAttribute("weekQuantityPercentage", df.format(weekQuantityPercentage));
        model.addAttribute("weekSalesPercentage", df.format(weekSalesPercentage));

        model.addAttribute("monthQuantity", decimalFormat.format(monthQuantity));
        model.addAttribute("monthSalesAmount", decimalFormat.format(monthSalesAmount));
        model.addAttribute("monthQuantityPercentage", df.format(monthQuantityPercentage));
        model.addAttribute("monthSalesPercentage", df.format(monthSalesPercentage));

        model.addAttribute("quarterQuantity", decimalFormat.format(quarterQuantity));
        model.addAttribute("quarterSalesAmount", decimalFormat.format(quarterSalesAmount));
        model.addAttribute("quarterQuantityPercentage", df.format(quarterQuantityPercentage));
        model.addAttribute("quarterSalesPercentage", df.format(quarterSalesPercentage));

        model.addAttribute("yearQuantity", decimalFormat.format(yearQuantity));
        model.addAttribute("yearSalesAmount", decimalFormat.format(yearSalesAmount));
        model.addAttribute("yearQuantityPercentage", df.format(yearQuantityPercentage));
        model.addAttribute("yearSalesPercentage", df.format(yearSalesPercentage));

        return "sales/salesAnalysisByMenuInput";
    }

    private double calculatePercentage(double value, double total) {
        return total != 0 ? (value / total) * 100 : 0;
    }
















    @GetMapping("/salesAnalysisDesignExample")
    public String getSalesAnalysisDesignExample() {
        return "sales/salesAnalysisDesignExample";
    }








}
