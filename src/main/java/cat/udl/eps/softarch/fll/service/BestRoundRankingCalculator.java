package cat.udl.eps.softarch.fll.service;

import cat.udl.eps.softarch.fll.domain.MatchResult;
import cat.udl.eps.softarch.fll.domain.Team;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BestRoundRankingCalculator extends RankingCalculator {

    @Override
    protected Map<Team, Integer> computeScorePerTeam(List<MatchResult> results) {
        Map<Team, Integer> bestScore = new HashMap<>();
        for (MatchResult result : results) {
            bestScore.merge(result.getTeam(), result.getScore(), Math::max);
        }
        return bestScore;
    }
}
