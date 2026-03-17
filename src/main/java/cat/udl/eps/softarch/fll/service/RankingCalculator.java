package cat.udl.eps.softarch.fll.service;

import cat.udl.eps.softarch.fll.domain.MatchResult;
import cat.udl.eps.softarch.fll.domain.Ranking;
import cat.udl.eps.softarch.fll.domain.Team;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public abstract class RankingCalculator {

	public final List<Ranking> calculate(List<MatchResult> results) {
		Map<Team, Integer> scoreByTeam = computeScorePerTeam(results);
		return buildRankings(scoreByTeam);
	}

	protected abstract Map<Team, Integer> computeScorePerTeam(List<MatchResult> results);

	private List<Ranking> buildRankings(Map<Team, Integer> scoreByTeam) {
		List<Map.Entry<Team, Integer>> sorted = new ArrayList<>(scoreByTeam.entrySet());
		sorted.sort(
			Comparator.comparing(Map.Entry<Team, Integer>::getValue, Comparator.reverseOrder())
				.thenComparing(e -> e.getKey().getId())
		);

		List<Ranking> rankings = new ArrayList<>();
		int position = 1;
		for (Map.Entry<Team, Integer> entry : sorted) {
			Ranking ranking = new Ranking();
			ranking.setTeam(entry.getKey());
			ranking.setTotalScore(entry.getValue());
			ranking.setPosition(position++);
			rankings.add(ranking);
		}
		return rankings;
	}
}
