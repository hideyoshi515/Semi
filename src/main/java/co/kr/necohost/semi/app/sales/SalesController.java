package co.kr.necohost.semi.app.sales;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.service.MenuService;
import co.kr.necohost.semi.domain.service.SalesService;
import com.nimbusds.jose.shaded.gson.Gson;
import org.apache.commons.collections4.bag.SynchronizedSortedBag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

        // 누적 매출 계산 및 포맷된 시간 데이터를 담을 Map 생성
        Map<String, Double> formattedHourlySales = new TreeMap<>();
        DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("HH:mm");
        double cumulativeSales = 0.0;
        for (Map.Entry<LocalDateTime, Double> entry : hourlySales.entrySet()) {
            cumulativeSales += entry.getValue();
            formattedHourlySales.put(entry.getKey().format(hourFormatter), cumulativeSales);
        }

        double totalSalesToday = salesService.getTotalSalesUntilNow(now);
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        String formattedTotalSalesToday = numberFormat.format(totalSalesToday);
        model.addAttribute("totalSalesToday", formattedTotalSalesToday);

        model.addAttribute("hourlySales", formattedHourlySales); // 포맷된 시간 데이터 전달
        System.out.println("hourlySales 확인 중");
        System.out.println(formattedHourlySales);
        return "sales/adminSalesMainHome";
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
        System.out.println("hourlySales관련 확인중");
        System.out.println(hourlySales);
        return "/sales/adminSalesMainHome2.html";

    }







    // 판매 데이터베이스 컨트롤러 메뉴 페이지를 반환하는 메서드
    @RequestMapping(value = "/salesDataBaseControllerMenu", method=RequestMethod.GET)
    public String getSalesDataBaseControllerMenu() {
        return "/sales/salesDataBaseControllerMenu.html";
    }

    // 판매 생성 페이지를 반환하는 메서드 (GET 요청)
    @RequestMapping(value="/createSales", method=RequestMethod.GET)
    public String getCreateSales(Model model) {
        model.addAttribute("salesRequest", new SalesRequest());
        return "/sales/createSales.html";
    }

    // 새로운 판매를 생성하는 메서드 (POST 요청)
    @RequestMapping(value="/createSales", method=RequestMethod.POST)
    public String postCreateSales(Model model, @ModelAttribute("salesRequest") SalesRequest salesRequest) {
        salesService.save(salesRequest);
        return "/sales/createSales.html";
    }

    // 판매 데이터를 읽어오는 메서드
    @RequestMapping(value="/readSales", method=RequestMethod.GET)
    public String getReadSales(Model model) {
        List<Sales> sales = salesService.findByProcess(1);
        model.addAttribute("sales", sales);
        model.addAttribute("salesRequest", new SalesRequest());
        return "/sales/readSales.html";
    }


    //각 카테고리별 총 판매액을 반환하는 메서드
    @RequestMapping(value = "/totalSalesbyCategory", method = RequestMethod.GET)
    public String getTotalSalesByCategory(Model model) {
        Map<String, Double> totalByCategory = salesService.getTotalSalesByCategory();
        model.addAttribute("totalByCategory", totalByCategory);
        System.out.println(totalByCategory);
        return "/sales/totalSalesByCategory.html";
    }








    //메뉴별 판매액을 기간 검색하여 반환하는 메서드.
    @RequestMapping(value = "/totalSalesbyMenuAndPeriodInput", method = RequestMethod.GET)
    public String getTotalSalesByMenuAndPeriodInput(Model model) {


        return "/sales/totalSalesbyMenuAndPeriodInput";
    }


    //메뉴별 판매액을 기간 검색하여 반환하는 메서드.
    @RequestMapping(value = "/totalSalesbyMenuAndPeriodInput", method = RequestMethod.POST)
    public String getSalesByMenu(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                 Model model) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Map<String, Double> salesByMenu = salesService.getSalesByMenuInRange(startDateTime, endDateTime);
        Map<String, Integer> quantityByMenu = salesService.getQuantityByMenuInRange(startDateTime, endDateTime);

        model.addAttribute("salesByMenu", salesByMenu);
        model.addAttribute("quantityByMenu", quantityByMenu);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedStartDate = startDate.format(formatter);
        String formattedEndDate = endDate.format(formatter);

        model.addAttribute("formattedStartDate", formattedStartDate);
        model.addAttribute("formattedEndDate", formattedEndDate);

        return "sales/totalSalesbyMenuAndPeriodInput";
    }

    //카테고리별 판매액을 기간 검색하여 반환하는 메서드.
    @RequestMapping(value = "/totalSalesbyCategoryAndPeriodInput", method = RequestMethod.GET)
    public String getTotalSalesByCategoryAndPeriodInput(Model model) {
        return "/sales/totalSalesbyCategoryAndPeriodInput";
    }

    @RequestMapping(value = "/totalSalesbyCategoryAndPeriodInput", method = RequestMethod.POST)
    public String getSalesByCategory(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
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

    //연도별 총 판매액을 반환하는 메서드
    @RequestMapping(value = "/totalSalesByYear", method = RequestMethod.GET)
    public String getTotalSalesByYear(Model model) {
        Map<Integer, Double> yearlySales = salesService.getYearlySalesByProcess();

        // 연도별 오름차순으로 정렬
        Map<String, Double> yearlySalesStringKey = yearlySales.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        model.addAttribute("yearlySales", yearlySalesStringKey);

        return "sales/totalSalesByYear";
    }



    //월별 총 판매액을 반환하는 메서드
    @RequestMapping(value = "/totalSalesByMonth", method = RequestMethod.GET)
    public String getTotalSalesByMonth(Model model) {
        Map<String, Double> monthlySales = salesService.getMonthlySalesByProcess();
        model.addAttribute("monthlySales", monthlySales);
        return "sales/totalSalesByMonth";
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

    //입력된 날짜(연-월-일)의 총 판매액을 반환하는 메서드
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


    // 입력된 날짜(연-월-일)가 속한 주의 주별 매출(1주간 총매출, 요일별 매출)을 반환하는 메서드
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
    //메뉴리스트에서 메뉴의 버튼 중 매출분석 버튼 클릭하면 그 메뉴 관련 매출 판매 보고서 반환 넘 길어서 수정중
//    @RequestMapping(value="salesAnalysisByMenuInput", method= RequestMethod.GET)
//    public String getSalesAnalysisByMenuInput(@RequestParam Map<String, Object> params, Model model) {
//        // menuId 파라미터로 받은 숫자를 가진 메뉴의 테이블에 담긴 정보들을 가져온다.
//        Menu menu = menuService.getMenuById(Long.parseLong(params.get("menuId").toString()));
//        model.addAttribute("menu", menu);
//
//        // menuId 파라미터로 받은 숫자를 가진 세일즈의 테이블 정보들 가져옴
//        List<Sales> salesListall = salesService.findSalesByMenuId(Long.parseLong(params.get("menuId").toString()));
//        model.addAttribute("salesListall", salesListall);
//        System.out.println(salesListall);
//
//        // 총 판매량과 총 판매액 계산
//        int totalQuantity = 0;
//        double totalSalesAmount = 0.0;
//
//        for (Sales sale : salesListall) {
//            totalQuantity += sale.getQuantity();
//            totalSalesAmount += sale.getPrice() * sale.getQuantity();
//        }
//
//        // 총 판매액 포맷팅
//        DecimalFormat salesFormatter = new DecimalFormat("#,###");
//        String formattedTotalSalesAmount = salesFormatter.format(totalSalesAmount);
//
//        // 이익률 계산 및 포맷팅
//        double profitRate = 100.0 * (menu.getPrice() - menu.getCost()) / menu.getPrice();
//        DecimalFormat profitFormatter = new DecimalFormat("#0.0"); // 소수점 첫째 자리까지 포맷팅
//        String formattedProfitRate = profitFormatter.format(profitRate);
//
//        System.out.println("이익률은");
//        System.out.println(formattedProfitRate);
//
//        // 모델에 총 판매량과 포맷된 총 판매액 및 이익률 추가
//        model.addAttribute("totalQuantity", totalQuantity);
//        model.addAttribute("totalSalesAmount", formattedTotalSalesAmount);
//        model.addAttribute("profitRate", formattedProfitRate);
//
//        // process가 1인 판매량과 판매액 총합 계산
//        Map<String, Double> totalSalesAndQuantityByProcess = salesService.getTotalSalesAndQuantityByProcess(1);
//        String formattedTotalSalesAmountProcess1 = salesFormatter.format(totalSalesAndQuantityByProcess.get("totalSalesAmount"));
//        model.addAttribute("totalQuantityProcess1", totalSalesAndQuantityByProcess.get("totalQuantity").intValue());
//        model.addAttribute("totalSalesAmountProcess1", formattedTotalSalesAmountProcess1);
//
//        return "sales/salesAnalysisByMenuInput";
//    }


    @RequestMapping(value = "salesAnalysisByMenuInput", method = RequestMethod.GET)
    public String getSalesAnalysisByMenuInput(@RequestParam Map<String, Object> params, Model model) {
        // 메뉴 ID를 파싱
        Long menuId = Long.parseLong(params.get("menuId").toString());

        // 서비스 계층에서 데이터 처리
        Map<String, Object> salesData = salesService.calculateMenuSalesData(menuId);
        model.addAllAttributes(salesData);

        // process가 1인 판매량과 판매액 총합 계산
        Map<String, Double> totalSalesAndQuantityByProcess = salesService.getTotalSalesAndQuantityByProcess(1);
        double totalSalesAmountProcess1 = totalSalesAndQuantityByProcess.get("totalSalesAmount");
        double totalQuantityProcess1 = totalSalesAndQuantityByProcess.get("totalQuantity");

        model.addAttribute("totalQuantityProcess1", (int) totalQuantityProcess1);
        model.addAttribute("totalSalesAmountProcess1", totalSalesAmountProcess1);

        // 점유율 계산
        double totalSalesAmount = (double) salesData.get("totalSalesAmount");
        double totalQuantity = (double) salesData.get("totalQuantity");

        double salesPercentage = calculatePercentage(totalSalesAmount, totalSalesAmountProcess1);
        double quantityPercentage = calculatePercentage(totalQuantity, totalQuantityProcess1);

        DecimalFormat df = new DecimalFormat("#.##");
        model.addAttribute("salesPercentage", df.format(salesPercentage));
        model.addAttribute("quantityPercentage", df.format(quantityPercentage));
        model.addAttribute("totalQuantity", (long) totalQuantity); // 소수점 이하 절사
        model.addAttribute("totalSalesAmount", (long) totalSalesAmount); // 소수점 이하 절사

        // process가 1인 판매의 총 원가 계산
        double totalCostByProcess = salesService.calculateTotalCostByProcess(1);
        model.addAttribute("totalCostByProcess", totalCostByProcess);

        // 원가 점유율 계산
        Menu menu = (Menu) salesData.get("menu");
        double costPercentage = calculatePercentage(totalQuantity * menu.getCost(), totalCostByProcess);
        model.addAttribute("costPercentage", df.format(costPercentage));

        return "sales/salesAnalysisByMenuInput";
    }

    /**
     * 두 값을 기반으로 백분율을 계산합니다.
     * @param value 부분 값
     * @param total 전체 값
     * @return 백분율 값
     */
    private double calculatePercentage(double value, double total) {
        return total != 0 ? (value / total) * 100 : 0;
    }















    @GetMapping("/salesAnalysisDesignExample")
    public String getSalesAnalysisDesignExample() {
        return "sales/salesAnalysisDesignExample";
    }








}
