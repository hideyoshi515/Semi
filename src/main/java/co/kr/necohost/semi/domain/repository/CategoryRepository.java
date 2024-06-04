package co.kr.necohost.semi.domain.repository;


import co.kr.necohost.semi.domain.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

}
