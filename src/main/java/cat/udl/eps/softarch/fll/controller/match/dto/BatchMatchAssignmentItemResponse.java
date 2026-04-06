package cat.udl.eps.softarch.fll.controller.match.dto;

public record BatchMatchAssignmentItemResponse(
	String matchId,
	String refereeId,
	String status
) {
}
