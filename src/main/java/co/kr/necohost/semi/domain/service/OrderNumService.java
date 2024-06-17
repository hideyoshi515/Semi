package co.kr.necohost.semi.domain.service;

import co.kr.necohost.semi.domain.model.entity.OrderNum;
import co.kr.necohost.semi.domain.repository.OrderNumRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderNumService {
	private final OrderNumRepository repo;

	public OrderNumService(OrderNumRepository repo) {
		this.repo = repo;
	}

	// 注文番号を保存
	public OrderNum save(OrderNum orderNum) {
		return repo.save(orderNum);
	}
}
