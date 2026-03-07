package cat.udl.eps.softarch.fll.controller.dto;

public record MatchTableAssignmentResponse(
		Long matchId,
		String tableIdentifier,
		String startTime,
		String endTime) {
}
