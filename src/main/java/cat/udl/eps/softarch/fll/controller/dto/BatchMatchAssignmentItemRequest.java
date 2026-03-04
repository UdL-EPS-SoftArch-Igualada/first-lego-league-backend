package cat.udl.eps.softarch.fll.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record BatchMatchAssignmentItemRequest(
		@NotBlank String matchId,
		@NotBlank String refereeId
) {}
