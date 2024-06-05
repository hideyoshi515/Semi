package co.kr.necohost.semi.security;

import co.kr.necohost.semi.domain.model.entity.Account;
import co.kr.necohost.semi.domain.repository.AccountRepository;
import co.kr.necohost.semi.oauth2.service.OAuth2UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

public class CustomRememberMeServices extends PersistentTokenBasedRememberMeServices {

    private final AccountRepository accountRepository;

    public CustomRememberMeServices(String key, UserDetailsService userDetailsService, PersistentTokenRepository tokenRepository, AccountRepository accountRepository) {
        super(key, userDetailsService, tokenRepository);
        this.accountRepository = accountRepository;
    }

    @Override
    protected void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        super.onLoginSuccess(request, response, successfulAuthentication);
        Account account = null;
        // 세션에 사용자 정보 저장
        if (successfulAuthentication.getPrincipal().getClass() == OAuth2UserPrincipal.class) {
            OAuth2UserPrincipal getPrincipal = (OAuth2UserPrincipal) successfulAuthentication.getPrincipal();
            account = accountRepository.findByEmail(getPrincipal.getUsername()).orElse(null);
        } else {
            CustomUserDetails userDetails = (CustomUserDetails) successfulAuthentication.getPrincipal();
            account = userDetails.getAccount();
        }
        request.getSession().setAttribute("account", account);
    }

    @Override
    protected Authentication createSuccessfulAuthentication(HttpServletRequest request, UserDetails user) {
        Authentication authentication = super.createSuccessfulAuthentication(request, user);

        // 세션에 사용자 정보 저장
        CustomUserDetails userDetails = (CustomUserDetails) user;
        Account account = userDetails.getAccount();
        request.getSession().setAttribute("account", account);

        return authentication;
    }
}
