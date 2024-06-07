package co.kr.necohost.semi.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderNum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderNum;
}
