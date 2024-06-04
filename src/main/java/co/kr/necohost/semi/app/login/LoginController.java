package co.kr.necohost.semi.app.login;

import co.kr.necohost.semi.domain.model.dto.AccountRequest;
import co.kr.necohost.semi.domain.model.entity.Account;
import co.kr.necohost.semi.domain.repository.AccountRepository;
import co.kr.necohost.semi.domain.service.AccountService;
import co.kr.necohost.semi.domain.service.CategoryService;
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
    public String getRegister(Model model, HttpSession session, @RequestParam Map<String, Object> params) {
        AccountRequest accountRequest = new AccountRequest();
        model.addAttribute("session", session);
        model.addAttribute("categories", categoryService.getAllCategories());
        if (session.getAttribute("account") != null) {
            return "redirect:/";
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
        if (accountService.isExit(accountRequest)) {
            canCreate = "既に存在するアカウントです";
            model.addAttribute("canCreate", canCreate);
            return "login/register_failed.html";
        }
        accountService.save(accountRequest);
        Account account = accountRepository.findByEmail(accountRequest.getEmail()).orElse(null);
        session.setAttribute("account", account);
        session.setMaxInactiveInterval(60 * 60 * 24); //60s * 60m * 24h
        return "redirect:/";

    }
}
