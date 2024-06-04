package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.dto.AccountRequest;
import co.kr.necohost.semi.domain.model.entity.Account;
import co.kr.necohost.semi.domain.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@Service
public class AccountService {
    AccountRepository accountRepository;
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public boolean isExit(AccountRequest accountRequest){
       Account check = accountRepository.findByEmail(accountRequest.getEmail()).orElse(null);
       return check != null;
    }

    public Map<String, String> validateHandling(Errors errors){
        Map<String, String> errorMap = new HashMap<String, String>();
        for(FieldError fieldError : errors.getFieldErrors()){
            String validKeyName = String.format("valid_%s", fieldError.getField());
            errorMap.put(validKeyName, fieldError.getDefaultMessage());
        }
        return errorMap;
    }

    public void save(AccountRequest accountRequest){
        accountRepository.save(accountRequest.toEntity());
    }
}
