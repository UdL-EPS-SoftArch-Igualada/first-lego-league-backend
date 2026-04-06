package cat.udl.eps.softarch.fll.controller.match.dto;

import jakarta.validation.constraints.NotBlank;

public record MatchAssignmentRequest(
	@NotBlank String matchId,
	@NotBlank String refereeId
) {
}
