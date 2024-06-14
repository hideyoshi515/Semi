package co.kr.necohost.semi.domain.model.dto;

import co.kr.necohost.semi.domain.model.entity.Position;
import co.kr.necohost.semi.domain.model.entity.Staff;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffRequest {
    private Long id;
    private String name;
    private String username;
    private String phone;
    private Position position;

    public Staff toEntity() {
        Staff staff = new Staff();
        staff.setId(this.id);
        staff.setName(this.name);
        staff.setUsername(this.username);
        staff.setPhone(this.phone);
        staff.setPosition(this.position);
        return staff;
    }
}
