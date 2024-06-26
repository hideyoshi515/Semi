package co.kr.necohost.semi.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Sales {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private LocalDateTime date;
	private long category;
	private long menu;
	private int price;
	private int quantity;
	private long device;
	private long deviceNum;
	private long orderNum;
	private int process;

	@Override
	public String toString() {
		return "Sales{" +
				"id=" + id +
				", date=" + date +
				", category=" + category +
				", menu=" + menu +
				", price=" + price +
				", quantity=" + quantity +
				", device=" + device +
				", deviceNum=" + deviceNum +
				", orderNum=" + orderNum +
				", process=" + process +
				'}';
	}
}