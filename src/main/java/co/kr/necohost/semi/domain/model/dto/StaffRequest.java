package co.kr.necohost.semi.domain.model.dto;


import co.kr.necohost.semi.domain.model.entity.Staff;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffRequest {

    private Long id;
    private String name;
    private String username;

    public Staff toEntity() {
        Staff staff = new Staff();
        staff.setId(this.id);
        staff.setName(this.name);
        staff.setUsername(this.username);
        return staff;
    }
}
