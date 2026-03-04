package cat.udl.eps.softarch.fll.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cat.udl.eps.softarch.fll.domain.MatchResult;
import cat.udl.eps.softarch.fll.domain.Ranking;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.MatchResultRepository;
import cat.udl.eps.softarch.fll.repository.RankingRepository;

@Service
public class RankingService {

	private final MatchResultRepository matchResultRepository;
	private final RankingRepository rankingRepository;

	public RankingService(MatchResultRepository matchResultRepository, RankingRepository rankingRepository) {
		this.matchResultRepository = matchResultRepository;
		this.rankingRepository = rankingRepository;
	}

	@Transactional
	public void recalculateRanking() {
		List<MatchResult> allResults = matchResultRepository.findAll();
		Map<Team, Integer> totalScoreByTeam = new HashMap<>();

		for (MatchResult result : allResults) {
			totalScoreByTeam.merge(result.getTeam(), result.getScore(), Integer::sum);
		}

		List<Map.Entry<Team, Integer>> orderedEntries = new ArrayList<>(totalScoreByTeam.entrySet());
		orderedEntries.sort(
				Comparator
						.comparing((Map.Entry<Team, Integer> entry) -> entry.getValue(), Comparator.reverseOrder())
						.thenComparing(entry -> entry.getKey().getId())
		);

		rankingRepository.deleteAllInBatch();

		List<Ranking> rankings = new ArrayList<>();
		int position = 1;
		for (Map.Entry<Team, Integer> entry : orderedEntries) {
			Ranking ranking = new Ranking();
			ranking.setTeam(entry.getKey());
			ranking.setTotalScore(entry.getValue());
			ranking.setPosition(position++);
			rankings.add(ranking);
		}

		rankingRepository.saveAll(rankings);
	}
}
