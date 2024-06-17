package co.kr.necohost.semi.app.login;

import co.kr.necohost.semi.domain.model.dto.AccountRequest;
import co.kr.necohost.semi.domain.model.entity.Account;
import co.kr.necohost.semi.domain.repository.AccountRepository;
import co.kr.necohost.semi.domain.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class LoginController {
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    public LoginController(AccountService accountService, AccountRepository accountRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }

    // ログインページのGETリクエストを処理するメソッド
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLogin(Model model, HttpSession session, HttpServletRequest request) {
        setPreviousPage(session, request, "/login");
        AccountRequest accountRequest = new AccountRequest();
        model.addAttribute("session", session);
        model.addAttribute("accountRequest", accountRequest);
        return "login/login.html";
    }

    // ログインページのPOSTリクエストを処理するメソッド（実装は必要に応じて追加）
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String postLogin() {
        // postLoginの実装をここに追加します（必要な場合）
        return "login/login.html";
    }

    // マイページのGETリクエストを処理するメソッド
    @RequestMapping(value = "/mypage", method = RequestMethod.GET)
    public String getMypage(Model model, HttpSession session) {
        Account account = (Account) session.getAttribute("account");
        if (account == null) {
            return "redirect:/login";
        }
        model.addAttribute("account", account);
        model.addAttribute("accountRequest", account.toAccountRequest());
        model.addAttribute("session", session);
        return "user/mypage.html";
    }

    // マイページのPOSTリクエストを処理するメソッド
    @RequestMapping(value = "/mypage", method = RequestMethod.POST)
    public String postMypage(HttpSession session, @ModelAttribute("accountRequest") AccountRequest accountRequest, @RequestParam Map<String, Object> params) {
        Account account = (Account) session.getAttribute("account");
        AccountRequest newRequest = account.toAccountRequest();
        boolean changed = false;

        if ("edit".equals(params.get("passEdit"))) {
            newRequest.setPassword(accountRequest.getPassword());
            changed = true;
        }
        if ("edit".equals(params.get("phoneEdit"))) {
            newRequest.setPhone(accountRequest.getPhone());
            changed = true;
        }
        if ("edit".equals(params.get("msPassEdit"))) {
            newRequest.setMsPass(accountRequest.getMsPass());
            changed = true;
        }
        if (changed) {
            accountService.save(newRequest);
            session.setAttribute("account", newRequest.toEntity());
        }
        return "redirect:/";
    }

    // 登録ページのGETリクエストを処理するメソッド
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String getRegister(Model model, HttpSession session, HttpServletRequest request) {
        setPreviousPage(session, request, "/register");
        if (session.getAttribute("account") != null) {
            return "redirect:" + session.getAttribute("prevpage");
        }
        AccountRequest accountRequest = (AccountRequest) session.getAttribute("accountRequest");
        if (accountRequest == null) {
            accountRequest = new AccountRequest();
        }
        model.addAttribute("session", session);
        model.addAttribute("accountRequest", accountRequest);
        return "login/register.html";
    }

    // 登録ページのPOSTリクエストを処理するメソッド
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String postRegister(Model model, @Valid @ModelAttribute("accountRequest") AccountRequest accountRequest, Errors errors, HttpSession session) {
        if (errors.hasErrors()) {
            model.addAttribute("accountRequest", accountRequest);
            Map<String, String> validatorResult = accountService.validateHandling(errors);
            validatorResult.forEach(model::addAttribute);
            return "login/register.html";
        }

        if (accountService.isExisted(accountRequest)) {
            model.addAttribute("valid_email", "既に存在するメールアドレスです");
            return "login/register.html";
        }

        if (accountService.isPhoneExisted(accountRequest)) {
            model.addAttribute("valid_phone", "既に存在する携帯電話番号です");
            return "login/register.html";
        }

        accountService.save(accountRequest);
        Account account = accountRepository.findByEmail(accountRequest.getEmail()).orElse(null);
        session.setAttribute("account", account);
        session.setMaxInactiveInterval(60 * 60 * 24); // 24時間
        session.removeAttribute("accountRequest");
        return "redirect:" + session.getAttribute("prevpage");
    }

    // 前のページを設定するメソッド
    private void setPreviousPage(HttpSession session, HttpServletRequest request, String currentPath) {
        String uri = request.getHeader("Referer");
        if (uri != null && !uri.contains(currentPath) && !uri.contains("/login")) {
            session.setAttribute("prevpage", uri);
        } else {
            session.setAttribute("prevpage", "/");
        }
    }
}
