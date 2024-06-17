package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.entity.Device;
import co.kr.necohost.semi.domain.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService {
	private final DeviceRepository deviceRepository;

	public DeviceService(DeviceRepository deviceRepository) {
		this.deviceRepository = deviceRepository;
	}

	// すべてのデバイスを取得
	public List<Device> findAll() {
		return deviceRepository.findAll();
	}
}
