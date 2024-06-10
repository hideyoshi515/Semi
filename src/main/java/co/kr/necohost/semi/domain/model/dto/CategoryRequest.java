package co.kr.necohost.semi.domain.model.dto;

import co.kr.necohost.semi.domain.model.entity.Category;
import lombok.*;

@Getter
@Setter
public class CategoryRequest {
    private long id;
    private String name;

    public Category toEntity() {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }
}