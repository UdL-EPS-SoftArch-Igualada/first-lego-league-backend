package cat.udl.eps.softarch.fll.controller.dto;

public record BatchMatchAssignmentItemResponse(
		String matchId,
		String refereeId,
		String status
) {}
