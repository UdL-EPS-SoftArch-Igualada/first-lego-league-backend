package cat.udl.eps.softarch.fll.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cat.udl.eps.softarch.fll.domain.MatchResult;
import cat.udl.eps.softarch.fll.domain.Ranking;
import cat.udl.eps.softarch.fll.repository.MatchResultRepository;
import cat.udl.eps.softarch.fll.repository.RankingRepository;

@Service
public class RankingService {

	private final MatchResultRepository matchResultRepository;
	private final RankingRepository rankingRepository;
	private final RankingCalculator rankingCalculator;

	public RankingService(MatchResultRepository matchResultRepository,
						  RankingRepository rankingRepository,
						  RankingCalculator rankingCalculator) {
		this.matchResultRepository = matchResultRepository;
		this.rankingRepository = rankingRepository;
		this.rankingCalculator = rankingCalculator;
	}

	@Transactional
	public void recalculateRanking() {
		List<MatchResult> allResults = matchResultRepository.findAll();
		List<Ranking> rankings = rankingCalculator.calculate(allResults);
		rankingRepository.deleteAllInBatch();
		rankingRepository.saveAll(rankings);
	}
}
