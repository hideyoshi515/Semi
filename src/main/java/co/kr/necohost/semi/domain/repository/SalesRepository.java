package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Sales;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesRepository extends JpaRepository<Sales, Long> {

    @Transactional
    void deleteById(Long id);

    List<Sales> findByProcess(int process);

    List<Sales> findAllByOrderByDateAsc();



    //カテゴリー別の総売上高を返還
    List<Sales>  findByCategory(int category);

    List<Sales> findByCategoryAndProcess(int categoryId, int process);

    @Query("SELECT s FROM Sales s WHERE s.process = 1")
    List<Sales> findYearlySalesByProcess();


    //20240605確認中
    @Query("SELECT s FROM Sales s")
    List<Sales> findAllSales();





    @Query("SELECT s FROM Sales s WHERE s.process = 1 AND YEAR(s.date) = :year")
    List<Sales> findSalesByYearAndProcess(int year);
    //作成中
//    @Query("SELECT s FROM Sales s WHERE s.process = 1 AND YEAR(s.date) =:year And MONTH(s.date) =:month")
//    List<Sales> findSalesByYearAndMonthAndProcess(int year, int month);
    //作成中
    @Query("SELECT s FROM Sales s WHERE s.process = 1 AND FUNCTION('YEAR', s.date) = :year AND FUNCTION('MONTH', s.date) = :month")
    List<Sales> findSalesByYearAndMonthAndProcess(@Param("year") int year, @Param("month") int month);



    @Query("SELECT s FROM Sales s WHERE s.process = 1 AND YEAR(s.date) = :year AND s.category = :category")
    List<Sales> findSalesByYearAndCategory(int year, int category);

//6월 5일 오후 6시
//    @Query("SELECT s FROM Sales s WHERE s.process = 1 AND YEAR(s.date) = :year AND MONTH(s.date) = :month")
//    List<Sales> findSalesByYearAndMonthAndProcess(@Param("year") int year, @Param("month") int month);
//6월 5일 오후 6시- 겹치니 주석처리
//    @Query("SELECT s FROM Sales s WHERE s.process = 1")
//    List<Sales> findYearlySalesByProcess();


}
