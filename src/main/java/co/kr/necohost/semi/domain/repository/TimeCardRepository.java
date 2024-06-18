package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.model.entity.Staff;
import co.kr.necohost.semi.domain.model.entity.TimeCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TimeCardRepository extends JpaRepository<TimeCard, Long> {
	Optional<TimeCard> findTopByStaffOrderByStartDesc(Staff staff);

	List<TimeCard> findAllByOrderByStartDesc();

	@Query(value = "SELECT * FROM TimeCard WHERE staff_id = :staffid",nativeQuery = true)
	List<TimeCard> findAllByStaffId(@Param("staffid") long staffid);

	@Query("SELECT t FROM TimeCard t WHERE DATE(t.start) = :start AND t.staff = :staff")
	TimeCard findByIdAndStart(@Param("staff") Staff staff, @Param("start") LocalDate start);
}