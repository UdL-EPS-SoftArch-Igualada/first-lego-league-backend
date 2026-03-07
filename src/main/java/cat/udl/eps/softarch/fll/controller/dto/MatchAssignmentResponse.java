package cat.udl.eps.softarch.fll.controller.dto;

public record MatchAssignmentResponse(
		String matchId,
		String refereeId,
		String status
) {}
