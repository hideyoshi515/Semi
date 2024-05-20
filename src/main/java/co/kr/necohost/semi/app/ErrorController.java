package co.kr.necohost.semi.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {
    @RequestMapping("/error-page/{errorCode}")
    public String errorPage(@PathVariable("errorCode") int errorCode, Model model) {
        model.addAttribute("errorCode", errorCode);
        return "error.html";
    }
}
