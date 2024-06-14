package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.dto.CategoryRequest;
import co.kr.necohost.semi.domain.model.entity.Category;
import co.kr.necohost.semi.domain.repository.CategoryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
	CategoryRepository categoryRepository;

	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	public Category findById(int id) {
		return categoryRepository.findById(id).get();
	}

	@Cacheable(value = "categories")
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	@CacheEvict(value = "categories", allEntries = true)
	public void saveCategory(CategoryRequest categoryRequest) {
		categoryRepository.save(categoryRequest.toEntity());
	}

	@Transactional
	@CacheEvict(value = "categories", allEntries = true)
	public void deleteCategory(int id) {
		categoryRepository.deleteById(id);
	}
}