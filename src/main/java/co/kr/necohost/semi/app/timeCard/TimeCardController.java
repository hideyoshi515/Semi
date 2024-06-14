package co.kr.necohost.semi.app.timeCard;

import co.kr.necohost.semi.domain.model.dto.StaffRequest;
import co.kr.necohost.semi.domain.model.entity.TimeCard;
import co.kr.necohost.semi.domain.service.DiscordBotService;
import co.kr.necohost.semi.domain.service.StaffService;
import co.kr.necohost.semi.domain.service.TimeCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class TimeCardController {
    private final TimeCardService timeCardService;
    private final StaffService staffService;
    private final DiscordBotService discordBotService;

    public TimeCardController(TimeCardService timeCardService, StaffService staffService, DiscordBotService discordBotService) {
        this.timeCardService = timeCardService;
        this.staffService = staffService;
        this.discordBotService = discordBotService;
    }

    @RequestMapping(value = "/timeCardList", method = RequestMethod.GET)
    public String getTimeCardList(Model model) {
        model.addAttribute("staffs", staffService.getAllStaff());
        model.addAttribute("timeCards", timeCardService.getAllTimeCard());
        return "timeCard/timeCardList.html";
    }

    @RequestMapping(value = "/timeCardInput", method = RequestMethod.GET)
    public String getTimeCardInput(Model model) {
        StaffRequest staffRequest = new StaffRequest();
        model.addAttribute("staffRequest", staffRequest);
        return "timeCard/timeCardInput.html";
    }

    @RequestMapping(value = "/timeCardIn", method = RequestMethod.POST)
    public ResponseEntity<String> postTimeCardIn(@RequestBody StaffRequest request) {
        try {
            Optional<TimeCard> timeCard = timeCardService.clockIn(request);
            discordBotService.sendOrderNotification(request.getUsername() + "이/가 " + timeCard.get().getStart() + "에 출근했습니다.");
            return ResponseEntity.ok("Clock in attempted.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @RequestMapping(value = "/timeCardOut", method = RequestMethod.POST)
    public ResponseEntity<String> postTimeCardOut(@RequestBody StaffRequest request) {
        try {
            Optional<TimeCard> timeCard = timeCardService.clockOut(request);
            discordBotService.sendOrderNotification(request.getUsername() + "이/가 " + timeCard.get().getEnd() + "에 퇴근했습니다.");
            return ResponseEntity.ok("Clock out attempted.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
