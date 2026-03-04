package cat.udl.eps.softarch.fll.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cat.udl.eps.softarch.fll.controller.dto.BatchMatchAssignmentItemRequest;
import cat.udl.eps.softarch.fll.controller.dto.BatchMatchAssignmentItemResponse;
import cat.udl.eps.softarch.fll.controller.dto.BatchMatchAssignmentResponse;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.MatchState;
import cat.udl.eps.softarch.fll.domain.Referee;
import cat.udl.eps.softarch.fll.domain.Round;
import cat.udl.eps.softarch.fll.domain.Volunteer;
import cat.udl.eps.softarch.fll.exception.MatchAssignmentErrorCode;
import cat.udl.eps.softarch.fll.exception.MatchAssignmentException;
import cat.udl.eps.softarch.fll.repository.MatchRepository;
import cat.udl.eps.softarch.fll.repository.RoundRepository;
import cat.udl.eps.softarch.fll.repository.VolunteerRepository;

@Service
public class MatchAssignmentService {
	private final MatchRepository matchRepository;
	private final RoundRepository roundRepository;
	private final VolunteerRepository volunteerRepository;

	public MatchAssignmentService(
			MatchRepository matchRepository,
			RoundRepository roundRepository,
			VolunteerRepository volunteerRepository) {
		this.matchRepository = matchRepository;
		this.roundRepository = roundRepository;
		this.volunteerRepository = volunteerRepository;
	}

	@Transactional
	public Match assignReferee(String matchId, String refereeId) {
		Long parsedMatchId = parseIdOrThrow(matchId, null, matchId, refereeId);
		Long parsedRefereeId = parseIdOrThrow(refereeId, null, matchId, refereeId);

		Match match = matchRepository.findByIdForUpdate(parsedMatchId)
				.orElseThrow(() -> assignmentException(
						MatchAssignmentErrorCode.MATCH_NOT_FOUND,
						"Match not found: " + matchId,
						null,
						matchId,
						refereeId));

		validateMatchForAssignment(match, null, matchId, refereeId);
		Referee referee = resolveReferee(parsedRefereeId, refereeId, null, matchId, refereeId);
		validateAvailability(match, referee, null, matchId, refereeId);

		match.setReferee(referee);
		return matchRepository.save(match);
	}

	/**
	 * Assigns referees to multiple matches of one round in a single atomic operation.
	 *
	 * Preconditions: `roundId` and all item IDs are numeric strings; every assignment item references an existing
	 * scheduled/non-finished match from the given round and a volunteer with Referee role.
	 * Postconditions: either all assignments are persisted, or none are persisted if any validation fails.
	 * Exceptions: throws {@link MatchAssignmentException} with per-item details for batch failures.
	 * Atomicity: all-or-nothing due to transactional execution.
	 */
	@Transactional
	public BatchMatchAssignmentResponse assignBatch(String roundId, List<BatchMatchAssignmentItemRequest> assignments) {
		Long parsedRoundId = parseIdOrThrow(roundId, null, null, null);
		Round round = roundRepository.findById(parsedRoundId)
				.orElseThrow(() -> new MatchAssignmentException(
						MatchAssignmentErrorCode.ROUND_NOT_FOUND,
						"Round not found: " + roundId));

		List<ResolvedAssignmentCandidate> candidates = new ArrayList<>();
		Set<Long> seenMatchIds = new HashSet<>();

		for (int i = 0; i < assignments.size(); i++) {
			int assignmentIndex = i;
			BatchMatchAssignmentItemRequest item = assignments.get(i);
			Long parsedMatchId = parseIdOrThrow(item.matchId(), assignmentIndex, item.matchId(), item.refereeId());
			Long parsedRefereeId = parseIdOrThrow(item.refereeId(), assignmentIndex, item.matchId(), item.refereeId());

			if (!seenMatchIds.add(parsedMatchId)) {
				throw new MatchAssignmentException(
							MatchAssignmentErrorCode.MATCH_ALREADY_HAS_REFEREE,
							"Match appears multiple times in the same batch",
							assignmentIndex,
							item.matchId(),
							item.refereeId());
			}

			Match match = matchRepository.findByIdForUpdate(parsedMatchId)
					.orElseThrow(() -> new MatchAssignmentException(
								MatchAssignmentErrorCode.MATCH_NOT_FOUND,
								"Match not found: " + item.matchId(),
								assignmentIndex,
								item.matchId(),
								item.refereeId()));

			if (match.getRound() == null || !round.getId().equals(match.getRound().getId())) {
				throw new MatchAssignmentException(
							MatchAssignmentErrorCode.INVALID_MATCH_STATE,
							"Match does not belong to the provided round",
							assignmentIndex,
							item.matchId(),
							item.refereeId());
			}

			validateMatchForAssignment(match, assignmentIndex, item.matchId(), item.refereeId());
			Referee referee = resolveReferee(parsedRefereeId, item.refereeId(), assignmentIndex, item.matchId(), item.refereeId());
			validateAvailability(match, referee, assignmentIndex, item.matchId(), item.refereeId());

			candidates.add(new ResolvedAssignmentCandidate(assignmentIndex, item.matchId(), item.refereeId(), match, referee));
		}

		validateBatchInternalConflicts(candidates);
		applyBatchAssignments(candidates);

		List<BatchMatchAssignmentItemResponse> responseItems = candidates.stream()
				.map(candidate -> new BatchMatchAssignmentItemResponse(
						candidate.matchId(),
						candidate.refereeId(),
						"ASSIGNED"))
				.toList();

		return new BatchMatchAssignmentResponse(roundId, "ASSIGNED", responseItems.size(), responseItems);
	}

