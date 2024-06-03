package co.kr.necohost.semi.domain.model.dto.order;

import co.kr.necohost.semi.domain.model.entity.order.Order;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {
    private long id;
    private long device;
    private long deviceNum;

    public Order toEntity(){
        Order order = new Order();
        order.setId(id);
        order.setDevice(device);
        order.setDeviceNum(deviceNum);
        return order;
    }
}
