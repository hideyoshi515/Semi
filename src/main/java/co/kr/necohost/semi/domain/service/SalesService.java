package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.repository.SalesRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SalesService {
    private final SalesRepository salesRepository;
    SalesRepository SalesRepository;

    public SalesService(SalesRepository salesRepository) {
        this.SalesRepository = salesRepository;
        this.salesRepository = salesRepository;
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

    //作成中
//    public double getTotalSalesByYearAndMonth(int year, int month) {
//        List<Sales> salesList = salesRepository.findSalesByYearAndMonthAndProcess(year, month);
//        return salesList.stream()
//                .mapToDouble(s -> s.getPrice() * s.getQuantity())
//                .sum();
//    }

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
}