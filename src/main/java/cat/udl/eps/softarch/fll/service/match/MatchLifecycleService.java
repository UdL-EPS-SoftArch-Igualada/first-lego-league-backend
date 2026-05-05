package cat.udl.eps.softarch.fll.service.match;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cat.udl.eps.softarch.fll.domain.match.Match;
import cat.udl.eps.softarch.fll.domain.match.MatchState;
import cat.udl.eps.softarch.fll.exception.MatchLifecycleException;
import cat.udl.eps.softarch.fll.repository.match.MatchRepository;

@Service
public class MatchLifecycleService {

	private final MatchRepository matchRepository;

	public MatchLifecycleService(MatchRepository matchRepository) {
		this.matchRepository = matchRepository;
	}

	@Transactional
	public TransitionResult changeState(Long matchId, MatchState targetState) {
		Match match = matchRepository.findByIdForUpdate(matchId)
			.orElseThrow(() -> new MatchLifecycleException(
				"MATCH_NOT_FOUND", "Match with id " + matchId + " not found"));

		MatchState currentState = match.getState();
		if (!isValidTransition(currentState, targetState)) {
			throw new MatchLifecycleException(
				"INVALID_MATCH_STATE_TRANSITION",
				"Invalid transition from " + currentState + " to " + targetState);
	}

		match.setState(targetState);
		matchRepository.save(match);
		return new TransitionResult(match.getId(), currentState, targetState);
	}

	boolean isValidTransition(MatchState current, MatchState target) {
		return switch (current) {
			case SCHEDULED -> target == MatchState.IN_PROGRESS;
			case IN_PROGRESS -> target == MatchState.FINISHED;
			case FINISHED -> false;
		};
	}

	public record TransitionResult(Long matchId, MatchState previousState, MatchState newState) {}
}