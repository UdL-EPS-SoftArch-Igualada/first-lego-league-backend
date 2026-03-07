package cat.udl.eps.softarch.fll.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
		Long parsedMatchId = parseIdOrThrow(matchId);
		Long parsedRefereeId = parseIdOrThrow(refereeId);

		Match match = matchRepository.findByIdForUpdate(parsedMatchId)
				.orElseThrow(() -> new MatchAssignmentException(
						MatchAssignmentErrorCode.MATCH_NOT_FOUND,
						"Match not found: " + matchId));

		validateMatchForAssignment(match, null);
		Referee referee = resolveReferee(parsedRefereeId, refereeId, null);
		validateAvailability(match, referee, null);

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
		Long parsedRoundId = parseIdOrThrow(roundId);
		Round round = roundRepository.findById(parsedRoundId)
				.orElseThrow(() -> new MatchAssignmentException(
						MatchAssignmentErrorCode.ROUND_NOT_FOUND,
						"Round not found: " + roundId));

		List<ResolvedAssignmentCandidate> candidates = new ArrayList<>();
		Set<Long> seenMatchIds = new HashSet<>();
		List<ParsedAssignment> parsedAssignments = new ArrayList<>();

		for (int i = 0; i < assignments.size(); i++) {
			BatchMatchAssignmentItemRequest item = assignments.get(i);
			if (item == null) {
				AssignmentContext nullItemContext = new AssignmentContext(i, "null", "null");
				throw assignmentException(
						MatchAssignmentErrorCode.INVALID_ID_FORMAT,
						"Assignment item cannot be null",
						nullItemContext);
			}
			AssignmentContext context = new AssignmentContext(i, item.matchId(), item.refereeId());
			Long parsedMatchId = parseIdOrThrow(item.matchId(), context);
			Long parsedRefereeId = parseIdOrThrow(item.refereeId(), context);

			if (!seenMatchIds.add(parsedMatchId)) {
				throw assignmentException(
						MatchAssignmentErrorCode.DUPLICATE_MATCH_IN_BATCH,
						"Match appears multiple times in the same batch",
						context);
			}
			parsedAssignments.add(new ParsedAssignment(context, parsedMatchId, parsedRefereeId));
		}

		parsedAssignments.sort(Comparator
				.comparingLong(ParsedAssignment::parsedMatchId)
				.thenComparingLong(ParsedAssignment::parsedRefereeId)
				.thenComparingInt(parsedAssignment -> parsedAssignment.context().index()));

		for (ParsedAssignment parsedAssignment : parsedAssignments) {
			AssignmentContext context = parsedAssignment.context();
			Match match = matchRepository.findByIdForUpdate(parsedAssignment.parsedMatchId())
					.orElseThrow(() -> assignmentException(
							MatchAssignmentErrorCode.MATCH_NOT_FOUND,
							"Match not found: " + context.matchId(),
							context));

			if (match.getRound() == null || !round.getId().equals(match.getRound().getId())) {
				throw assignmentException(
						MatchAssignmentErrorCode.INVALID_MATCH_STATE,
						"Match does not belong to the provided round",
						context);
			}

			validateMatchForAssignment(match, context);
			Referee referee = resolveReferee(parsedAssignment.parsedRefereeId(), context.refereeId(), context);
			validateAvailability(match, referee, context);

			candidates.add(new ResolvedAssignmentCandidate(context, match, referee));
		}

		validateBatchInternalConflicts(candidates);
		applyBatchAssignments(candidates);

		List<BatchMatchAssignmentItemResponse> responseItems = candidates.stream()
				.sorted(Comparator.comparingInt(candidate -> candidate.context().index()))
				.map(candidate -> new BatchMatchAssignmentItemResponse(
						candidate.context().matchId(),
						candidate.context().refereeId(),
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
		Map<Long, List<ResolvedAssignmentCandidate>> candidatesByReferee = new HashMap<>();
		for (ResolvedAssignmentCandidate candidate : candidates) {
			candidatesByReferee.computeIfAbsent(candidate.referee().getId(), ignored -> new ArrayList<>()).add(candidate);
		}

		for (List<ResolvedAssignmentCandidate> refereeCandidates : candidatesByReferee.values()) {
			AssignmentContext conflictContext = detectIntraBatchConflict(refereeCandidates);
			if (conflictContext != null) {
				throw assignmentException(
						MatchAssignmentErrorCode.AVAILABILITY_CONFLICT,
						"Referee is assigned to overlapping matches in the same batch",
						conflictContext);
			}
		}
	}

	private AssignmentContext detectIntraBatchConflict(List<ResolvedAssignmentCandidate> refereeCandidates) {
		refereeCandidates.sort(Comparator
				.comparing((ResolvedAssignmentCandidate candidate) -> candidate.match().getStartTime())
				.thenComparing(candidate -> candidate.match().getEndTime())
				.thenComparingInt(candidate -> candidate.context().index()));

		for (int i = 1; i < refereeCandidates.size(); i++) {
			ResolvedAssignmentCandidate previous = refereeCandidates.get(i - 1);
			ResolvedAssignmentCandidate current = refereeCandidates.get(i);
			if (overlaps(previous.match(), current.match())) {
				return current.context().index() >= previous.context().index()
						? current.context()
						: previous.context();
			}
		}
		return null;
	}

	/**
	 * Persists all validated candidates as a single batch update.
	 *
	 * Preconditions: every candidate is fully validated.
	 * Postconditions: all matches are persisted with assigned referees.
	 * Exceptions: propagated persistence exceptions trigger transaction rollback.
	 */
	void applyBatchAssignments(List<ResolvedAssignmentCandidate> candidates) {
		List<Match> matchesToUpdate = candidates.stream()
				.map(ResolvedAssignmentCandidate::match)
				.toList();
		for (ResolvedAssignmentCandidate candidate : candidates) {
			candidate.match().setReferee(candidate.referee());
		}
		matchRepository.saveAll(matchesToUpdate);
	}

	private boolean overlaps(Match first, Match second) {
		return first.getStartTime().isBefore(second.getEndTime())
				&& first.getEndTime().isAfter(second.getStartTime());
	}

	private Referee resolveReferee(Long parsedRefereeId, String refereeId, AssignmentContext context) {
		Volunteer volunteer = volunteerRepository.findByIdForUpdate(parsedRefereeId)
				.orElseThrow(() -> assignmentException(
						MatchAssignmentErrorCode.REFEREE_NOT_FOUND,
						"Referee not found: " + refereeId,
						context));

		if (!(volunteer instanceof Referee referee)) {
			throw assignmentException(
					MatchAssignmentErrorCode.INVALID_ROLE,
					"Volunteer does not have the Referee role",
					context);
		}
		return referee;
	}

	private void validateMatchForAssignment(Match match, AssignmentContext context) {
		if (match.getReferee() != null) {
			throw assignmentException(
					MatchAssignmentErrorCode.MATCH_ALREADY_HAS_REFEREE,
					"Match already has a referee assigned",
					context);
		}

		if (match.getState() == null || match.getState() == MatchState.FINISHED
				|| match.getStartTime() == null || match.getEndTime() == null) {
			throw assignmentException(
					MatchAssignmentErrorCode.INVALID_MATCH_STATE,
					"Match is not in a valid state for referee assignment",
					context);
		}
	}

	private void validateAvailability(Match match, Referee referee, AssignmentContext context) {
		List<Match> conflicts = matchRepository.findOverlappingAssignments(
				referee, match.getStartTime(), match.getEndTime(), match.getId());
		if (!conflicts.isEmpty()) {
			throw assignmentException(
					MatchAssignmentErrorCode.AVAILABILITY_CONFLICT,
					"Referee is already assigned to another overlapping match",
					context);
		}
	}

	private Long parseIdOrThrow(String value) {
		return parseIdOrThrow(value, null);
	}

	private Long parseIdOrThrow(String value, AssignmentContext context) {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException | NullPointerException ex) {
			throw assignmentException(
					MatchAssignmentErrorCode.INVALID_ID_FORMAT,
					"Invalid ID format: " + value,
					context);
		}
	}

	private MatchAssignmentException assignmentException(MatchAssignmentErrorCode errorCode, String message, AssignmentContext context) {
		if (context == null) {
			return new MatchAssignmentException(errorCode, message);
		}
		return new MatchAssignmentException(
				errorCode,
				message,
				context.index(),
				context.matchId(),
				context.refereeId());
	}

	record AssignmentContext(Integer index, String matchId, String refereeId) {}

	record ParsedAssignment(AssignmentContext context, Long parsedMatchId, Long parsedRefereeId) {}

	record ResolvedAssignmentCandidate(
			AssignmentContext context,
			Match match,
			Referee referee) {}
}
