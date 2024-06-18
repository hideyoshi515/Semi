package co.kr.necohost.semi.app.timeCard;

import co.kr.necohost.semi.domain.model.dto.StaffRequest;
import co.kr.necohost.semi.domain.model.dto.TimeCardRequest;
import co.kr.necohost.semi.domain.model.entity.Staff;
import co.kr.necohost.semi.domain.model.entity.TimeCard;
import co.kr.necohost.semi.domain.service.DiscordBotService;
import co.kr.necohost.semi.domain.service.StaffService;
import co.kr.necohost.semi.domain.service.TimeCardService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class TimeCardController {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm");
    private final TimeCardService timeCardService;
    private final DiscordBotService discordBotService;
    private final StaffService staffService;

    public TimeCardController(TimeCardService timeCardService, StaffService staffService, DiscordBotService discordBotService) {
        this.timeCardService = timeCardService;
        this.discordBotService = discordBotService;
        this.staffService = staffService;
    }

    @RequestMapping(value = "/timeCardList", method = RequestMethod.GET)
    public String getTimeCardList(Model model) {
        List<TimeCardRequest> timeCards = timeCardService.getAllTimeCardDesc();
        model.addAttribute("timeCards", timeCards);
        return "timeCard/timeCardList.html";
    }

    @RequestMapping(value = "/timeCardInput", method = RequestMethod.GET)
    public String getTimeCardInput(Model model) {
        StaffRequest staffRequest = new StaffRequest();
        model.addAttribute("staffRequest", staffRequest);
        return "timeCard/timeCardInput.html";
    }

    @ResponseBody
    @RequestMapping(value = "/timeCardIn", method = RequestMethod.GET)
    public String getTimeCardIn(@RequestParam Map<String, Object> params) {
        Staff staff = staffService.getStaff(Long.valueOf(params.get("staffId").toString()));
        timeCardService.clockIn(staff);
        Optional<TimeCard> timeCard = timeCardService.getTimeCardByUserName(staff);
        discordBotService.sendOrderNotification(staff.getName() + "が " + timeCard.get().getStart().format(formatter) + "に出勤しました。");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
        return "出勤処理しました。";
    }

    @ResponseBody
    @RequestMapping(value = "/timeCardOut", method = RequestMethod.GET)
    public String getTimeCardOut(@RequestParam Map<String, Object> params) {
        Staff staff = staffService.getStaff(Long.valueOf(params.get("staffId").toString()));
        timeCardService.clockOut(staff);
        Optional<TimeCard> timeCard = timeCardService.getTimeCardByUserName(staff);
        discordBotService.sendOrderNotification(staff.getName() + "が " + timeCard.get().getEnd().format(formatter) + "に退社しました。");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
        return "退勤処理しました。";
    }
}
