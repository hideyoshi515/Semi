package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.entity.Coupon;
import co.kr.necohost.semi.domain.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponService {
	private final CouponRepository couponRepository;
	private final DiscordBotService discordBotService;

	public CouponService(CouponRepository couponRepository, DiscordBotService discordBotService) {
		this.couponRepository = couponRepository;
		this.discordBotService = discordBotService;
	}

	// クーポンを保存
	public void saveCoupon(String couponCode) {
		Coupon coupon = new Coupon();
		LocalDateTime now = LocalDateTime.now();

		coupon.setCouponNum(couponCode);
		coupon.setDate(now);
		coupon.setProcess(0);

		couponRepository.save(coupon);
	}

	public boolean isCouponExists(String couponCode) {
		return couponRepository.findByCouponNum(couponCode) != null;
	}

	public void deleteOldCoupons() {
//		LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
		LocalDateTime oneWeekAgo = LocalDateTime.now().minusMinutes(5);

		List<Coupon> oldCoupons = couponRepository.findAllByDateBefore(oneWeekAgo);

		for (Coupon coupon : oldCoupons) {
			discordBotService.sendOrderNotification("クーポンの使用時間が過ぎました : " + coupon.getCouponNum());
		}

		couponRepository.deleteAll(oldCoupons);
	}

	public Coupon findByCouponNum(String couponNum) {
		return couponRepository.findByCouponNum(couponNum);
	}

	public void save(Coupon coupon) {
		couponRepository.save(coupon);
	}
}
