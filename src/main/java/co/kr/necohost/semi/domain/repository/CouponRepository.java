package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {
	Coupon findByCouponNum(String couponNum);

	List<Coupon> findAllByDateBefore(LocalDateTime date);
}