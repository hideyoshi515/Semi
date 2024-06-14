package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.entity.Coupon;
import co.kr.necohost.semi.domain.repository.CouponRepository;
import org.springframework.stereotype.Service;

@Service
public class CouponService {
	private final CouponRepository couponRepository;

	public CouponService(CouponRepository couponRepository) {
		this.couponRepository = couponRepository;
	}

	public void saveCoupon(String couponCode) {
		Coupon coupon = new Coupon();

		coupon.setCouponNum(couponCode);
		coupon.setProcess(0);

		couponRepository.save(coupon);
	}

	public Coupon findByCouponNum(String couponNum) {
		return couponRepository.findByCouponNum(couponNum);
	}

	public void save(Coupon coupon) {
		couponRepository.save(coupon);
	}
}