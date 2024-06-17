package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.dto.StaffRequest;
import co.kr.necohost.semi.domain.model.dto.TimeCardRequest;
import co.kr.necohost.semi.domain.model.entity.Staff;
import co.kr.necohost.semi.domain.model.entity.TimeCard;
import co.kr.necohost.semi.domain.repository.StaffRepository;
import co.kr.necohost.semi.domain.repository.TimeCardRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TimeCardService {

	private final StaffRepository staffRepository;
	private final TimeCardRepository timeCardRepository;

	public TimeCardService(StaffRepository staffRepository, TimeCardRepository timeCardRepository) {
		this.staffRepository = staffRepository;
		this.timeCardRepository = timeCardRepository;
	}

	// 出勤を記録
	public void clockIn(StaffRequest request) {
		Staff staff = staffRepository.findByUsername(request.getUsername())
				.orElseThrow(() -> new IllegalArgumentException("Invalid username"));

		Optional<TimeCard> recentTimeCard = timeCardRepository.findTopByStaffOrderByStartDesc(staff);
		LocalDateTime now = LocalDateTime.now();

		if (recentTimeCard.isPresent() && recentTimeCard.get().getStart().toLocalDate().isEqual(now.toLocalDate())) {
			// 既に出勤記録がある場合
			System.out.println("Already clocked in today.");
			return;
		}

		// 新しいタイムカードを作成
		TimeCard timeCard = new TimeCard();
		timeCard.setStaff(staff);
		timeCard.setStart(now);
		timeCardRepository.save(timeCard);
		System.out.println("Clock in recorded.");
	}

	// 退勤を記録
	public void clockOut(StaffRequest request) {
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
			// 出勤記録がない場合のエラーメッセージ
			System.out.println("Cannot clock out without clocking in today.");
		}
	}

	// すべてのタイムカードを取得
	public List<TimeCard> getAllTimeCard() {
		return timeCardRepository.findAll();
	}

	public List<TimeCardRequest> getAllTimeCardDesc() {
		return timeCardRepository.findAllByOrderByStartDesc().stream()
				.map(TimeCardRequest::new)
				.collect(Collectors.toList());
	}

	// ユーザー名でタイムカードを取得
	public Optional<TimeCard> getTimeCardByUserName(StaffRequest request){
		Staff staff = staffRepository.findByUsername(request.getUsername())
				.orElseThrow(() -> new IllegalArgumentException("Invalid username"));
		return timeCardRepository.findTopByStaffOrderByStartDesc(staff);
	}
}
