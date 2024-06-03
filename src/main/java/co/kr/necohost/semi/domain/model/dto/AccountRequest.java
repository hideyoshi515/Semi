package co.kr.necohost.semi.domain.model.dto;


import co.kr.necohost.semi.domain.model.entity.Account;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
public class AccountRequest {
    private Long id;
    private String email;
    private String password;
    private String name;
    private LocalDate birthday;
    private String phone;
    private int msRank;
    private int msPoint;
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
        account.setOAuth(OAuth);
        return account;
    }
}
