package cat.udl.eps.softarch.fll.repository.projection;

public interface LeaderboardRowProjection {
	String getTeamId();

	String getTeamName();

	Long getTotalScore();

	Long getMatchesPlayed();
}
