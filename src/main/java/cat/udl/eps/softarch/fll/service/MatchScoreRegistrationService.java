package cat.udl.eps.softarch.fll.service;

import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.MatchResult;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.MatchRepository;
import cat.udl.eps.softarch.fll.repository.MatchResultRepository;

@Service
public class MatchScoreRegistrationService {

	private final MatchRepository matchRepository;
	private final MatchResultRepository matchResultRepository;
	private final RankingService rankingService;

	public MatchScoreRegistrationService(MatchRepository matchRepository,
			MatchResultRepository matchResultRepository,
			RankingService rankingService) {
		this.matchRepository = matchRepository;
		this.matchResultRepository = matchResultRepository;
		this.rankingService = rankingService;
	}

	@Transactional
	public void registerMatchScore(Long matchId, String teamAId, String teamBId, Integer teamAScore, Integer teamBScore) {
		validateRequestShape(matchId, teamAId, teamBId, teamAScore, teamBScore);

		Match match = matchRepository.findById(matchId)
				.orElseThrow(() -> new RegistrationException(
						ErrorCode.MATCH_NOT_FOUND,
						"Match with id " + matchId + " does not exist"));

		validateMatchState(match);

		if (matchResultRepository.existsByMatch(match)) {
			throw new RegistrationException(
					ErrorCode.RESULT_ALREADY_EXISTS,
					"A result has already been registered for this match");
		}

		validateScoreValues(teamAId, teamBId, teamAScore, teamBScore);
		Map<String, Integer> scoreByTeam = Map.of(
				teamAId, teamAScore,
				teamBId, teamBScore);

		Team matchTeamA = match.getTeamA();
		Team matchTeamB = match.getTeamB();
		validateTeamAssignment(matchTeamA, matchTeamB, teamAId, teamBId);

		List<MatchResult> matchResults = List.of(
				buildMatchResult(match, matchTeamA, scoreByTeam.get(matchTeamA.getId())),
				buildMatchResult(match, matchTeamB, scoreByTeam.get(matchTeamB.getId())));

		try {
			matchResultRepository.saveAllAndFlush(matchResults);
		} catch (DataIntegrityViolationException exception) {
			throw new RegistrationException(
					ErrorCode.RESULT_ALREADY_EXISTS,
					"A result has already been registered for this match");
		}
		rankingService.recalculateRanking();
	}

	private void validateRequestShape(Long matchId, String teamAId, String teamBId, Integer teamAScore, Integer teamBScore) {
		if (matchId == null || teamAId == null || teamBId == null || teamAScore == null || teamBScore == null) {
			throw new RegistrationException(
					ErrorCode.INVALID_SCORE,
					"Invalid score payload");
		}
	}

	private void validateMatchState(Match match) {
		if (match.getStartTime() == null) {
			throw new RegistrationException(
					ErrorCode.INVALID_MATCH_STATE,
					"Match is not in a valid state for result submission");
		}
		if (match.getEndTime() == null) {
			throw new RegistrationException(
					ErrorCode.MATCH_NOT_FINISHED,
					"Match must be finished before registering the result");
		}
		if (match.getEndTime().isBefore(match.getStartTime())) {
			throw new RegistrationException(
					ErrorCode.INVALID_MATCH_STATE,
					"Match end time cannot be before start time");
		}
	}

	private void validateScoreValues(String teamAId, String teamBId, Integer teamAScore, Integer teamBScore) {
		if (teamAId.equals(teamBId)) {
			throw new RegistrationException(
					ErrorCode.INVALID_SCORE,
					"A match result requires two different teams");
		}
		if (teamAScore < 0 || teamBScore < 0) {
			throw new RegistrationException(
					ErrorCode.INVALID_SCORE,
					"Score cannot be negative");
		}
	}

	private void validateTeamAssignment(Team matchTeamA, Team matchTeamB, String providedTeamA, String providedTeamB) {
		if (matchTeamA == null || matchTeamB == null) {
			throw new RegistrationException(
					ErrorCode.INVALID_MATCH_STATE,
					"Match teams are not assigned");
		}

		boolean directMatch = providedTeamA.equals(matchTeamA.getId())
				&& providedTeamB.equals(matchTeamB.getId());
		boolean reverseMatch = providedTeamA.equals(matchTeamB.getId())
				&& providedTeamB.equals(matchTeamA.getId());

		if (!directMatch && !reverseMatch) {
			throw new RegistrationException(
					ErrorCode.TEAM_MISMATCH,
					"Provided team IDs do not match the teams assigned to the match");
		}
	}

	private MatchResult buildMatchResult(Match match, Team team, Integer score) {
		MatchResult matchResult = new MatchResult();
		matchResult.setMatch(match);
		matchResult.setTeam(team);
		matchResult.setScore(score);
		return matchResult;
	}

	public static class RegistrationException extends RuntimeException {
		private final ErrorCode errorCode;

		public RegistrationException(ErrorCode errorCode, String message) {
			super(message);
			this.errorCode = errorCode;
		}

		public ErrorCode getErrorCode() {
			return errorCode;
		}

		public HttpStatus getStatus() {
			return errorCode.getStatus();
		}
	}

	public enum ErrorCode {
		MATCH_NOT_FOUND(HttpStatus.NOT_FOUND),
		INVALID_MATCH_STATE(HttpStatus.BAD_REQUEST),
		MATCH_NOT_FINISHED(HttpStatus.BAD_REQUEST),
		RESULT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST),
		INVALID_SCORE(HttpStatus.BAD_REQUEST),
		TEAM_MISMATCH(HttpStatus.BAD_REQUEST);

		private final HttpStatus status;

		ErrorCode(HttpStatus status) {
			this.status = status;
		}

		public HttpStatus getStatus() {
			return status;
		}
	}
}
