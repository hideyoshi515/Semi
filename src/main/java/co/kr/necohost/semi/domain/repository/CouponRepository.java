package co.kr.necohost.semi.domain.repository;

import co.kr.necohost.semi.domain.model.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {
}
