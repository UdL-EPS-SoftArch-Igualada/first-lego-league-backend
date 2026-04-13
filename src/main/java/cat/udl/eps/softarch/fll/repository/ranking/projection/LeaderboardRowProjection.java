package cat.udl.eps.softarch.fll.repository.ranking.projection;

public interface LeaderboardRowProjection {
	Long getTeamId();
	String getTeamName();
	Integer getTotalScore();
	Long getMatchesPlayed();
}