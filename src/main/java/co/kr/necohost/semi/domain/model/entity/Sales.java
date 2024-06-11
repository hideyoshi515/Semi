package co.kr.necohost.semi.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Sales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime date;
    private int category;
    private int menu;
    private int price;
    private int quantity;
    private int device;
    private int deviceNum;
    private int orderNum;
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