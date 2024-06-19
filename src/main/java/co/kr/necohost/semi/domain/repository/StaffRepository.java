package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
	Optional<Staff> findByName(String name);

    @Transactional
    void delete(Staff target);
}