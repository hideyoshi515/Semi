package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Staff;
import co.kr.necohost.semi.domain.model.entity.TimeCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimeCardRepository extends JpaRepository<TimeCard, Long> {
	Optional<TimeCard> findTopByStaffOrderByStartDesc(Staff staff);

	List<TimeCard> findAllByOrderByStartDesc();
}