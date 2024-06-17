package co.kr.necohost.semi.domain.model.dto;

import co.kr.necohost.semi.domain.model.entity.Coupon;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponRequest {
	private long id;
	private String couponNum;
	private LocalDateTime date;
	private int process;

	public Coupon toEntity() {
		Coupon coupon = new Coupon();

		coupon.setId(id);
		coupon.setCouponNum(couponNum);
		coupon.setDate(date);
		coupon.setProcess(process);

		return coupon;
	}
}
