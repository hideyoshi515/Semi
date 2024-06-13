package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.entity.Staff;
import co.kr.necohost.semi.domain.model.entity.TimeCard;
import co.kr.necohost.semi.domain.repository.StaffRepository;
import co.kr.necohost.semi.domain.repository.TimeCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TimeCardService {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private TimeCardRepository timecardRepository;

    public void clockIn(Long staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid staff ID"));

        Optional<TimeCard> recentTimecard = timecardRepository.findTopByStaffOrderByStartDesc(staff);
        LocalDateTime now = LocalDateTime.now();

        if (recentTimecard.isPresent() && recentTimecard.get().getStart().toLocalDate().isEqual(now.toLocalDate())) {
            // 중복 출근
            System.out.println("Already clocked in today.");
            return;
        }

        // 새로운 타임카드 생성
        TimeCard timecard = new TimeCard();
        timecard.setStaff(staff);
        timecard.setStart(now);
        timecardRepository.save(timecard);
        System.out.println("Clock in recorded.");
    }

    public void clockOut(Long staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid staff ID"));

        Optional<TimeCard> recentTimecard = timecardRepository.findTopByStaffOrderByStartDesc(staff);
        LocalDateTime now = LocalDateTime.now();

        if (recentTimecard.isPresent() && recentTimecard.get().getStart().toLocalDate().isEqual(now.toLocalDate())) {
            TimeCard timecard = recentTimecard.get();
            if (timecard.getEnd() == null) {
                timecard.setEnd(now);
                timecardRepository.save(timecard);
                System.out.println("Clock out recorded.");
            } else {
                System.out.println("Already clocked out today.");
            }
        } else {
            // 현재 시각의 날짜와 다르다면 오류 발생
            System.out.println("Cannot clock out without clocking in today.");
        }
    }
}
