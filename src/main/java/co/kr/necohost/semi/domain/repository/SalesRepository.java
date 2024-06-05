package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Sales;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesRepository extends JpaRepository<Sales, Long> {
    @Transactional
    void deleteById(Long id);

    List<Sales> findByProcess(int process);

    List<Sales> findAllByOrderByDateAsc();

    //カテゴリー別の総売上高を返還
    List<Sales> findByCategory(int category);

    List<Sales> findByCategoryAndProcess(int categoryId, int process);

    @Query("SELECT s FROM Sales s WHERE s.process = 1")
    List<Sales> findYearlySalesByProcess();

    //確認中
    @Query("SELECT s FROM Sales s WHERE s.process = 1 AND YEAR(s.date) = :year")
    List<Sales> findSalesByYearAndProcess(int year);

    @Query("SELECT s FROM Sales s WHERE s.process = 1 AND YEAR(s.date) = :year AND s.category = :category")
    List<Sales> findSalesByYearAndCategory(int year, int category);
}