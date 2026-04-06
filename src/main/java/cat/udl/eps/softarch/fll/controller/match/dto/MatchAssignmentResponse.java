package cat.udl.eps.softarch.fll.controller.match.dto;

public record MatchAssignmentResponse(
	String matchId,
	String refereeId,
	String status
) {
}
