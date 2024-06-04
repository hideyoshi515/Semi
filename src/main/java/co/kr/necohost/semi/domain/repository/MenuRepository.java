package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findAll();
 }
