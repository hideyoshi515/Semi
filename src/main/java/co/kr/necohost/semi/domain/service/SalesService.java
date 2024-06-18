package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Category;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.repository.CategoryRepository;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.repository.SalesRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@EnableAsync
public class SalesService {
	private final SalesRepository salesRepository;
	private final CategoryRepository categoryRepository;
	private final MenuRepository menuRepository;

	public SalesService(SalesRepository salesRepository, MenuRepository menuRepository, CategoryRepository categoryRepository) {
		this.salesRepository = salesRepository;
		this.menuRepository = menuRepository;
		this.categoryRepository = categoryRepository;
	}

	// 新しい販売記録を保存
	public void save(SalesRequest salesRequest) {
		salesRepository.save(salesRequest.toEntity());
	}

	// 注文を保存
	public void save2(SalesRequest salesRequest) {
		Sales sales = salesRequest.toEntity();
		salesRepository.save(sales);
	}

	// すべての販売記録を検索
	public List<Sales> findAll() {
		return salesRepository.findAll();
	}

	// IDで販売記録を検索
	public Sales findById(Long id) {
		return salesRepository.findById(id).orElse(null);
	}

	// IDで販売記録を削除
	public void deleteById(SalesRequest salesRequest) {
		salesRepository.deleteById(salesRequest.getId());
	}

	public List<Sales> getAllSalesForMenu(Long menuId) {
		return salesRepository.findAllByMenu(menuId);
	}


	// 時間別の販売額を計算（管理者ページ - ホームページ）
	public Map<LocalDateTime, Double> getHourlySalesByDay(LocalDateTime startOfDay, LocalDateTime endOfDay) {
		List<Sales> salesList = salesRepository.findSalesByDateRangeAndProcess(startOfDay, endOfDay);
		Map<LocalDateTime, Double> hourlySales = new TreeMap<>();

		for (Sales sales : salesList) {
			LocalDateTime hour = sales.getDate().withMinute(0).withSecond(0).withNano(0);
			double salesAmount = sales.getPrice() * sales.getQuantity();
			hourlySales.merge(hour, salesAmount, Double::sum);
		}

		return hourlySales;
	}

	// 今日の現在時刻までの販売額を計算（管理者ページ - ホームページ）
	public double getTotalSalesUntilNow(LocalDateTime now) {
		LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
		List<Sales> salesList = salesRepository.findSalesByDateRangeAndProcess(startOfDay, now);
		return salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	// 時間別の累積売上データをフォーマット（管理者ページ - ホームページ）
	public Map<String, Double> getFormattedHourlySales(Map<LocalDateTime, Double> hourlySales) {
		Map<String, Double> formattedHourlySales = new TreeMap<>();
		DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("HH:mm");
		double cumulativeSales = 0.0;

		for (Map.Entry<LocalDateTime, Double> entry : hourlySales.entrySet()) {
			cumulativeSales += entry.getValue();
			formattedHourlySales.put(entry.getKey().format(hourFormatter), cumulativeSales);
		}

		return formattedHourlySales;
	}

	// 今日の現在時刻までのフォーマットされた総売上を返す（管理者ページ - ホームページ）
	public String getFormattedTotalSalesUntilNow(LocalDateTime now) {
		double totalSalesToday = getTotalSalesUntilNow(now);
		NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
		return numberFormat.format(totalSalesToday);
	}

	// プロセス別の年間総売上を計算（1つ目のボタン 年度別総売上高）
	public Map<Integer, Double> getYearlySalesByProcess() {
		List<Sales> salesList = salesRepository.findYearlySalesByProcess();
		return salesList.stream()
				.collect(Collectors.groupingBy(
						s -> s.getDate().atZone(java.time.ZoneId.systemDefault()).getYear(),
						Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
				));
	}

	// 年別の売上データをフォーマット（1つ目のボタン 年度別総売上高）
	public Map<String, Double> getFormattedYearlySales(Map<Integer, Double> yearlySales) {
		return yearlySales.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(
						entry -> entry.getKey().toString(),
						Map.Entry::getValue,
						(e1, e2) -> e1,
						LinkedHashMap::new
				));
	}

