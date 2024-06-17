package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
}
