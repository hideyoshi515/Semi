package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Menu;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findAllByOrderByCategory();
    List<Menu> findAllByOrderByCategoryAscIdDesc();
    List<Menu> findAllByOrderByIdDescCategoryAsc();

    @Override
    @Cacheable("Menu_findById")
    public Optional<Menu> findById(Long id);

    @Cacheable("cat_menus")
    List<Menu> findByCategory(int category);
    @Cacheable("cat_menus")
    List<Menu> findByCategoryOrderByIdDesc(int category);
}