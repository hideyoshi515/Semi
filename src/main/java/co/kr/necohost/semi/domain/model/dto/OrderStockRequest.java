package co.kr.necohost.semi.domain.model.dto;


import co.kr.necohost.semi.domain.model.entity.OrderStock;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class OrderStockRequest {
    private int menu;
    private int quantity;
    private Timestamp orderDate;
    private int id;

    public OrderStock toEntity() {
        OrderStock orderStock = new OrderStock();
        orderStock.setMenu(menu);
        orderStock.setQuantity(quantity);
        orderStock.setOrderDate(orderDate);
        orderStock.setId(id);
        return orderStock;
    }
}
