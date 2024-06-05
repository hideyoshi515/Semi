package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.entity.Category;
import co.kr.necohost.semi.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;

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

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