	/**
	 * Checks overlap conflicts between candidates inside the same batch payload.
	 *
	 * Preconditions: each candidate already passed single-item DB validations.
	 * Postconditions: no two overlapping matches share the same referee inside the batch.
	 * Exceptions: throws {@link MatchAssignmentException} with failing item details.
	 */
	void validateBatchInternalConflicts(List<ResolvedAssignmentCandidate> candidates) {
		for (int i = 0; i < candidates.size(); i++) {
			ResolvedAssignmentCandidate left = candidates.get(i);
			for (int j = i + 1; j < candidates.size(); j++) {
				ResolvedAssignmentCandidate right = candidates.get(j);
				boolean sameReferee = left.referee().getId().equals(right.referee().getId());
				if (sameReferee && overlaps(left.match(), right.match())) {
					throw new MatchAssignmentException(
							MatchAssignmentErrorCode.AVAILABILITY_CONFLICT,
							"Referee is assigned to overlapping matches in the same batch",
							right.index(),
							right.matchId(),
							right.refereeId());
				}
			}
		}
	}

	/**
	 * Persists all validated candidates as a single batch update.
	 *
	 * Preconditions: every candidate is fully validated.
	 * Postconditions: all matches are persisted with assigned referees.
	 * Exceptions: propagated persistence exceptions trigger transaction rollback.
	 */
	void applyBatchAssignments(List<ResolvedAssignmentCandidate> candidates) {
		List<Match> matchesToUpdate = candidates.stream().map(ResolvedAssignmentCandidate::match).toList();
		for (ResolvedAssignmentCandidate candidate : candidates) {
			candidate.match().setReferee(candidate.referee());
		}
		matchRepository.saveAll(matchesToUpdate);
	}

	private boolean overlaps(Match first, Match second) {
		return first.getStartTime().isBefore(second.getEndTime())
				&& first.getEndTime().isAfter(second.getStartTime());
	}

	private Referee resolveReferee(
			Long parsedRefereeId,
			String refereeId,
			Integer index,
			String matchIdForDetails,
			String refereeIdForDetails) {
		Volunteer volunteer = volunteerRepository.findByIdForUpdate(parsedRefereeId)
				.orElseThrow(() -> assignmentException(
						MatchAssignmentErrorCode.REFEREE_NOT_FOUND,
						"Referee not found: " + refereeId,
						index,
						matchIdForDetails,
						refereeIdForDetails));

		if (!(volunteer instanceof Referee referee)) {
			throw assignmentException(
					MatchAssignmentErrorCode.INVALID_ROLE,
					"Volunteer does not have the Referee role",
					index,
					matchIdForDetails,
					refereeIdForDetails);
		}
		return referee;
	}

	private void validateMatchForAssignment(Match match, Integer index, String matchId, String refereeId) {
		if (match.getReferee() != null) {
			throw assignmentException(
					MatchAssignmentErrorCode.MATCH_ALREADY_HAS_REFEREE,
					"Match already has a referee assigned",
					index,
					matchId,
					refereeId);
		}

		if (match.getState() == null || match.getState() == MatchState.FINISHED
				|| match.getStartTime() == null || match.getEndTime() == null) {
			throw assignmentException(
					MatchAssignmentErrorCode.INVALID_MATCH_STATE,
					"Match is not in a valid state for referee assignment",
					index,
					matchId,
					refereeId);
		}
	}

	private void validateAvailability(
			Match match,
			Referee referee,
			Integer index,
			String matchId,
			String refereeId) {
		List<Match> conflicts = matchRepository.findOverlappingAssignments(
				referee, match.getStartTime(), match.getEndTime(), match.getId());
		if (!conflicts.isEmpty()) {
			throw assignmentException(
					MatchAssignmentErrorCode.AVAILABILITY_CONFLICT,
					"Referee is already assigned to another overlapping match",
					index,
					matchId,
					refereeId);
		}
	}

	private Long parseIdOrThrow(String value, Integer index, String matchId, String refereeId) {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException ex) {
			throw assignmentException(
					MatchAssignmentErrorCode.INVALID_ID_FORMAT,
					"Invalid ID format: " + value,
					index,
					matchId,
					refereeId);
		}
	}

	private MatchAssignmentException assignmentException(
			MatchAssignmentErrorCode errorCode,
			String message,
			Integer index,
			String matchId,
			String refereeId) {
		if (index == null) {
			return new MatchAssignmentException(errorCode, message);
		}
		return new MatchAssignmentException(errorCode, message, index, matchId, refereeId);
	}

	record ResolvedAssignmentCandidate(
			Integer index,
			String matchId,
			String refereeId,
			Match match,
			Referee referee) {}
}
