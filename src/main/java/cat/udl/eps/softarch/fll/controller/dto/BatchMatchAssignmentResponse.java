package cat.udl.eps.softarch.fll.controller.dto;

import java.util.List;

public record BatchMatchAssignmentResponse(
		String roundId,
		String status,
		int processed,
		List<BatchMatchAssignmentItemResponse> assignments
) {
	public BatchMatchAssignmentResponse {
		if (assignments == null || processed != assignments.size()) {
			throw new IllegalArgumentException("processed must match assignments size");
		}
	}
}
