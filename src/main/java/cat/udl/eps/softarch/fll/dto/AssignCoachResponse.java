package cat.udl.eps.softarch.fll.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssignCoachResponse {
	private String teamId;
	private Integer coachId;
	private String status;
}