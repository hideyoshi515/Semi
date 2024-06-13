package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Menu;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findAllByOrderByCategory();
    List<Menu> findAllByOrderByCategoryAscIdDesc();
    List<Menu> findAllByOrderByIdDescCategoryAsc();

    Menu getMenuById(Long id);

    @Cacheable("cat_menus")
    List<Menu> findByCategory(int category);

    List<Menu> findByCategoryOrderByIdDesc(int category);
}