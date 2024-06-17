package co.kr.necohost.semi.domain.model.dto;

import co.kr.necohost.semi.domain.model.entity.TimeCard;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class TimeCardRequest {
    private long id;
    private String staffName;
    private String start;
    private String end;

    public TimeCardRequest(TimeCard timeCard) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.id = timeCard.getId();
        this.staffName = timeCard.getStaff().getName();
        this.start = timeCard.getStart().format(formatter);
        this.end = timeCard.getEnd()!=null ? timeCard.getEnd().format(formatter) : "記録なし";
    }
}
