package cat.udl.eps.softarch.fll.service;

import cat.udl.eps.softarch.fll.domain.MatchResult;
import cat.udl.eps.softarch.fll.domain.Team;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Primary
@Component
public class TotalScoreRankingCalculator extends RankingCalculator {

    @Override
    protected Map<Team, Integer> computeScorePerTeam(List<MatchResult> results) {
        Map<Team, Integer> totalScore = new HashMap<>();
        for (MatchResult result : results) {
            totalScore.merge(result.getTeam(), result.getScore(), Integer::sum);
        }
        return totalScore;
    }
}
