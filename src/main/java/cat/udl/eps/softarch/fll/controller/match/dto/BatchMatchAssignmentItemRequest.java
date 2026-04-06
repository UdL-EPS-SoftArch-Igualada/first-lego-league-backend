package cat.udl.eps.softarch.fll.controller.match.dto;

import jakarta.validation.constraints.NotBlank;

public record BatchMatchAssignmentItemRequest(
	@NotBlank String matchId,
	@NotBlank String refereeId
) {
}
