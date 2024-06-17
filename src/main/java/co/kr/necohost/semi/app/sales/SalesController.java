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

    public SalesController(MenuService menuService, SalesService salesService, MenuRepository menuRepository) {
        this.salesService = salesService;
        this.menuService = menuService;
        this.menuRepository = menuRepository;
    }

    // 古いバージョンの管理者販売メニューページを返すメソッド(cssなし)
    @RequestMapping(value = "/adminSalesMenu", method = RequestMethod.GET)
    public String getOldAdminSalesMenu() {
        return "/sales/adminSalesMenu.html";
    }

    // 新しいバージョンの管理者ページ - 販売管理ページを返すメソッド(cssあり)
    @RequestMapping(value = "/adminSalesMainMenu", method = RequestMethod.GET)
    public String getAdminSalesMainMenu() {
        return "/sales/adminSalesMainMenu.html";
    }

    // 管理者ページ - ホームページ（今日の現在の時刻までの売上）を返すメソッド(cssあり)
    @RequestMapping(value = "/adminSalesMainHome", method = RequestMethod.GET)
    public String getAdminSalesMainHome(Model model) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);
        model.addAttribute("currentTime", formattedNow);

        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59);

        // サービスから時間ごとの売上データを取得
        Map<LocalDateTime, Double> hourlySales = salesService.getHourlySalesByDay(startOfDay, endOfDay);

        // 累積売上計算およびフォーマットされた時間データを格納するMapを生成
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

        model.addAttribute("hourlySales", formattedHourlySales); // フォーマットされた時間データを渡す
        return "sales/adminSalesMainHome";
    }

    // 管理者ページ - ホームページの別バージョンを返すメソッド
    @RequestMapping(value = "/adminSalesMainHome2", method = RequestMethod.GET)
    public String getAdmininSalesMainHome2(Model model) {
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

    // 販売データベースコントローラーメニューページを返すメソッド
    @RequestMapping(value = "/salesDataBaseControllerMenu", method = RequestMethod.GET)
    public String getSalesDataBaseControllerMenu() {
        return "/sales/salesDataBaseControllerMenu.html";
    }

    // 販売作成ページを返すメソッド (GETリクエスト)
    @RequestMapping(value = "/createSales", method = RequestMethod.GET)
    public String getCreateSales(Model model) {
        model.addAttribute("salesRequest", new SalesRequest());
        return "/sales/createSales.html";
    }

    // 新しい販売を作成するメソッド (POSTリクエスト)
    @RequestMapping(value = "/createSales", method = RequestMethod.POST)
    public String postCreateSales(Model model, @ModelAttribute("salesRequest") SalesRequest salesRequest) {
        salesService.save(salesRequest);
        return "/sales/createSales.html";
    }

    // 販売データを読み込むメソッド
    @RequestMapping(value = "/readSales", method = RequestMethod.GET)
    public String getReadSales(Model model) {
        List<Sales> sales = salesService.findByProcess(1);
        model.addAttribute("sales", sales);
        model.addAttribute("salesRequest", new SalesRequest());
        return "/sales/readSales.html";
    }

    // 各カテゴリー別の総販売額を返すメソッド
    @RequestMapping(value = "/totalSalesbyCategory", method = RequestMethod.GET)
    public String getTotalSalesByCategory(Model model) {
        Map<String, Double> totalByCategory = salesService.getTotalSalesByCategory();
        model.addAttribute("totalByCategory", totalByCategory);
        return "/sales/totalSalesByCategory.html";
    }

    // メニュー別販売額を期間検索して返すメソッド (入力ページ)
    @RequestMapping(value = "/totalSalesbyMenuAndPeriodInput", method = RequestMethod.GET)
    public String getTotalSalesByMenuAndPeriodInput(Model model) {
        return "/sales/totalSalesbyMenuAndPeriodInput";
    }

    // メニュー別販売額を期間検索して返すメソッド (検索結果)
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

    // カテゴリー別販売額を期間検索して返すメソッド (入力ページ)
    @RequestMapping(value = "/totalSalesbyCategoryAndPeriodInput", method = RequestMethod.GET)
    public String getTotalSalesByCategoryAndPeriodInput(Model model) {
        return "/sales/totalSalesbyCategoryAndPeriodInput";
    }

    // カテゴリー別販売額を期間検索して返すメソッド (検索結果)
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

    // 年度別の総販売額を返すメソッド
    @RequestMapping(value = "/totalSalesByYear", method = RequestMethod.GET)
    public String getTotalSalesByYear(Model model) {
        Map<Integer, Double> yearlySales = salesService.getYearlySalesByProcess();

        // 年度別の昇順でソート
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

    // 月別の総販売額を返すメソッド
    @RequestMapping(value = "/totalSalesByMonth", method = RequestMethod.GET)
    public String getTotalSalesByMonth(Model model) {
        Map<String, Double> monthlySales = salesService.getMonthlySalesByProcess();
        model.addAttribute("monthlySales", monthlySales);
        return "sales/totalSalesByMonth";
    }

    // 入力された年度の総販売額を返すメソッド
    @RequestMapping(value = "/totalSalesByYearInput", method = RequestMethod.GET)
    public String getTotalSalesByYearInput(@RequestParam(value = "year", required = false) Integer year, Model model) {
        if (year != null) {
            double totalSales = salesService.getTotalSalesByYear(year);
            model.addAttribute("year", year);
            model.addAttribute("totalSales", totalSales);
        }
        return "/sales/totalSalesByYearInput";
    }

    // 入力された年度と月の総販売額および成長率を返すメソッド
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

    // 入力された日付（年-月-日）の総販売額を返すメソッド
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

    // 入力された日付（年-月-日）が含まれる週の週別売上（1週間の総売上、曜日別売上）を返すメソッド
    @RequestMapping(value = "/totalWeeklySalesByDayInput", method = RequestMethod.GET)
    public String getTotalWeeklySalesByDay(@RequestParam(value = "year", required = false) Integer year,
                                           @RequestParam(value = "month", required = false) Integer month,
                                           @RequestParam(value = "day", required = false) Integer day,
                                           Model model) {
        if (year != null && month != null && day != null) {
            Map<LocalDate, Double> weeklySales = salesService.getWeeklySalesByDay(year, month, day);
            double totalWeeklySales = weeklySales.values().stream().mapToDouble(Double::doubleValue).sum();

            // 売上金額のフォーマット指定
            DecimalFormat decimalFormat = new DecimalFormat("#,###");

            // 千単位のカンマ追加および小数点以下の省略
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

    // 入力された年度とカテゴリーの総販売額を返すメソッド
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

    // メニューごとの販売分析ページを返すメソッド
    @RequestMapping(value = "salesAnalysisByMenuInput", method = RequestMethod.GET)
    public String getSalesAnalysisByMenuInput(@RequestParam Map<String, Object> params, Model model) {
        // メニューIDをパース
        Long menuId = Long.parseLong(params.get("menuId").toString());

        // サービス層でデータ処理
        Map<String, Object> salesData = salesService.calculateMenuSalesData(menuId);
        model.addAllAttributes(salesData);

        // processが1の販売量と販売額の総合計を計算
        Map<String, Double> totalSalesAndQuantityByProcess = salesService.getTotalSalesAndQuantityByProcess(1);
        double totalSalesAmountProcess1 = totalSalesAndQuantityByProcess.get("totalSalesAmount");
        double totalQuantityProcess1 = totalSalesAndQuantityByProcess.get("totalQuantity");

        model.addAttribute("totalQuantityProcess1", (int) totalQuantityProcess1);
        model.addAttribute("totalSalesAmountProcess1", totalSalesAmountProcess1);

        // シェア率計算
        double totalSalesAmount = (double) salesData.get("totalSalesAmount");
        double totalQuantity = (double) salesData.get("totalQuantity");

        double salesPercentage = calculatePercentage(totalSalesAmount, totalSalesAmountProcess1);
        double quantityPercentage = calculatePercentage(totalQuantity, totalQuantityProcess1);

        DecimalFormat df = new DecimalFormat("#.##");
        model.addAttribute("salesPercentage", df.format(salesPercentage));
        model.addAttribute("quantityPercentage", df.format(quantityPercentage));
        model.addAttribute("totalQuantity", (long) totalQuantity); // 小数点以下切捨て
        model.addAttribute("totalSalesAmount", (long) totalSalesAmount); // 小数点以下切捨て

        // processが1の販売の総コスト計算
        double totalCostByProcess = salesService.calculateTotalCostByProcess(1);
        model.addAttribute("totalCostByProcess", totalCostByProcess);

        // コストシェア率計算
        Menu menu = (Menu) salesData.get("menu");
        double costPercentage = calculatePercentage(totalQuantity * menu.getCost(), totalCostByProcess);
        model.addAttribute("costPercentage", df.format(costPercentage));

        return "sales/salesAnalysisByMenuInput";
    }

    // 二つの値に基づいてパーセンテージを計算するメソッド
    private double calculatePercentage(double value, double total) {
        return total != 0 ? (value / total) * 100 : 0;
    }

    // 販売分析デザイン例ページを返すメソッド
    @GetMapping("/salesAnalysisDesignExample")
    public String getSalesAnalysisDesignExample() {
        return "sales/salesAnalysisDesignExample";
    }
}
