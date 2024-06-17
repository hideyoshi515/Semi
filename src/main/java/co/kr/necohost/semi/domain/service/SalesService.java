package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Category;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.repository.CategoryRepository;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.repository.SalesRepository;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalesService {
	private final SalesRepository salesRepository;
	private final CategoryRepository categoryRepository;
	MenuRepository menuRepository;

	public SalesService(SalesRepository salesRepository, MenuRepository menuRepository, CategoryRepository categoryRepository) {
		this.salesRepository = salesRepository;
		this.menuRepository = menuRepository;
		this.categoryRepository = categoryRepository;
	}

	// プロセスでフィルタリングされた販売リストを取得
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

	// メニューの販売データを計算
	public Map<String, Object> calculateMenuSalesData(Long menuId) {
		Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new RuntimeException("Menu not found"));
		List<Sales> salesList = salesRepository.findByMenu(menuId);

		int totalQuantity = 0;
		double totalSalesAmount = 0.0;

		// 総売上、総販売量を計算
		for (Sales sale : salesList) {
			totalQuantity += sale.getQuantity();
			totalSalesAmount += sale.getPrice() * sale.getQuantity();
		}

		DecimalFormat salesFormatter = new DecimalFormat("#,###");
		String formattedTotalSalesAmount = salesFormatter.format(totalSalesAmount);

		// 利益率を計算
		double profitRate = 100.0 * (menu.getPrice() - menu.getCost()) / menu.getPrice();
		DecimalFormat profitFormatter = new DecimalFormat("#0.0");
		String formattedProfitRate = profitFormatter.format(profitRate);

		Map<String, Object> result = new HashMap<>();
		result.put("menu", menu);
		result.put("salesListall", salesList);
		result.put("totalQuantity", totalQuantity);
		result.put("totalSalesAmount", formattedTotalSalesAmount);
		result.put("profitRate", formattedProfitRate);

		return result;
	}

	// 販売をIDで削除
	public void deleteById(SalesRequest salesRequest) {
		salesRepository.deleteById(salesRequest.getId());
	}

	// すべての販売を検索
	public List<Sales> findAll() {
		return salesRepository.findAll();
	}

	// プロセスで販売を検索
	public List<Sales> findByProcess(int process) {
		return salesRepository.findByProcess(process);
	}

	// IDで販売を検索
	public Sales findById(Long id) {
		return salesRepository.findById(id).orElse(null);
	}

	// 今日の販売を検索
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

	// プロセスでフィルタリングされた販売リストを取得
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

	// 範囲内の日付でメニューの販売数を取得
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

	// 範囲内の日付でメニューの販売数を取得
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

	// 範囲内の日付でメニューの販売を取得
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

	// 日毎の時間ごとの販売を取得
	public Map<LocalDateTime, Double> getHourlySalesByDay(LocalDateTime startOfDay, LocalDateTime endOfDay) {
		List<Sales> salesList = salesRepository.findSalesByDateRangeAndProcess(startOfDay, endOfDay);
		Map<LocalDateTime, Double> hourlySales = new TreeMap<>();

		for (Sales sales : salesList) {
			LocalDateTime hour = sales.getDate().withMinute(0).withSecond(0).withNano(0);
			hour = hour.withHour(hour.getHour());
			double salesAmount = sales.getPrice() * sales.getQuantity();
			hourlySales.put(hour, hourlySales.getOrDefault(hour, 0.0) + salesAmount);
		}

		return hourlySales;
	}

	// メニューIDで特定の日数後の販売数を取得
	public int getCountByMenuAfterDaysAgo(long menuId, int days) {
		return salesRepository.getCountByMenuAfterDaysAgo(menuId, days);
	}

	// 月ごとの販売成長率を取得
	public double getMonthlySalesGrowthRate(int year, int month) {
		// 現在の月の売上
		double currentMonthSales = getTotalSalesByYearAndMonth(year, month);

		// 前月の売上を計算
		int previousYear = month == 1 ? year - 1 : year;
		int previousMonth = month == 1 ? 12 : month - 1;
		double previousMonthSales = getTotalSalesByYearAndMonth(previousYear, previousMonth);

		// 前月比の売上成長率を計算
		if (previousMonthSales == 0) {
			return 0; // 前月の売上が0の場合、成長率を計算できない
		}
		return ((currentMonthSales - previousMonthSales) / previousMonthSales) * 100;
	}

	// 月ごとの売上を取得
	public Map<String, Double> getMonthlySalesByProcess() {
		List<Sales> salesList = salesRepository.findMonthlySalesByProcess();
		Map<String, Double> monthlySales = new TreeMap<>();

		// 初期値を設定
		LocalDate currentDate = LocalDate.now();
		for (int year = salesList.get(0).getDate().getYear(); year <= currentDate.getYear(); year++) {
			for (int month = 1; month <= 12; month++) {
				String key = String.format("%d-%02d", year, month);
				monthlySales.put(key, 0.0);
			}
		}

		// 売上データを更新
		for (Sales sales : salesList) {
			String key = String.format("%d-%02d", sales.getDate().getYear(), sales.getDate().getMonthValue());
			monthlySales.put(key, monthlySales.get(key) + sales.getPrice() * sales.getQuantity());
		}

		return monthlySales;
	}

	// (旧)年別とカテゴリ別の総販売額を計算
	public double getTotalSalesByYearAndCategory(int year, int category) {
		List<Sales> salesList = salesRepository.findSalesByYearAndCategory(year, category);
		return salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	// 範囲内の日付の総販売額を計算
	public double getTotalSalesByDay(int year, int month, int day) {
		List<Sales> salesList = salesRepository.findSalesByDayAndProcess(year, month, day);
		return salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	// 年別と月別の総販売額を計算
	public double getTotalSalesByYearAndMonth(int year, int month) {
		List<Sales> salesList = salesRepository.findSalesByYearAndMonthAndProcess(year, month);
		return salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	// 年別の総販売額を計算
	public double getTotalSalesByYear(int year) {
		List<Sales> salesList = salesRepository.findSalesByYearAndProcess(year);
		return salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	// カテゴリ別の総販売額を取得
	public Map<String, Double> getTotalSalesByCategory() {
		List<Sales> salesList = salesRepository.findAllSales();
		List<Category> categoryList = categoryRepository.findAll();

		// カテゴリIDと名前をマッピング
		Map<Integer, String> categoryMap = categoryList.stream()
				.collect(Collectors.toMap(category -> Math.toIntExact(category.getId()), Category::getName));

		// 販売データをカテゴリ名でグループ化
		return salesList.stream()
				.collect(Collectors.groupingBy(
						sales -> categoryMap.get(sales.getCategory()),
						Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
				));
	}

	// メニュー別の総販売額を取得
	public Map<String, Double> getTotalSalesByMenu() {
		List<Sales> salesList = salesRepository.findAllSales();
		List<Menu> menuList = menuRepository.findAll();

		// メニューIDと名前をマッピング
		Map<Integer, String> menuMap = menuList.stream()
				.collect(Collectors.toMap(menu -> Math.toIntExact(menu.getId()), Menu::getName));

		// menuMapに存在しないキーを持つ販売項目をフィルタリング
		return salesList.stream()
				.filter(sales -> menuMap.containsKey(sales.getMenu()))
				.collect(Collectors.groupingBy(
						sales -> menuMap.get(sales.getMenu()),
						Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
				));
	}

	// 総販売と数量をプロセス別に取得
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

	// 注文を保存
	public void save(SalesRequest salesRequest) {
		salesRepository.save(salesRequest.toEntity());
	}

	// 注文を保存
	public void save2(SalesRequest salesRequest) {
		Sales sales = salesRequest.toEntity();
		salesRepository.save(sales);
	}

	// 入力された日付の週ごとの販売を取得
	public Map<LocalDate, Double> getWeeklySalesByDay(int year, int month, int day) {
		LocalDate date = LocalDate.of(year, month, day);
		LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
		LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

		// クエリのためにLocalDateTimeに調整
		LocalDateTime startOfWeekDateTime = startOfWeek.atStartOfDay();
		LocalDateTime endOfWeekDateTime = endOfWeek.atTime(23, 59, 59);

		List<Sales> salesList = salesRepository.findSalesByDateRange(startOfWeekDateTime, endOfWeekDateTime);
		Map<LocalDate, Double> weeklySales = new TreeMap<>();

		for (Sales sales : salesList) {
			LocalDate salesDate = sales.getDate().toLocalDate(); // LocalDateTimeをLocalDateに変換
			double salesAmount = sales.getPrice() * sales.getQuantity();
			weeklySales.put(salesDate, weeklySales.getOrDefault(salesDate, 0.0) + salesAmount);
		}

		return weeklySales;
	}

	// 現在までの総販売額を取得
	public double getTotalSalesUntilNow(LocalDateTime now) {
		LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
		List<Sales> salesList = salesRepository.findSalesByDateRangeAndProcess(startOfDay, now);
		return salesList.stream()
				.mapToDouble(s -> s.getPrice() * s.getQuantity())
				.sum();
	}

	// プロセス別に年間の総販売額を取得
	public Map<Integer, Double> getYearlySalesByProcess() {
		List<Sales> salesList = salesRepository.findYearlySalesByProcess();
		return salesList.stream()
				.collect(Collectors.groupingBy(
						s -> s.getDate().atZone(java.time.ZoneId.systemDefault()).getYear(),
						Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
				));
	}
}
