package co.kr.necohost.semi.domain.repository;


import co.kr.necohost.semi.domain.model.entity.Category;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
	@Cacheable("categories")
	@Override
	public List<Category> findAll();
}