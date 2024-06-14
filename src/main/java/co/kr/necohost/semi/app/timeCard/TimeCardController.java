package co.kr.necohost.semi.app.timeCard;

import co.kr.necohost.semi.domain.model.dto.StaffRequest;
import co.kr.necohost.semi.domain.service.TimeCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class TimeCardController {
    private final TimeCardService timeCardService;

    public TimeCardController(TimeCardService timeCardService) {
        this.timeCardService = timeCardService;
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
            timeCardService.clockIn(request);
            return ResponseEntity.ok("Clock in attempted.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @RequestMapping(value = "/timeCardOut", method = RequestMethod.POST)
    public ResponseEntity<String> postTimeCardOut(@RequestBody StaffRequest request) {
        try {
            timeCardService.clockOut(request);
            return ResponseEntity.ok("Clock out attempted.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
