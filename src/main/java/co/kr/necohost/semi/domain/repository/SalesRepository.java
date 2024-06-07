package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Sales;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalesRepository extends JpaRepository<Sales, Long> {

    // ID로 판매 기록을 삭제
    @Transactional
    void deleteById(Long id);

    // 프로세스 상태로 판매 기록을 조회
    List<Sales> findByProcess(int process);

    // 날짜 오름차순으로 모든 판매 기록을 조회
    List<Sales> findAllByOrderByDateAsc();

    // 카테고리별로 판매 기록을 조회
    List<Sales> findByCategory(int category);

    // 카테고리와 프로세스 상태로 판매 기록을 조회
    List<Sales> findByCategoryAndProcess(int categoryId, int process);

    // 프로세스가 1인 모든 판매 기록을 조회 (연간 총 판매량 계산에 사용)
    @Query("SELECT s FROM Sales s WHERE s.process = 1")
    List<Sales> findYearlySalesByProcess();

    // 모든 판매 기록을 조회 (카테고리별 총 판매량 계산에 사용)
    @Query("SELECT s FROM Sales s")
    List<Sales> findAllSales();

    // 오늘 날짜의 판매 기록을 조회
    @Query(value = "SELECT * FROM Sales s WHERE s.process = 1 AND DATE(s.date) = :today", nativeQuery = true)
    List<Sales> findSalesByToday(@Param("today") LocalDate today);

    // 연도별로 프로세스가 1인 판매 기록을 조회 (연간 총 판매량 계산에 사용)
    @Query("SELECT s FROM Sales s WHERE s.process = 1 AND YEAR(s.date) = :year")
    List<Sales> findSalesByYearAndProcess(@Param("year") int year);

    // 연도와 월별로 프로세스가 1인 판매 기록을 조회 (연도 및 월별 총 판매량 계산에 사용)
    @Query("SELECT s FROM Sales s WHERE s.process = 1 AND FUNCTION('YEAR', s.date) = :year AND FUNCTION('MONTH', s.date) = :month")
    List<Sales> findSalesByYearAndMonthAndProcess(@Param("year") int year, @Param("month") int month);

    // 연-월-일과 프로세스를 기준으로 판매 데이터를 찾는 메서드
    @Query("SELECT s FROM Sales s WHERE YEAR(s.date) = :year AND MONTH(s.date) = :month AND DAY(s.date) = :day AND s.process = 1")
    List<Sales> findSalesByDayAndProcess(@Param("year") int year, @Param("month") int month, @Param("day") int day);
    //6월 7일 작업중
    @Query("SELECT s FROM Sales s WHERE s.date BETWEEN :startOfWeek AND :endOfWeek AND s.process = 1")
    List<Sales> findSalesByDateRange(@Param("startOfWeek") LocalDateTime startOfWeek, @Param("endOfWeek") LocalDateTime endOfWeek);

    // 연도와 카테고리별로 프로세스가 1인 판매 기록을 조회 (연도 및 카테고리별 총 판매량 계산에 사용)
    @Query("SELECT s FROM Sales s WHERE s.process = 1 AND YEAR(s.date) = :year AND s.category = :category")
    List<Sales> findSalesByYearAndCategory(@Param("year") int year, @Param("category") int category);

    @Query(value = "SELECT COUNT(s.id) FROM Sales s WHERE s.process = 1 AND s.menu = :menuID AND s.date > DATE_SUB(NOW(), INTERVAL :days DAY)", nativeQuery = true)
    int getCountByMenuAfterDaysAgo(@Param("menuID") long menuID, @Param("days") int days);

}
