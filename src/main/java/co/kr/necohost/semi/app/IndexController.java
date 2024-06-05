package co.kr.necohost.semi.app;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {
    @RequestMapping(value = "/index",method = RequestMethod.GET)
    public String getIndex(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
        model.addAttribute("session", session);
        model.addAttribute("lang", lang);
        return "index.html";
    }

    @RequestMapping(value = "/admin",method = RequestMethod.GET)
    public String getAdmin(Model model, @RequestParam(name = "lang", required = false) String lang, HttpSession session) {
        model.addAttribute("session", session);
        model.addAttribute("lang", lang);
        return "index.html";
    }
}
