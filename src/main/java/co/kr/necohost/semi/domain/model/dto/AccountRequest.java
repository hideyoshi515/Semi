package co.kr.necohost.semi.domain.model.dto;


import co.kr.necohost.semi.domain.model.entity.Account;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
public class AccountRequest {
    private Long id;

    @NotBlank(message = "メールアドレスではありません")
    @Email(message = "メールアドレスではありません")
    private String email;

    @NotBlank(message = "パスワードを入力してください")
    @Pattern(regexp="^[a-zA-Z0-9\\W]{8,45}$",
            message = "パスワードは半角英数字・記号のみ８文字以上入力してください")
    private String password;
    private String name;
    private LocalDate birthday;

    @Pattern(regexp = "^[0-9]{11}$",message = "携帯電話番号はハイフンを抜く入力してください")
    private String phone;
    private int msRank;
    private int msPoint;

    @Pattern(regexp = "^[0-9]{4}$", message = "暗証番号は半角数字４文字で入力してください")
    private String msPass;
    private String OAuth;

    public Account toEntity(){
        Account account = new Account();
        account.setId(id);
        account.setEmail(email);
        account.setPassword(password);
        account.setName(name);
        account.setBirthday(birthday);
        account.setPhone(phone);
        account.setMsRank(msRank);
        account.setMsPoint(msPoint);
        account.setMsPass(msPass);
        account.setOAuth(OAuth);
        return account;
    }
}
