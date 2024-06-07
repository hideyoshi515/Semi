package co.kr.necohost.semi.app.login;

import co.kr.necohost.semi.domain.model.dto.AccountRequest;
import co.kr.necohost.semi.domain.model.entity.Account;
import co.kr.necohost.semi.domain.repository.AccountRepository;
import co.kr.necohost.semi.domain.service.AccountService;
import co.kr.necohost.semi.domain.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class LoginController {
    private final AccountRepository accountRepository;
    CategoryService categoryService;
    AccountService accountService;

    public LoginController(CategoryService categoryService, AccountService accountService, AccountRepository accountRepository) {
        this.categoryService = categoryService;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String getRegister(Model model, HttpSession session, HttpServletRequest request, @RequestParam Map<String, Object> params) {
        AccountRequest accountRequest = new AccountRequest();
        model.addAttribute("session", session);
        model.addAttribute("categories", categoryService.getAllCategories());
        String uri = request.getHeader("Referer");
        if (uri != null && !uri.contains("/register") && !uri.contains("/login")) {
            session.setAttribute("prevpage", uri);
        } else {
            session.setAttribute("prevpage", "/");
        }
        if (session.getAttribute("account") != null) {
            return "redirect:" + session.getAttribute("prevpage");
        }
        if (session.getAttribute("accountRequest") != null) {
            accountRequest = (AccountRequest) session.getAttribute("accountRequest");
        }
        model.addAttribute("accountRequest", accountRequest);
        return "login/register.html";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String postRegister(Model model, @Valid @ModelAttribute("accountRequest") AccountRequest accountRequest, Errors errors, HttpSession session, @RequestParam Map<String, Object> params) {
        String canCreate = "";
        if (errors.hasErrors()) {
            model.addAttribute("accountRequest", accountRequest);
            Map<String, String> validatorResult = accountService.validateHandling(errors);
            for (String error : validatorResult.keySet()) {
                model.addAttribute(error, validatorResult.get(error));
            }
            return "login/register.html";
        }
        boolean errored = false;
        if (accountService.isExit(accountRequest)) {
            canCreate = "既に存在するメールアドレスです";
            model.addAttribute("valid_email", canCreate);
            errored = true;
        }
        if (accountService.isPhoneExit(accountRequest)) {
            canCreate = "既に存在する携帯電話番号です";
            model.addAttribute("valid_phone", canCreate);
            errored = true;
        }
        if (errored) {
            return "login/register.html";
        }
        accountService.save(accountRequest);
        Account account = accountRepository.findByEmail(accountRequest.getEmail()).orElse(null);
        session.setAttribute("account", account);
        session.setMaxInactiveInterval(60 * 60 * 24); //60s * 60m * 24h
        session.removeAttribute("accountRequest");
        return "redirect:" + session.getAttribute("prevpage");

    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLogin(Model model, HttpSession session, HttpServletRequest request, @RequestParam Map<String, Object> params) {
        String uri = request.getHeader("Referer");
        if (uri != null && !uri.contains("/register") && !uri.contains("/login")) {
            session.setAttribute("prevpage", uri);
        } else {
            session.setAttribute("prevpage", "/");
        }
        AccountRequest accountRequest = new AccountRequest();
        model.addAttribute("session", session);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("accountRequest", accountRequest);
        return "login/login.html";
    }

    @RequestMapping(value = "/mypage", method = RequestMethod.GET)
    public String getMypage(Model model, HttpSession session, @RequestParam Map<String, Object> params) {
        Account account = (Account) session.getAttribute("account");
        if (account == null) {
            model.addAttribute("account", null);
            return "redirect:/login";
        } else {
            model.addAttribute("account", account);
        }

        AccountRequest accountRequest = account.toAccountRequest();

        model.addAttribute("accountRequest", accountRequest);
        model.addAttribute("session", session);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "user/mypage.html";
    }

    @RequestMapping(value = "/mypage", method = RequestMethod.POST)
    public String postMypage(Model model, HttpSession session, @ModelAttribute("accountRequest") AccountRequest accountRequest, @RequestParam Map<String, Object> params) {
        Account account = (Account) session.getAttribute("account");
        AccountRequest newRequest = account.toAccountRequest();
        boolean changed = false;
        System.out.println("passEdit"+params.get("passEdit"));
        System.out.println("phoneEdit"+params.get("phoneEdit"));
        System.out.println("msPassEdit"+params.get("msPassEdit"));
        if (params.get("passEdit") != null && params.get("passEdit").toString().equals("edit")) {
            newRequest.setPassword(accountRequest.getPassword());
            changed = true;
        }
        if (params.get("phoneEdit") != null && params.get("phoneEdit").toString().equals("edit")) {
            newRequest.setPhone(accountRequest.getPhone());
            changed = true;
        }
        if (params.get("msPassEdit") != null && params.get("msPassEdit").toString().equals("edit")) {
            newRequest.setMsPass(accountRequest.getMsPass());
            changed = true;
        }
        if (changed) {
            accountService.save(newRequest);
            session.setAttribute("account", newRequest.toEntity());
        }
        return "redirect:/";
    }
}