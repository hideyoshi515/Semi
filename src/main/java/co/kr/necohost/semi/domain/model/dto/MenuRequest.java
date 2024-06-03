package co.kr.necohost.semi.domain.model.dto;

import co.kr.necohost.semi.domain.model.entity.Menu;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class MenuRequest {
    private String name;
    private int price;
    private long category;
    private int stock;
    private MultipartFile image;
    private String description;
    private long id;

    public Menu toEntity(){
        Menu menu = new Menu();
        menu.setName(name);
        menu.setPrice(price);
        menu.setCategory(category);
        menu.setStock(stock);
        menu.setDescription(description);
        menu.setId(id);
        return menu;
    }

    public Menu toEntity(String img){
        Menu menu = new Menu();
        menu.setName(name);
        menu.setPrice(price);
        menu.setCategory(category);
        menu.setStock(stock);
        menu.setImage(img);
        menu.setDescription(description);
        menu.setId(id);
        return menu;
    }
}
