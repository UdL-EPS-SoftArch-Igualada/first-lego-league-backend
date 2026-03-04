package cat.udl.eps.softarch.fll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignCoachRequest {
	@NotBlank
	private String teamId;
	@NotNull
	private Integer coachId;
}