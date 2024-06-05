package co.kr.necohost.semi.config;

import co.kr.necohost.semi.domain.model.entity.Account;
import co.kr.necohost.semi.domain.repository.AccountRepository;
import co.kr.necohost.semi.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import co.kr.necohost.semi.oauth2.handler.OAuth2AuthenticationFailureHandler;
import co.kr.necohost.semi.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import co.kr.necohost.semi.oauth2.service.CustomOAuth2UserService;
import co.kr.necohost.semi.oauth2.user.OAuth2UserUnlinkManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final DataSource dataSource;
    private final OAuth2UserUnlinkManager oAuth2UserUnlinkManager;
    private final AccountRepository accountRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        OAuth2AuthenticationSuccessHandler successHandler = new OAuth2AuthenticationSuccessHandler(
                httpCookieOAuth2AuthorizationRequestRepository,
                oAuth2UserUnlinkManager,
                accountRepository,
                rememberMeServices() // CustomRememberMeServices를 생성하여 주입
        );
        http.csrf(AbstractHttpConfigurer::disable)
                .headers(headersConfigurer ->
                        headersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) // For H2 DB
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests ->
                        requests
                                .requestMatchers(antMatcher("/api/admin/**")).hasRole("ADMIN")
                                .requestMatchers(antMatcher("/api/user/**")).hasRole("USER")
                                .requestMatchers(antMatcher("/h2-console/**")).permitAll()
                                .anyRequest().permitAll())
                .sessionManagement(sessions ->
                        sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(configure ->
                        configure
                                .authorizationEndpoint(config ->
                                        config.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository))
                                .userInfoEndpoint(config ->
                                        config.userService(customOAuth2UserService))
                                .successHandler(successHandler)
                                .failureHandler(oAuth2AuthenticationFailureHandler))
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/signin")
                                .usernameParameter("email")
                                .passwordParameter("password")
                                .permitAll()
                                .successHandler((request, response, authentication) -> {
                                    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                                    Account account = userDetails.getAccount();
                                    request.getSession().setAttribute("account", account);
                                    response.sendRedirect(request.getSession().getAttribute("prevpage").toString());
                                }))
                .logout(logoutConfig ->
                        logoutConfig
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .logoutSuccessUrl("/")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID"))
                .rememberMe(rememberMe ->
                        rememberMe.key("unique")
                                .rememberMeServices(rememberMeServices())
                                .tokenRepository(persistentTokenRepository())
                                .userDetailsService(userDetailsService()))
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService(accountRepository);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        CustomAuthenticationProvider authProvider = new CustomAuthenticationProvider(userDetailsService());
        return authProvider;
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        CustomJdbcTokenRepositoryImpl tokenRepository = new CustomJdbcTokenRepositoryImpl(dataSource);
        return tokenRepository;
    }


    @Bean
    public CustomRememberMeServices rememberMeServices() {
        CustomRememberMeServices rememberMeServices = new CustomRememberMeServices("unique", userDetailsService(), persistentTokenRepository(), accountRepository);
        rememberMeServices.setAlwaysRemember(true);
        rememberMeServices.setParameter("remember-me");
        rememberMeServices.setTokenValiditySeconds(60 * 60 * 24 * 7);
        rememberMeServices.setUseSecureCookie(false);
        return rememberMeServices;
    }
}
