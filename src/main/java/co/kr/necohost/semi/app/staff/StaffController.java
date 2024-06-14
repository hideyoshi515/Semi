package co.kr.necohost.semi.app.staff;

import co.kr.necohost.semi.domain.model.dto.StaffRequest;
import co.kr.necohost.semi.domain.model.entity.Position;
import co.kr.necohost.semi.domain.service.PositionService;
import co.kr.necohost.semi.domain.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class StaffController {

    private final StaffService staffService;
    private final PositionService positionService;

    @Autowired
    public StaffController(StaffService staffService, PositionService positionService) {
        this.staffService = staffService;
        this.positionService = positionService;
    }

    @RequestMapping(value = "/insertStaff", method = RequestMethod.GET)
    public String getInsertStaff(Model model) {
        model.addAttribute("staffRequest", new StaffRequest());
        model.addAttribute("positions", positionService.getAllPositions());
        return "staff/staffInsert.html";
    }

    @RequestMapping(value = "/insertStaff", method = RequestMethod.POST)
    public String postInsertStaff(@ModelAttribute("staffRequest") StaffRequest staffRequest, Model model) {
        try {
            Position position = positionService.getPositionById(staffRequest.getPosition().getId());
            staffRequest.setPosition(position);
            staffService.insertStaff(staffRequest.toEntity());
            model.addAttribute("successMessage", "Staff successfully created.");
            return "redirect:/staffList"; // 성공 시 스태프 목록 페이지로 리디렉션
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating staff: " + e.getMessage());
            return "staff/staffInsert.html"; // 오류 시 현재 페이지로 다시 표시
        }
    }

    @RequestMapping(value = "/staffList", method = RequestMethod.GET)
    public String listStaff(Model model) {
        model.addAttribute("staffs", staffService.getAllStaff());
        return "staff/staffList.html";
    }
}
