package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderRepository extends JpaRepository<Sales, Integer> {
    List<Sales> findByProcess(int processId);

    Sales findById(long id);

    @Query("SELECT s, m.name, c.name, m.stock FROM Sales s JOIN Menu m ON s.menu = m.id JOIN Category c ON s.category = c.id WHERE s.process = 0")
    List<Object[]> findSalesByProcess(int process);

    @Query("SELECT s, m.name, c.name, m.stock FROM Sales s JOIN Menu m ON s.menu = m.id JOIN Category c ON s.category = c.id WHERE s.process = 0 AND s.device = 3")
    List<Object[]> findSalesByProcessAndDevice(int process);

    @Query("SELECT s, m.name, c.name, m.stock FROM Sales s JOIN Menu m ON s.menu = m.id JOIN Category c ON s.category = c.id WHERE s.id = :orderId")
    List<Object[]> findSalesById(@Param("orderId") long id);

    @Modifying
    @Transactional
    @Query("UPDATE Sales s SET s.process = s.process - 1 WHERE s.id= :orderId")
    void updateDenialByProcess(@Param("orderId") long id);

    @Modifying
    @Transactional
    @Query("UPDATE Sales s SET s.process = s.process + 1 WHERE s.id = :orderId")
    void updateSalesProcess(@Param("orderId") long id);

    @Modifying
    @Transactional
    @Query("UPDATE Menu m SET m.stock = :orderQuantity WHERE m.id = :menuId")
    void updateMenuStock(@Param("menuId") long menuId, @Param("orderQuantity") int quantity);
}
