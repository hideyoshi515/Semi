package co.kr.necohost.semi.domain.model.dto;

import co.kr.necohost.semi.domain.model.entity.Category;
import co.kr.necohost.semi.domain.model.entity.Menu;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuWithCategoryRequest {
    private Menu menu;
    private Category category;
}
