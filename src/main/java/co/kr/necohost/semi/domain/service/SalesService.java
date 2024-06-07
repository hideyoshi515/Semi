package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.repository.SalesRepository;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalesService {
    private final SalesRepository salesRepository;
    MenuRepository menuRepository;

    public SalesService(SalesRepository salesRepository, MenuRepository menuRepository) {
        this.salesRepository = salesRepository;
        this.menuRepository = menuRepository;
    }

    // 새로운 판매 기록을 저장
    public void save(SalesRequest salesRequest) {
        salesRepository.save(salesRequest.toEntity());
    }

    // 모든 판매 기록을 조회
    public List<Sales> findAll() {
        return salesRepository.findAll();
    }

    // ID로 판매 기록을 조회
    public Sales findById(Long id) {
        return salesRepository.findById(id).orElse(null);
    }

    // 판매 기록을 ID로 삭제
    public void deleteById(SalesRequest salesRequest) {
        salesRepository.deleteById(salesRequest.getId());
    }

    // 카테고리와 프로세스로 총 판매량을 계산
    public int getTotalSalesByCategory(int categoryId, int process) {
        List<Sales> allSales = salesRepository.findByCategoryAndProcess(categoryId, process);

        int totalSales = 0;

        for (Sales sale : allSales) {
            totalSales += sale.getPrice() * sale.getQuantity();
        }

        return totalSales;
    }
    // 프로세스로 판매 기록을 조회
    public List<Sales> findByProcess(int process) {
        return salesRepository.findByProcess(process);
    }

    // 오늘 날짜의 판매 기록을 조회
    public Map<String, Long> findSalesByToday() {

        LocalDate localDate = LocalDate.now();
        System.out.println(localDate);
        List<Sales> salesList = salesRepository.findSalesByToday(localDate);
        List<Menu> menuList = menuRepository.findAll();
        Map<Long, String> menuMap = menuList.stream()
                .collect(Collectors.toMap(Menu::getId, Menu::getName));
        Map<String, Long> result = salesList.stream()
                .collect(Collectors.groupingBy(
                        sales -> menuMap.get(Long.parseLong(String.valueOf(sales.getMenu()))),
                        Collectors.counting()
                ));
        return result;
    }

    // 프로세스로 연간 총 판매량을 계산
    public Map<Integer, Double> getYearlySalesByProcess() {
        List<Sales> salesList = salesRepository.findYearlySalesByProcess();
        return salesList.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDate().atZone(java.time.ZoneId.systemDefault()).getYear(),
                        Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
                ));
    }
    // 카테고리별 총 판매량을 계산
    public Map<Integer, Double> getTotalSalesByCategory() {
        List<Sales> salesList = salesRepository.findAllSales();
        return salesList.stream()
                .collect(Collectors.groupingBy(
                        Sales::getCategory,
                        Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
                ));
    }

    // 각 메뉴별 총 판매액을 계산. 6월 7일 오후 5시 작업중
    public Map<Integer, Double> getTotalSalesByMenu() {
        List<Sales> salesList = salesRepository.findAllSales();
        return salesList.stream()
                .collect(Collectors.groupingBy(
                        Sales::getMenu,
                        Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
                ));
    }



    // 연도별 총 판매량을 계산
    public double getTotalSalesByYear(int year) {
        List<Sales> salesList = salesRepository.findSalesByYearAndProcess(year);
        return salesList.stream()
                .mapToDouble(s -> s.getPrice() * s.getQuantity())
                .sum();
    }


    // 연도와 월별 총 판매량을 계산
    public double getTotalSalesByYearAndMonth(int year, int month) {
        List<Sales> salesList = salesRepository.findSalesByYearAndMonthAndProcess(year, month);
        return salesList.stream()
                .mapToDouble(s -> s.getPrice() * s.getQuantity())
                .sum();
    }

    // 입력된 날짜(연-월일)의  총 판매량을 계산
    public double getTotalSalesByDay(int year, int month, int day) {
        List<Sales> salesList = salesRepository.findSalesByDayAndProcess(year, month, day);
        return salesList.stream()
                .mapToDouble(s -> s.getPrice() * s.getQuantity())
                .sum();
    }

    //입력된 날짜(연-월-일)가 속한 주의 주별 매출(1주간 총매출, 요일별 매출)을 반환하는 메서드
    public Map<LocalDate, Double> getWeeklySalesByDay(int year, int month, int day) {
        LocalDate date = LocalDate.of(year, month, day);
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        // Adjust to LocalDateTime for querying
        LocalDateTime startOfWeekDateTime = startOfWeek.atStartOfDay();
        LocalDateTime endOfWeekDateTime = endOfWeek.atTime(23, 59, 59);

        List<Sales> salesList = salesRepository.findSalesByDateRange(startOfWeekDateTime, endOfWeekDateTime);
        Map<LocalDate, Double> weeklySales = new TreeMap<>();

        for (Sales sales : salesList) {
            LocalDate salesDate = sales.getDate().toLocalDate(); // Convert LocalDateTime to LocalDate
            double salesAmount = sales.getPrice() * sales.getQuantity();
            weeklySales.put(salesDate, weeklySales.getOrDefault(salesDate, 0.0) + salesAmount);
        }

        return weeklySales;
    }





    // 연도와 카테고리별 총 판매량을 계산
    public double getTotalSalesByYearAndCategory(int year, int category) {
        List<Sales> salesList = salesRepository.findSalesByYearAndCategory(year, category);
        return salesList.stream()
                .mapToDouble(s -> s.getPrice() * s.getQuantity())
                .sum();
    }
    // 프로세스로 월별 총 판매량을 계산
    public Map<String, Double> getMonthlySalesByProcess() {
        List<Sales> salesList = salesRepository.findYearlySalesByProcess();
        return salesList.stream()
                .collect(Collectors.groupingBy(
                        s -> {
                            LocalDate date = s.getDate().atZone(ZoneId.systemDefault()).toLocalDate();
                            return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
                        },
                        Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
                ));
    }

    public int getCountByMenuAfterDaysAgo(long menuId,int days){
        return salesRepository.getCountByMenuAfterDaysAgo(menuId, days);
    }

    // 현재 월과 전월의 매출을 계산하여 전월대비 매출 상승률을 계산 6월 7일 추가중
    public double getMonthlySalesGrowthRate(int year, int month) {
        // 현재 월 매출
        double currentMonthSales = getTotalSalesByYearAndMonth(year, month);

        // 전월 매출 계산 (년과 월을 고려하여 처리)
        int previousYear = month == 1 ? year - 1 : year;
        int previousMonth = month == 1 ? 12 : month - 1;
        double previousMonthSales = getTotalSalesByYearAndMonth(previousYear, previousMonth);

        // 전월대비 매출 상승률 계산
        if (previousMonthSales == 0) {
            return 0; // 전월 매출이 0인 경우 상승률 계산 불가
        }
        return ((currentMonthSales - previousMonthSales) / previousMonthSales) * 100;
    }



}