	// 月別の総売上を返すメソッド（2つ目のボタン 月別総売上高）
	public Map<String, Double> getMonthlySalesByProcess() {
		List<Sales> salesList = salesRepository.findMonthlySalesByProcess();
		Map<String, Double> monthlySales = new TreeMap<>();

		LocalDate currentDate = LocalDate.now();
		for (int year = salesList.get(0).getDate().getYear(); year <= currentDate.getYear(); year++) {
			for (int month = 1; month <= 12; month++) {
				String key = String.format("%d-%02d", year, month);
				monthlySales.put(key, 0.0);
			}
		}

		for (Sales sales : salesList) {
			String key = String.format("%d-%02d", sales.getDate().getYear(), sales.getDate().getMonthValue());
			monthlySales.put(key, monthlySales.get(key) + sales.getPrice() * sales.getQuantity());
		}

		return monthlySales;
	}

	// 指定された日付（年-月日）の総売上を計算（3つ目のボタン 日商/週間売上）
	public double getTotalSalesByDay(int year, int month, int day) {
		List<Sales> salesList = salesRepository.findSalesByDayAndProcess(year, month, day);
		return salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	// 指定された日付の週の総売上と曜日別売上を返すメソッド（3つ目のボタン 日商/週間売上）
	public Map<LocalDate, Double> getWeeklySalesByDay(int year, int month, int day) {
		LocalDate date = LocalDate.of(year, month, day);
		LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
		LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

		LocalDateTime startOfWeekDateTime = startOfWeek.atStartOfDay();
		LocalDateTime endOfWeekDateTime = endOfWeek.atTime(23, 59, 59);

		List<Sales> salesList = salesRepository.findSalesByDateRange(startOfWeekDateTime, endOfWeekDateTime);
		Map<LocalDate, Double> weeklySales = new TreeMap<>();

		for (Sales sales : salesList) {
			LocalDate salesDate = sales.getDate().toLocalDate();
			double salesAmount = sales.getPrice() * sales.getQuantity();
			weeklySales.put(salesDate, weeklySales.getOrDefault(salesDate, 0.0) + salesAmount);
		}

		return weeklySales;
	}

	// カテゴリとプロセスで総売上を計算
	public int getTotalSalesByCategory(int categoryId, int process) {
		List<Sales> allSales = salesRepository.findByCategoryAndProcess(categoryId, process);

		int totalSales = 0;

		for (Sales sale : allSales) {
			totalSales += sale.getPrice() * sale.getQuantity();
		}

		return totalSales;
	}

	// プロセスで販売記録を検索
	public List<Sales> findByProcess(int process) {
		return salesRepository.findByProcess(process);
	}

	// 今日の販売記録を検索
	public Map<String, Long> findSalesByToday() {
		LocalDate localDate = LocalDate.now();
		List<Sales> salesList = salesRepository.findSalesByToday(localDate);
		List<Menu> menuList = menuRepository.findAll();
		Map<Long, String> menuMap = menuList.stream()
				.collect(Collectors.toMap(Menu::getId, Menu::getName));
		return salesList.stream()
				.collect(Collectors.groupingBy(
						sales -> menuMap.get(Long.parseLong(String.valueOf(sales.getMenu()))),
						Collectors.counting()
				));
	}

	// カテゴリ別の販売額を計算
	public Map<String, Double> getTotalSalesByCategory() {
		List<Sales> salesList = salesRepository.findAllSales();
		List<Category> categoryList = categoryRepository.findAll();

		Map<Integer, String> categoryMap = categoryList.stream()
				.collect(Collectors.toMap(category -> Math.toIntExact(category.getId()), Category::getName));

		return salesList.stream()
				.collect(Collectors.groupingBy(
						sales -> categoryMap.get(sales.getCategory()),
						Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
				));
	}

	// メニュー別の販売額を計算
	public Map<String, Double> getTotalSalesByMenu() {
		List<Sales> salesList = salesRepository.findAllSales();
		List<Menu> menuList = menuRepository.findAll();

		Map<Integer, String> menuMap = menuList.stream()
				.collect(Collectors.toMap(menu -> Math.toIntExact(menu.getId()), Menu::getName));

		return salesList.stream()
				.filter(sales -> menuMap.containsKey(sales.getMenu()))
				.collect(Collectors.groupingBy(
						sales -> menuMap.get(sales.getMenu()),
						Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
				));
	}

	// 年間の総販売量を計算
	public double getTotalSalesByYear(int year) {
		List<Sales> salesList = salesRepository.findSalesByYearAndProcess(year);
		return salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	// 年別と月別の総販売量を計算
	public double getTotalSalesByYearAndMonth(int year, int month) {
		List<Sales> salesList = salesRepository.findSalesByYearAndMonthAndProcess(year, month);
		return salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	// メニューIDで指定された日数後の販売数を取得
	public int getCountByMenuAfterDaysAgo(long menuId, int days) {
		return salesRepository.getCountByMenuAfterDaysAgo(menuId, days);
	}

	// 現在の月と前月の売上を計算し、前月比の売上成長率を計算
	public double getMonthlySalesGrowthRate(int year, int month) {
		double currentMonthSales = getTotalSalesByYearAndMonth(year, month);

		int previousYear = month == 1 ? year - 1 : year;
		int previousMonth = month == 1 ? 12 : month - 1;
		double previousMonthSales = getTotalSalesByYearAndMonth(previousYear, previousMonth);

		if (previousMonthSales == 0) {
			return 0;
		}
		return ((currentMonthSales - previousMonthSales) / previousMonthSales) * 100;
	}

	// 年別とカテゴリ別の総販売額を計算
	public double getTotalSalesByYearAndCategory(int year, int category) {
		List<Sales> salesList = salesRepository.findSalesByYearAndCategory(year, category);
		return salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	// 指定された日付範囲のカテゴリ別売上を取得（4つ目のボタン カテゴリ別売上販売の現状）
	public Map<String, Double> getSalesByCategoryInRange(LocalDateTime startDate, LocalDateTime endDate) {
		List<Sales> salesList = salesRepository.findSalesByDateRangeWithProcess(startDate, endDate);
		List<Category> categoryList = categoryRepository.findAll();

		Map<Long, String> categoryMap = categoryList.stream()
				.collect(Collectors.toMap(Category::getId, Category::getName));

		return salesList.stream()
				.filter(sales -> categoryMap.containsKey(sales.getCategory()))
				.collect(Collectors.groupingBy(
						sales -> categoryMap.get(sales.getCategory()),
						Collectors.summingDouble(sales -> sales.getPrice() * sales.getQuantity())
				));
	}

	// 指定された日付範囲のカテゴリ別販売量を取得（4つ目のボタン カテゴリ別売上販売の現状）
	public Map<String, Integer> getQuantityByCategoryInRange(LocalDateTime startDate, LocalDateTime endDate) {
		List<Sales> salesList = salesRepository.findSalesByDateRangeWithProcess(startDate, endDate);
		List<Category> categoryList = categoryRepository.findAll();

		Map<Long, String> categoryMap = categoryList.stream()
				.collect(Collectors.toMap(Category::getId, Category::getName));

		return salesList.stream()
				.filter(sales -> categoryMap.containsKey(sales.getCategory()))
				.collect(Collectors.groupingBy(
						sales -> categoryMap.get(sales.getCategory()),
						Collectors.summingInt(Sales::getQuantity)
				));
	}

	// 指定された日付範囲のメニュー別売上を取得（5つ目のボタン メニュー別売上販売の現状）
	public Map<String, Double> getSalesByMenuInRange(LocalDateTime startDate, LocalDateTime endDate) {
		List<Sales> salesList = salesRepository.findSalesByDateRangeWithProcess(startDate, endDate);
		List<Menu> menuList = menuRepository.findAll();

		Map<Long, String> menuMap = menuList.stream()
				.collect(Collectors.toMap(Menu::getId, Menu::getName));

		return salesList.stream()
				.filter(sales -> menuMap.containsKey(sales.getMenu()))
				.collect(Collectors.groupingBy(
						sales -> menuMap.get(sales.getMenu()),
						Collectors.summingDouble(sales -> sales.getPrice() * sales.getQuantity())
				));
	}

	// 指定された日付範囲のメニュー別販売量を取得（5つ目のボタン メニュー別売上販売の現状）
	public Map<String, Integer> getQuantityByMenuInRange(LocalDateTime startDate, LocalDateTime endDate) {
		List<Sales> salesList = salesRepository.findSalesByDateRangeWithProcess(startDate, endDate);
		List<Menu> menuList = menuRepository.findAll();

		Map<Long, String> menuMap = menuList.stream()
				.collect(Collectors.toMap(Menu::getId, Menu::getName));

		return salesList.stream()
				.filter(sales -> menuMap.containsKey(sales.getMenu()))
				.collect(Collectors.groupingBy(
						sales -> menuMap.get(sales.getMenu()),
						Collectors.summingInt(Sales::getQuantity)
				));
	}

	// メニューIDで販売データを検索
	public List<Sales> findSalesByMenuId(Long menuId) {
		return salesRepository.findByMenu(menuId);
	}

	public Map<String, Double> getTimePeriodSalesAndQuantity() {
		LocalDateTime now = LocalDateTime.now();

		Map<String, Double> result = new HashMap<>();
		result.put("totalSalesAmountToday", getTotalSalesAndQuantityToday().get("totalSalesAmount"));
		result.put("totalQuantityToday", getTotalSalesAndQuantityToday().get("totalQuantity"));

		LocalDateTime startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).toLocalDate().atStartOfDay();
		LocalDateTime endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).toLocalDate().atTime(23, 59, 59);
		result.put("totalSalesAmountWeek", getTotalSalesAndQuantityByDateRange(startOfWeek, endOfWeek).get("totalSalesAmount"));
		result.put("totalQuantityWeek", getTotalSalesAndQuantityByDateRange(startOfWeek, endOfWeek).get("totalQuantity"));

		LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
		LocalDateTime endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atTime(23, 59, 59);
		result.put("totalSalesAmountMonth", getTotalSalesAndQuantityByDateRange(startOfMonth, endOfMonth).get("totalSalesAmount"));
		result.put("totalQuantityMonth", getTotalSalesAndQuantityByDateRange(startOfMonth, endOfMonth).get("totalQuantity"));

		LocalDateTime startOfQuarter = now.with(now.getMonth().firstMonthOfQuarter()).withDayOfMonth(1).toLocalDate().atStartOfDay();
		LocalDateTime endOfQuarter = now.with(now.getMonth().firstMonthOfQuarter().plus(2)).with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atTime(23, 59, 59);
		result.put("totalSalesAmountQuarter", getTotalSalesAndQuantityByDateRange(startOfQuarter, endOfQuarter).get("totalSalesAmount"));
		result.put("totalQuantityQuarter", getTotalSalesAndQuantityByDateRange(startOfQuarter, endOfQuarter).get("totalQuantity"));

		LocalDateTime startOfYear = now.withDayOfYear(1).toLocalDate().atStartOfDay();
		LocalDateTime endOfYear = now.with(TemporalAdjusters.lastDayOfYear()).toLocalDate().atTime(23, 59, 59);
		result.put("totalSalesAmountYear", getTotalSalesAndQuantityByDateRange(startOfYear, endOfYear).get("totalSalesAmount"));
		result.put("totalQuantityYear", getTotalSalesAndQuantityByDateRange(startOfYear, endOfYear).get("totalQuantity"));

		return result;
	}


	// プロセスが1の（販売完了）売上量と売上額を計算
	public Map<String, Double> getTotalSalesAndQuantityByProcess(int process) {
		List<Sales> salesList = findByProcess(process).stream()
				.filter(s -> s.getProcess() == 1)
				.collect(Collectors.toList());

		double totalSalesAmount = salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
		int totalQuantity = salesList.stream()
				.mapToInt(Sales::getQuantity)
				.sum();

		Map<String, Double> result = new HashMap<>();
		result.put("totalSalesAmount", totalSalesAmount);
		result.put("totalQuantity", Double.valueOf(totalQuantity));
		return result;
	}

	// プロセスが1の（販売完了）原価の総額を計算
	public double calculateTotalCostByProcess(int process) {
		List<Sales> salesList = findByProcess(process).stream()
				.filter(s -> s.getProcess() == 1)
				.collect(Collectors.toList());

		double totalCost = 0.0;

		for (Sales sale : salesList) {
			Menu menu = menuRepository.findById(sale.getMenu()).orElseThrow(() -> new RuntimeException("Menu not found"));
			totalCost += sale.getQuantity() * menu.getCost();
		}

		return totalCost;
	}

	// 特定メニューの売上データ
	public Map<String, Object> calculateMenuSalesData(Long menuId) {
		Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new RuntimeException("Menu not found"));
		List<Sales> salesList = salesRepository.findByMenu(menuId);

		int totalQuantity = 0;
		double totalSalesAmount = 0.0;

		// 総売上と総販売量を計算
		for (Sales sale : salesList) {
			totalQuantity += sale.getQuantity();
			totalSalesAmount += sale.getPrice() * sale.getQuantity();
		}

		// 利益率を計算
		double rawProfitRate = 100.0 * (menu.getPrice() - menu.getCost()) / menu.getPrice();
		BigDecimal profitRate = new BigDecimal(rawProfitRate).setScale(2, RoundingMode.HALF_UP);

		Map<String, Object> result = new HashMap<>();
		result.put("menu", menu);
		result.put("salesListall", salesList);
		result.put("totalQuantity", totalQuantity);
		result.put("totalSalesAmount", totalSalesAmount); // Double型として維持
		result.put("profitRate", profitRate);

		return result;
	}

	// 今日の総販売量と販売額を計算
	public Map<String, Double> getTotalSalesAndQuantityToday() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();

		return getTotalSalesAndQuantityByDateRange(startOfToday, now);
	}

