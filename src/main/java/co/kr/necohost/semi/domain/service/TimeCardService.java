package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.dto.StaffRequest;
import co.kr.necohost.semi.domain.model.entity.Staff;
import co.kr.necohost.semi.domain.model.entity.TimeCard;
import co.kr.necohost.semi.domain.repository.StaffRepository;
import co.kr.necohost.semi.domain.repository.TimeCardRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TimeCardService {

	private final StaffRepository staffRepository;

	private final TimeCardRepository timeCardRepository;

	public TimeCardService(StaffRepository staffRepository, TimeCardRepository timecardRepository) {
		this.staffRepository = staffRepository;
		this.timeCardRepository = timecardRepository;
	}

	public List<TimeCard> getAllTimeCard() {
		return timeCardRepository.findAll();
	}

//	public Optional<TimeCard> getTimeCardByUserName(StaffRequest request){
//		Staff staff = staffRepository.findByUsername(request.getUsername())
//				.orElseThrow(() -> new IllegalArgumentException("Invalid username"));
//        return timeCardRepository.findTopByStaffOrderByStartDesc(staff);
//	}

	public Optional<TimeCard> clockIn(StaffRequest request) {
		Staff staff = staffRepository.findByUsername(request.getUsername())
				.orElseThrow(() -> new IllegalArgumentException("Invalid username"));

		Optional<TimeCard> recentTimeCard = timeCardRepository.findTopByStaffOrderByStartDesc(staff);
		LocalDateTime now = LocalDateTime.now();

		if (recentTimeCard.isPresent() && recentTimeCard.get().getStart().toLocalDate().isEqual(now.toLocalDate())) {
			// 중복 출근
			System.out.println("Already clocked in today.");
			return recentTimeCard;
		}

		// 새로운 타임카드 생성
		TimeCard timeCard = new TimeCard();
		timeCard.setStaff(staff);
		timeCard.setStart(now);
		timeCardRepository.save(timeCard);
		System.out.println("Clock in recorded.");

		return recentTimeCard;
	}

	public Optional<TimeCard> clockOut(StaffRequest request) {
		Staff staff = staffRepository.findByUsername(request.getUsername())
				.orElseThrow(() -> new IllegalArgumentException("Invalid username"));

		Optional<TimeCard> recentTimeCard = timeCardRepository.findTopByStaffOrderByStartDesc(staff);
		LocalDateTime now = LocalDateTime.now();

		if (recentTimeCard.isPresent() && recentTimeCard.get().getStart().toLocalDate().isEqual(now.toLocalDate())) {
			TimeCard timeCard = recentTimeCard.get();
			if (timeCard.getEnd() == null) {
				timeCard.setEnd(now);
				timeCardRepository.save(timeCard);
				System.out.println("Clock out recorded.");
			} else {
				System.out.println("Already clocked out today.");
			}
		} else {
			// 현재 시각의 날짜와 다르다면 오류 발생
			System.out.println("Cannot clock out without clocking in today.");
		}

		return recentTimeCard;
	}
}