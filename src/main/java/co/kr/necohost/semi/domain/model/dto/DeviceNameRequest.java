package co.kr.necohost.semi.domain.model.dto;

import co.kr.necohost.semi.domain.model.entity.Device;
import co.kr.necohost.semi.domain.model.entity.DeviceName;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class DeviceNameRequest {
	private long id;
	private String name;

	public DeviceName toEntity() {
		DeviceName deviceName = new DeviceName();

		deviceName.setId(id);
		deviceName.setName(name);

		return deviceName;
	}

	@Override
	public String toString() {
		return "DeviceNameRequest{" +
				"id=" + id +
				", name=" + name +
				'}';
	}
}