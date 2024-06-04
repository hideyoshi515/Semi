package co.kr.necohost.semi.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class Device {

    private long device;
    private long deviceNum;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
}
