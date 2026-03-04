package cat.udl.eps.softarch.fll.controller.dto;

import java.util.List;

public record BatchMatchAssignmentResponse(
		String roundId,
		String status,
		int processed,
		List<BatchMatchAssignmentItemResponse> assignments
) {}
