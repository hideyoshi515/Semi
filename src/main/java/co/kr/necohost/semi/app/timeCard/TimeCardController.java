package co.kr.necohost.semi.app.timeCard;

import co.kr.necohost.semi.domain.service.TimeCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/timeCard")
public class TimeCardController {
    @Autowired
    private TimeCardService timeCardService;

    @PostMapping("/clockin/{staffId}")
    public ResponseEntity<String> clockIn(@PathVariable Long staffId) {
        timeCardService.clockIn(staffId);
        return ResponseEntity.ok("Clock in attempted.");
    }

    @PostMapping("/clockout/{staffId}")
    public ResponseEntity<String> clockOut(@PathVariable Long staffId) {
        timeCardService.clockOut(staffId);
        return ResponseEntity.ok("Clock out attempted.");
    }
}
