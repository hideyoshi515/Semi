package co.kr.necohost.semi.app.user;

import co.kr.necohost.semi.domain.model.entity.Account;
import co.kr.necohost.semi.domain.repository.AccountRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class UserController {
    private final AccountRepository accountRepository;

    public UserController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // ホームページのリクエストを処理するメソッド
    @RequestMapping("/")
    public String index(Model model, HttpSession session) {
        model.addAttribute("session", session);
        return "user/home.html";
    }

    // ユーザーの会員チェックを行うメソッド
    @ResponseBody
    @RequestMapping("/user/memberCheck")
    public Account getMemberCheck(@RequestParam Map<String, Object> params) {
        String phoneNum = params.get("phoneNum").toString().replaceAll("-", "");
        return accountRepository.findByPhone(phoneNum).orElse(null);
    }
}
