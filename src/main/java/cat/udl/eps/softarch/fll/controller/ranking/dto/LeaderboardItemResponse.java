package cat.udl.eps.softarch.fll.controller.ranking.dto;

public record LeaderboardItemResponse(
	int position,
	String teamId,
	String teamName,
	Long totalScore,
	Long matchesPlayed
) {
}
