package co.kr.necohost.semi.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString

public class Account {
    private String email;
    private String password;
    private String name;
    private LocalDate birthday;
    private String phone;
    private int msRank;
    private int msPoint;
    private String OAuth;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
