package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.repository.SalesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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



}