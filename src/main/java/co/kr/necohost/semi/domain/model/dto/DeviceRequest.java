package co.kr.necohost.semi.domain.model.dto;

import co.kr.necohost.semi.domain.model.entity.Device;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceRequest {
    private long id;
    private long device;
    private long deviceNum;

    public Device toEntity(){
        Device device = new Device();
        device.setId(id);
        device.setDevice(this.device);
        device.setDeviceNum(deviceNum);
        return device;
    }
}