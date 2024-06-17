package co.kr.necohost.semi.app.timeCard;

import co.kr.necohost.semi.domain.model.dto.StaffRequest;
import co.kr.necohost.semi.domain.model.dto.TimeCardRequest;
import co.kr.necohost.semi.domain.model.entity.TimeCard;
import co.kr.necohost.semi.domain.service.DiscordBotService;
import co.kr.necohost.semi.domain.service.StaffService;
import co.kr.necohost.semi.domain.service.TimeCardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
public class TimeCardController {
    private final TimeCardService timeCardService;
    private final DiscordBotService discordBotService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm");

    public TimeCardController(TimeCardService timeCardService, StaffService staffService, DiscordBotService discordBotService) {
        this.timeCardService = timeCardService;
        this.discordBotService = discordBotService;
    }

    /*@RequestMapping(value = "/timeCardList", method = RequestMethod.GET)
    public String getTimeCardList(Model model) {
        List<TimeCardRequest> timeCards = timeCardService.getAllTimeCardDesc();
        model.addAttribute("timeCards", timeCards);
        return "timeCard/timeCardList.html";
    }*/

    @RequestMapping(value = "/timeCardInput", method = RequestMethod.GET)
    public String getTimeCardInput(Model model) {
        StaffRequest staffRequest = new StaffRequest();
        model.addAttribute("staffRequest", staffRequest);
        return "timeCard/timeCardInput.html";
    }

    @RequestMapping(value = "/timeCardIn", method = RequestMethod.POST)
    public ResponseEntity<String> postTimeCardIn(@RequestBody StaffRequest request) {
        try {
            timeCardService.clockIn(request);
            Optional<TimeCard> timeCard = timeCardService.getTimeCardByUserName(request);
            discordBotService.sendOrderNotification(request.getUsername() + "이/가 " + timeCard.get().getStart().format(formatter) + "에 출근했습니다.");
            return ResponseEntity.ok("Clock in attempted.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @RequestMapping(value = "/timeCardOut", method = RequestMethod.POST)
    public ResponseEntity<String> postTimeCardOut(@RequestBody StaffRequest request) {
        try {
            timeCardService.clockOut(request);
            Optional<TimeCard> timeCard = timeCardService.getTimeCardByUserName(request);
            discordBotService.sendOrderNotification(request.getUsername() + "이/가 " + timeCard.get().getEnd().format(formatter) + "에 퇴근했습니다.");
            return ResponseEntity.ok("Clock out attempted.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
