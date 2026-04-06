package cat.udl.eps.softarch.fll.controller.match.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record BatchMatchAssignmentRequest(
	@NotBlank String roundId,
	@NotEmpty List<@NotNull @Valid BatchMatchAssignmentItemRequest> assignments
) {
}
