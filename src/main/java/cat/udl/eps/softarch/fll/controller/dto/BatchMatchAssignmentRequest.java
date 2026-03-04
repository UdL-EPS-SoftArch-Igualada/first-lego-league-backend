package cat.udl.eps.softarch.fll.controller.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record BatchMatchAssignmentRequest(
		@NotBlank String roundId,
		@NotEmpty List<@Valid BatchMatchAssignmentItemRequest> assignments
) {}
