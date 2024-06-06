package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.repository.MenuRepository;
import co.kr.necohost.semi.domain.repository.SalesRepository;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SalesService {
    private final SalesRepository salesRepository;
    MenuRepository menuRepository;

    public SalesService(SalesRepository salesRepository, MenuRepository menuRepository) {
        this.salesRepository = salesRepository;
        this.menuRepository = menuRepository;
    }

    //create
    public void save(SalesRequest salesRequest) {
        salesRepository.save(salesRequest.toEntity());
    }

    //read everything
    public List<Sales> findAll() {
        return salesRepository.findAll();
    }

    //read by id//update?
    public Sales findById(Long id) {
        return salesRepository.findById(id).orElse(null);
    }

    //delete
    public void deleteById(SalesRequest salesRequest) {
        salesRepository.deleteById(salesRequest.getId());
    }

    public int getTotalSalesByCategory(int categoryId, int process) {
        List<Sales> allSales = salesRepository.findByCategoryAndProcess(categoryId, process);

        int totalSales = 0;

        for (Sales sale : allSales) {
            totalSales += sale.getPrice() * sale.getQuantity();
        }

        return totalSales;
    }

    public List<Sales> findByProcess(int process) {
        return salesRepository.findByProcess(process);
    }

    public Map<String, Long> findSalesByToday(){

        LocalDate localDate = LocalDate.now();
        Date today = Date.valueOf(localDate);
        List<Sales> salesList = salesRepository.findSalesByToday(today);
        List<Menu> menuList = menuRepository.findAll();
        Map<Long, String> menuMap = menuList.stream()
                .collect(Collectors.toMap(Menu::getId, Menu::getName));
        Map<String, Long>  result = salesList.stream()
                .collect(Collectors.groupingBy(
                        sales -> menuMap.get(Long.parseLong(String.valueOf(sales.getMenu()))),
                        Collectors.counting()
                ));
        return result;
    }


    public Map<Integer, Double> getYearlySalesByProcess() {
        List<Sales> salesList = salesRepository.findYearlySalesByProcess();
        return salesList.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).getYear(),
                        Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
                ));
    }
    //20240605確認中
    public Map<Integer, Double> getTotalSalesByCategory() {
        List<Sales> salesList = salesRepository.findAllSales();
        return salesList.stream()
                .collect(Collectors.groupingBy(
                        Sales::getCategory,
                        Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
                ));
    }




    public double getTotalSalesByYear(int year) {
        List<Sales> salesList = salesRepository.findSalesByYearAndProcess(year);
        return salesList.stream()
                .mapToDouble(s -> s.getPrice() * s.getQuantity())
                .sum();
    }



    public double getTotalSalesByYearAndMonth(int year, int month) {
        List<Sales> salesList = salesRepository.findSalesByYearAndMonthAndProcess(year, month);
        return salesList.stream()
                .mapToDouble(s -> s.getPrice() * s.getQuantity())
                .sum();
    }






    public double getTotalSalesByYearAndCategory(int year, int category) {
        List<Sales> salesList = salesRepository.findSalesByYearAndCategory(year, category);
        return salesList.stream()
                .mapToDouble(s -> s.getPrice() * s.getQuantity())
                .sum();
    }

//6월 5일 5시
//    public double getTotalSalesByYearAndMonth(int year, int month) {
//        List<Sales> salesList = salesRepository.findSalesByYearAndMonthAndProcess(year, month);
//        return salesList.stream()
//                .mapToDouble(s -> s.getPrice() * s.getQuantity())
//                .sum();
//    }
//6월 5일 5시
    public Map<String, Double> getMonthlySalesByProcess() {
        List<Sales> salesList = salesRepository.findYearlySalesByProcess();
        return salesList.stream()
                .collect(Collectors.groupingBy(
                        s -> {
                            LocalDate date = s.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
                        },
                        Collectors.summingDouble(s -> s.getPrice() * s.getQuantity())
                ));
    }



















}