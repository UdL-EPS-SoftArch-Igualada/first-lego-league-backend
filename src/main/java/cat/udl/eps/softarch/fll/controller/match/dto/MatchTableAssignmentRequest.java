package cat.udl.eps.softarch.fll.controller.match.dto;

import jakarta.validation.constraints.NotBlank;

public record MatchTableAssignmentRequest(
	@NotBlank String tableIdentifier) {
}
