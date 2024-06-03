package co.kr.necohost.semi.domain.model.dto;

import co.kr.necohost.semi.domain.model.entity.Sales;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SalesRequest {
    private Long id;
    private Date date;
    private int category;
    private int menu;
    private int price;
    private int quantity;
    private int device;
    private int devieNum;
    private String salescol;

    public Sales toEntity() {
        Sales sales = new Sales();
        sales.setId(id);
        sales.setDate(date);
        sales.setCategory(category);
        sales.setMenu(menu);
        sales.setPrice(price);
        sales.setQuantity(quantity);
        sales.setDevice(device);
        sales.setDevieNum(devieNum);
        sales.setSalescol(salescol);
        return sales;

    }
}
