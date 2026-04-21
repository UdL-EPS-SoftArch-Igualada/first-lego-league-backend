package cat.udl.eps.softarch.fll.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignAwardRequest(
		@NotBlank String awardId,
		@NotBlank String teamId) {
}
