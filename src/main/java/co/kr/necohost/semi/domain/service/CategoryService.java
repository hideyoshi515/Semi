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

	// カテゴリを削除（キャッシュ無効化）
	@Transactional
	@CacheEvict(value = "categories", allEntries = true)
	public void deleteCategory(int id) {
		categoryRepository.deleteById(id);
	}

	// IDでカテゴリを検索
	public Category findById(int id) {
		return categoryRepository.findById(id).get();
	}

	// すべてのカテゴリを取得（キャッシュ有効）
	@Cacheable(value = "categories")
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	// カテゴリを保存（キャッシュ無効化）
	@CacheEvict(value = "categories", allEntries = true)
	public void saveCategory(CategoryRequest categoryRequest) {
		categoryRepository.save(categoryRequest.toEntity());
	}
}
