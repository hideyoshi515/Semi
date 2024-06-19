package co.kr.necohost.semi.app.user;

import co.kr.necohost.semi.domain.model.entity.Account;
import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.repository.AccountRepository;
import co.kr.necohost.semi.domain.service.MenuService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
public class UserController {
    private final AccountRepository accountRepository;
    private final MenuService menuService;

    public UserController(AccountRepository accountRepository, MenuService menuService) {
        this.accountRepository = accountRepository;
        this.menuService = menuService;
    }

    // ホームページのリクエストを処理するメソッド
    @RequestMapping("/")
    public String index(Model model, HttpSession session) {
        List<Menu> menuList = menuService.getAllMenus();
        Random rand = new Random();
        model.addAttribute("menuList", menuList);
        model.addAttribute("randomMenu", menuList.get(rand.nextInt(menuList.size())));
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
