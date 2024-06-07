package co.kr.necohost.semi.domain.model.entity;

import co.kr.necohost.semi.domain.model.dto.AccountRequest;
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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private String name;
    private LocalDate birthday;
    private String phone;
    private int msRank;
    private int msPoint;
    private String msPass;
    private String OAuth;

    public AccountRequest toAccountRequest() {
       AccountRequest accountRequest = new AccountRequest();
       accountRequest.setId(id);
       accountRequest.setEmail(email);
       accountRequest.setPassword(password);
       accountRequest.setName(name);
       accountRequest.setBirthday(birthday);
       accountRequest.setPhone(phone);
       accountRequest.setMsRank(msRank);
       accountRequest.setMsPoint(msPoint);
       accountRequest.setMsPass(msPass);
       accountRequest.setOAuth(OAuth);
       return accountRequest;
    }
}