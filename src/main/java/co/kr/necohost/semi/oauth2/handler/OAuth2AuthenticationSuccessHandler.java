package co.kr.necohost.semi.oauth2.handler;

import co.kr.necohost.semi.domain.model.dto.AccountRequest;
import co.kr.necohost.semi.domain.model.entity.Account;
import co.kr.necohost.semi.domain.repository.AccountRepository;
import co.kr.necohost.semi.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import co.kr.necohost.semi.oauth2.service.OAuth2UserPrincipal;
import co.kr.necohost.semi.oauth2.user.OAuth2Provider;
import co.kr.necohost.semi.oauth2.user.OAuth2UserUnlinkManager;
import co.kr.necohost.semi.oauth2.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final OAuth2UserUnlinkManager oAuth2UserUnlinkManager;
    private final AccountRepository accountRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {


        Optional<String> redirectUri = CookieUtils.getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME).map(Cookie::getValue);

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        OAuth(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed.");
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected Account OAuth(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {


        String mode = CookieUtils.getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.MODE_PARAM_COOKIE_NAME).map(Cookie::getValue).orElse("");

        OAuth2UserPrincipal principal = getOAuth2UserPrincipal(authentication);

        if (principal == null) {
            return null;
        }

        if ("login".equalsIgnoreCase(mode)) {
            // TODO: DB 저장
            log.info("email={}, name={}, nickname={}, accessToken={}", principal.getUserInfo().getEmail(), principal.getUserInfo().getName(), principal.getUserInfo().getNickname(), principal.getUserInfo().getAccessToken());
            Account account = accountRepository.findByEmail(principal.getUserInfo().getEmail()).orElse(null);
                HttpSession session = request.getSession(true);
            if (account == null) {
                AccountRequest accountRequest = new AccountRequest();
                accountRequest.setEmail(principal.getUserInfo().getEmail());
                accountRequest.setName(principal.getUserInfo().getLastName() + principal.getUserInfo().getFirstName());
                accountRequest.setOAuth(principal.getUserInfo().getProvider().getRegistrationId());
                session.setAttribute("accountRequest", accountRequest);
                session.setMaxInactiveInterval(60 * 10); //60s * 10m
                //accountRepository.save(account);
            }
            if (account != null) {
                session.setAttribute("account", account);
                session.setMaxInactiveInterval(60 * 60 * 24); //60s * 60m * 24h
            }

            String accessToken = principal.getUserInfo().getAccessToken();
            //String refreshToken = "test_refresh_token";

            return account;

        } else if ("unlink".equalsIgnoreCase(mode)) {

            String accessToken = principal.getUserInfo().getAccessToken();
            OAuth2Provider provider = principal.getUserInfo().getProvider();

            // TODO: DB 삭제
            // TODO: 리프레시 토큰 삭제
            oAuth2UserUnlinkManager.unlink(provider, accessToken);

            return null;
        }

        return null;
    }

    private OAuth2UserPrincipal getOAuth2UserPrincipal(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2UserPrincipal) {
            return (OAuth2UserPrincipal) principal;
        }
        return null;
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}