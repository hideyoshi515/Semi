package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Menu;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findAll();
    List<Menu> findAllByOrderByCategory();


    @Cacheable("cat_menus")
    List<Menu> findByCategory(int category);

 }
