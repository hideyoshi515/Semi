package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Sales;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesRepository extends JpaRepository<Sales, Long> {

    @Transactional
    void deleteById(Long id);

    List<Sales> findAllByOrderByDateAsc();



    //カテゴリー別の総売上高を返還
    List<Sales>  findByCategory(int category);


}
