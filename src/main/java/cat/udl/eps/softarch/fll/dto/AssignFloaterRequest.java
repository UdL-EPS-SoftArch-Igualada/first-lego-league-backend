package cat.udl.eps.softarch.fll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignFloaterRequest {

	@NotBlank
	private String teamId;

	@NotNull
	private Long floaterId;
}
