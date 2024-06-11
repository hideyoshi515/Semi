package co.kr.necohost.semi.domain.model.dto;

import co.kr.necohost.semi.domain.model.entity.Sales;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class SalesRequest {
    private Long id;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;
    private long category;
    private long menu;
    private int price;
    private int quantity;
    private int device;
    private int deviceNum;
    private long orderNum;
    private int process;

    public Sales toEntity() {
        Sales sales = new Sales();

        sales.setId(id);
        sales.setDate(date);
        sales.setCategory(category);
        sales.setMenu(menu);
        sales.setPrice(price);
        sales.setQuantity(quantity);
        sales.setDevice(device);
        sales.setDeviceNum(deviceNum);
        sales.setOrderNum(orderNum);
        sales.setProcess(process);

        return sales;
    }
}