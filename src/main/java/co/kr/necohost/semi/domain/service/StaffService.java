package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.entity.Staff;
import co.kr.necohost.semi.domain.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffService {

    private final StaffRepository staffRepository;

    @Autowired
    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    // すべてのスタッフを取得
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    // スタッフを削除
    public void deleteStaff(Long id) {
        staffRepository.deleteById(id);
    }

    // スタッフを挿入
    public void insertStaff(Staff staff) {
        staffRepository.save(staff);
    }

    public Staff getStaff(Long id) {
        return staffRepository.findById(id).get();
    }

    public void save(Staff staff) {
        staffRepository.save(staff);
    }
}
