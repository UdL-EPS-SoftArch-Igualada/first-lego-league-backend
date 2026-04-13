package cat.udl.eps.softarch.fll.controller.ranking.dto;

public record LeaderboardItemResponse(
	int position,
	Long teamId,
	String teamName,
	Integer totalScore,
	Long matchesPlayed
) {
}
