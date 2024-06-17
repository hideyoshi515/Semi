package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.dto.AccountRequest;
import co.kr.necohost.semi.domain.model.entity.Account;
import co.kr.necohost.semi.domain.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

	// アカウントIDでアカウントを取得
	public Account getAccountById(long id) {
		return accountRepository.findById(id).orElse(null);
	}

	// 電話番号でアカウントを取得
	public Account getAccountByPhone(String phone) {
		return accountRepository.findByPhone(phone).orElse(null);
	}

	// メールでアカウントが存在するかチェック
	public boolean isExisted(AccountRequest accountRequest) {
		Account check = accountRepository.findByEmail(accountRequest.getEmail()).orElse(null);
		return check != null;
	}

	// 電話番号でアカウントが存在するかチェック
	public boolean isPhoneExisted(AccountRequest accountRequest) {
		Account check = accountRepository.findByPhone(accountRequest.getPhone()).orElse(null);
		return check != null;
	}

	// アカウントリクエストをエンティティに変換して保存
	@Transactional
	public void save(AccountRequest accountRequest) {
		accountRepository.save(accountRequest.toEntity());
	}

	// アカウントを保存
	@Transactional
	public void save(Account account) {
		accountRepository.save(account);
	}

	// エラーメッセージを処理してマップに格納
	public Map<String, String> validateHandling(Errors errors) {
		Map<String, String> errorMap = new HashMap<String, String>();

		for (FieldError fieldError : errors.getFieldErrors()) {
			String validKeyName = String.format("valid_%s", fieldError.getField());
			errorMap.put(validKeyName, fieldError.getDefaultMessage());
		}

		return errorMap;
	}
}
