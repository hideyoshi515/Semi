package co.kr.necohost.semi.domain.model.dto;

import co.kr.necohost.semi.domain.model.entity.Device;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class DeviceRequest {
    private long id;
    private long device;
    private long deviceNum;
    private Map<Long, Integer> quantities;
    private long orderNum;

    public Device toEntity() {
        Device device = new Device();

        device.setId(id);
        device.setDevice(this.device);
        device.setDeviceNum(deviceNum);

        return device;
    }
    public String toString(){
        return "DeviceRequest [id=" + id + ", device=" + device + ", deviceNum=" + deviceNum
                + ", quantities=" + quantities + ", orderNum=" + orderNum + "]";
    }
}