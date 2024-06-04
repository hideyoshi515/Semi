package co.kr.necohost.semi.domain.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString

public class Account {
    @Email(message = "メールアドレスではありません")
    private String email;

    @NotBlank(message = "パスワードを入力してください")
    @Pattern(regexp="(?=.*[0-9])(?=.*[a-z])(?=.*\\W)(?=\\S+$).{8,60}",
            message = "パスワードは半角英数字・記号のみ８文字以上入力してください")
    private String password;

    private String name;
    private LocalDate birthday;

    @Size(min=11, max=11)
    @Pattern(regexp = "/^[0-9]*$/",message = "携帯電話番号はハイフンを抜く入力してください")
    private String phone;

    private int msRank;
    private int msPoint;

    @Size(min=4, max=4)
    @Pattern(regexp = "/^[0-9]*$/")
    private String msPass;

    private String OAuth;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
