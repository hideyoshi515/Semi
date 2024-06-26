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

    List<Sales> findAllByMenu(Long menuId);
    //process 값이 1인것만 가져오도록 수정중 6월 19일 오후 2시 54분
    List<Sales> findAllByMenuAndProcess(Long menuId, int process);

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

    //LocalDateTime 범위로 매출 기록을 조회
    @Query("SELECT s FROM Sales s WHERE s.date >= :start AND s.date <= :end AND s.process = 1")
    List<Sales> findSalesByDateTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    //특정 날짜의 시간대별 매출을 조회하는 쿼리
    @Query("SELECT s FROM Sales s WHERE s.date >= :start AND s.date <= :end AND s.process = 1 ORDER BY s.date")
    List<Sales> findSalesByDateAndTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    //특정 날짜의 시간대별 매출을 조회하는 쿼리
    @Query("SELECT s FROM Sales s WHERE s.process = 1 AND s.date BETWEEN :start AND :end ORDER BY s.date")
    List<Sales> findSalesByDateRangeAndProcess(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    //해당 연도의 모든 월을 반환하는 쿼리(해당 월의 매출이 0이더라도)
    @Query("SELECT s FROM Sales s WHERE s.process = 1 ORDER BY s.date")
    List<Sales> findMonthlySalesByProcess();

    //날짜범위에 따른 메뉴별 매출액 도출 위한 쿼리
    @Query("SELECT s FROM Sales s WHERE s.date BETWEEN :startDate AND :endDate AND s.process = 1")
    List<Sales> findSalesByDateRangeWithProcess(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    //이 메서드는 데이터베이스에서 지정된 menuId와 일치하는 메뉴 ID를 가지며 process 값이 1인 Sales 엔티티 목록을 검색
    @Query("SELECT s FROM Sales s WHERE s.menu = :menuId AND s.process = 1")
    List<Sales> findByMenu(@Param("menuId") Long menuId);



}