	// 今週の総販売量と販売額を計算
	public Map<String, Double> getTotalSalesAndQuantityThisWeek() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).toLocalDate().atStartOfDay();
		LocalDateTime endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).toLocalDate().atTime(23, 59, 59);

		return getTotalSalesAndQuantityByDateRange(startOfWeek, endOfWeek);
	}

	// 今月の総販売量と販売額を計算
	public Map<String, Double> getTotalSalesAndQuantityThisMonth() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

		return getTotalSalesAndQuantityByDateRange(startOfMonth, now);
	}

	// 今四半期の総販売量と販売額を計算
	public Map<String, Double> getTotalSalesAndQuantityThisQuarter() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime startOfQuarter = now.with(now.getMonth().firstMonthOfQuarter()).withDayOfMonth(1).toLocalDate().atStartOfDay();

		return getTotalSalesAndQuantityByDateRange(startOfQuarter, now);
	}

	// 今年の総販売量と販売額を計算
	public Map<String, Double> getTotalSalesAndQuantityThisYear() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime startOfYear = now.withDayOfYear(1).toLocalDate().atStartOfDay();

		return getTotalSalesAndQuantityByDateRange(startOfYear, now);
	}

	// 日付範囲に基づく総販売量と販売額を計算
	public Map<String, Double> getTotalSalesAndQuantityByDateRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
		List<Sales> salesList = salesRepository.findSalesByDateRangeWithProcess(startDateTime, endDateTime);
		double totalSalesAmount = salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
		int totalQuantity = salesList.stream()
				.mapToInt(Sales::getQuantity)
				.sum();

		Map<String, Double> result = new HashMap<>();
		result.put("totalSalesAmount", totalSalesAmount);
		result.put("totalQuantity", Double.valueOf(totalQuantity));
		return result;
	}

	// 日付範囲に基づく総売上を計算
	public double getTotalSalesByDateRange(LocalDateTime startDateTime, LocalDateTime endDateTime, Long menuId) {
		List<Sales> salesList = salesRepository.findSalesByDateRangeWithProcess(startDateTime, endDateTime);
		return salesList.stream()
				.filter(s -> s.getMenu() == menuId)
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	// 日付範囲に基づく総販売量を計算
	public int getTotalQuantityByDateRange(LocalDateTime startDateTime, LocalDateTime endDateTime, Long menuId) {
		List<Sales> salesList = salesRepository.findSalesByDateRangeWithProcess(startDateTime, endDateTime);
		return salesList.stream()
				.filter(s -> s.getMenu() == menuId)
				.mapToInt(Sales::getQuantity)
				.sum();
	}
}
