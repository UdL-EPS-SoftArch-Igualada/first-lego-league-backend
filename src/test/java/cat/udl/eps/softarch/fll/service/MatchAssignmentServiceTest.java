package cat.udl.eps.softarch.fll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import cat.udl.eps.softarch.fll.controller.dto.BatchMatchAssignmentItemRequest;
import cat.udl.eps.softarch.fll.controller.dto.BatchMatchAssignmentResponse;
import cat.udl.eps.softarch.fll.domain.Floater;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.MatchState;
import cat.udl.eps.softarch.fll.domain.Referee;
import cat.udl.eps.softarch.fll.domain.Round;
import cat.udl.eps.softarch.fll.exception.MatchAssignmentErrorCode;
import cat.udl.eps.softarch.fll.exception.MatchAssignmentException;
import cat.udl.eps.softarch.fll.repository.MatchRepository;
import cat.udl.eps.softarch.fll.repository.RoundRepository;
import cat.udl.eps.softarch.fll.repository.VolunteerRepository;

@ExtendWith(MockitoExtension.class)
class MatchAssignmentServiceTest {

	@Mock
	private MatchRepository matchRepository;
	@Mock
	private VolunteerRepository volunteerRepository;
	@Mock
	private RoundRepository roundRepository;

	private MatchAssignmentService service;

	@BeforeEach
	void setUp() {
		service = new MatchAssignmentService(matchRepository, roundRepository, volunteerRepository);
	}

	@Test
	void assignRefereeSuccess() {
		Match match = buildMatch(10L, MatchState.SCHEDULED, null);
		Referee referee = buildReferee(20L);

		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(match));
		when(volunteerRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(referee));
		when(matchRepository.findOverlappingAssignments(
				referee, match.getStartTime(), match.getEndTime(), match.getId())).thenReturn(List.of());
		when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Match result = service.assignReferee("10", "20");

		assertSame(referee, result.getReferee());
		verify(matchRepository).save(match);
	}

	@Test
	void assignRefereeFailsWhenMatchNotFound() {
		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.empty());

		MatchAssignmentException ex = assertThrows(
				MatchAssignmentException.class, () -> service.assignReferee("10", "20"));

		assertEquals(MatchAssignmentErrorCode.MATCH_NOT_FOUND, ex.getErrorCode());
	}

	@Test
	void assignRefereeFailsWhenRefereeNotFound() {
		Match match = buildMatch(10L, MatchState.SCHEDULED, null);
		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(match));
		when(volunteerRepository.findByIdForUpdate(20L)).thenReturn(Optional.empty());

		MatchAssignmentException ex = assertThrows(
				MatchAssignmentException.class, () -> service.assignReferee("10", "20"));

		assertEquals(MatchAssignmentErrorCode.REFEREE_NOT_FOUND, ex.getErrorCode());
	}

	@Test
	void assignRefereeFailsWhenVolunteerIsNotReferee() {
		Match match = buildMatch(10L, MatchState.SCHEDULED, null);
		Floater floater = new Floater();
		floater.setId(20L);

		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(match));
		when(volunteerRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(floater));

		MatchAssignmentException ex = assertThrows(
				MatchAssignmentException.class, () -> service.assignReferee("10", "20"));

		assertEquals(MatchAssignmentErrorCode.INVALID_ROLE, ex.getErrorCode());
	}

	@Test
	void assignRefereeFailsWhenAvailabilityConflict() {
		Match match = buildMatch(10L, MatchState.SCHEDULED, null);
		Referee referee = buildReferee(20L);
		Match overlapping = buildMatch(11L, MatchState.SCHEDULED, referee);

		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(match));
		when(volunteerRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(referee));
		when(matchRepository.findOverlappingAssignments(
				referee, match.getStartTime(), match.getEndTime(), match.getId())).thenReturn(List.of(overlapping));

		MatchAssignmentException ex = assertThrows(
				MatchAssignmentException.class, () -> service.assignReferee("10", "20"));

		assertEquals(MatchAssignmentErrorCode.AVAILABILITY_CONFLICT, ex.getErrorCode());
	}

	@Test
	void assignRefereeFailsWhenMatchAlreadyHasReferee() {
		Referee assigned = buildReferee(99L);
		Match match = buildMatch(10L, MatchState.SCHEDULED, assigned);

		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(match));

		MatchAssignmentException ex = assertThrows(
				MatchAssignmentException.class, () -> service.assignReferee("10", "20"));

		assertEquals(MatchAssignmentErrorCode.MATCH_ALREADY_HAS_REFEREE, ex.getErrorCode());
	}

	@Test
	void assignRefereeFailsWhenMatchStateIsInvalid() {
		Match match = buildMatch(10L, MatchState.FINISHED, null);
		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(match));

		MatchAssignmentException ex = assertThrows(
				MatchAssignmentException.class, () -> service.assignReferee("10", "20"));

		assertEquals(MatchAssignmentErrorCode.INVALID_MATCH_STATE, ex.getErrorCode());
	}

	@Test
	void assignRefereeFailsWhenMatchIdIsInvalidFormat() {
		MatchAssignmentException ex = assertThrows(
				MatchAssignmentException.class, () -> service.assignReferee("abc", "20"));

		assertEquals(MatchAssignmentErrorCode.INVALID_ID_FORMAT, ex.getErrorCode());
	}

	@Test
	void assignBatchSuccess() {
		Round round = buildRound(3L);
		Match matchA = buildMatch(10L, MatchState.SCHEDULED, null);
		matchA.setRound(round);
		matchA.setStartTime(LocalTime.of(10, 0));
		matchA.setEndTime(LocalTime.of(11, 0));
		Match matchB = buildMatch(11L, MatchState.SCHEDULED, null);
		matchB.setRound(round);
		matchB.setStartTime(LocalTime.of(11, 0));
		matchB.setEndTime(LocalTime.of(12, 0));
		Referee refereeA = buildReferee(20L);
		Referee refereeB = buildReferee(21L);

		when(roundRepository.findById(3L)).thenReturn(Optional.of(round));
		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(matchA));
		when(matchRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(matchB));
		when(volunteerRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(refereeA));
		when(volunteerRepository.findByIdForUpdate(21L)).thenReturn(Optional.of(refereeB));
		when(matchRepository.findOverlappingAssignments(refereeA, matchA.getStartTime(), matchA.getEndTime(), 10L))
				.thenReturn(List.of());
		when(matchRepository.findOverlappingAssignments(refereeB, matchB.getStartTime(), matchB.getEndTime(), 11L))
				.thenReturn(List.of());

		BatchMatchAssignmentResponse response = service.assignBatch("3", List.of(
				new BatchMatchAssignmentItemRequest("10", "20"),
				new BatchMatchAssignmentItemRequest("11", "21")));

		assertEquals("3", response.roundId());
		assertEquals("ASSIGNED", response.status());
		assertEquals(2, response.processed());
		assertEquals(2, response.assignments().size());
		assertSame(refereeA, matchA.getReferee());
		assertSame(refereeB, matchB.getReferee());
		verify(matchRepository).saveAll(List.of(matchA, matchB));
	}

	@Test
	void assignBatchFailsWhenPayloadHasIntraBatchConflictAndPersistsNone() {
		Round round = buildRound(3L);
		Match matchA = buildMatch(10L, MatchState.SCHEDULED, null);
		matchA.setRound(round);
		matchA.setStartTime(LocalTime.of(10, 0));
		matchA.setEndTime(LocalTime.of(11, 0));
		Match matchB = buildMatch(11L, MatchState.SCHEDULED, null);
		matchB.setRound(round);
		matchB.setStartTime(LocalTime.of(10, 30));
		matchB.setEndTime(LocalTime.of(11, 30));
		Referee referee = buildReferee(20L);

		when(roundRepository.findById(3L)).thenReturn(Optional.of(round));
		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(matchA));
		when(matchRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(matchB));
		when(volunteerRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(referee));
		when(matchRepository.findOverlappingAssignments(referee, matchA.getStartTime(), matchA.getEndTime(), 10L))
				.thenReturn(List.of());
		when(matchRepository.findOverlappingAssignments(referee, matchB.getStartTime(), matchB.getEndTime(), 11L))
				.thenReturn(List.of());
		List<BatchMatchAssignmentItemRequest> assignments = List.of(
				new BatchMatchAssignmentItemRequest("10", "20"),
				new BatchMatchAssignmentItemRequest("11", "20"));

		MatchAssignmentException ex = assertThrows(
				MatchAssignmentException.class,
				() -> service.assignBatch("3", assignments));

		assertEquals(MatchAssignmentErrorCode.AVAILABILITY_CONFLICT, ex.getErrorCode());
		assertEquals(1, ex.getIndex());
		verify(matchRepository, never()).saveAll(any());
	}

	@Test
	void assignBatchFailsWhenOneVolunteerIsNotRefereeAndPersistsNone() {
		Round round = buildRound(3L);
		Match matchA = buildMatch(10L, MatchState.SCHEDULED, null);
		matchA.setRound(round);
		Match matchB = buildMatch(11L, MatchState.SCHEDULED, null);
		matchB.setRound(round);
		Referee referee = buildReferee(20L);
		Floater floater = new Floater();
		floater.setId(21L);

		when(roundRepository.findById(3L)).thenReturn(Optional.of(round));
		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(matchA));
		when(matchRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(matchB));
		when(volunteerRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(referee));
		when(volunteerRepository.findByIdForUpdate(21L)).thenReturn(Optional.of(floater));
		when(matchRepository.findOverlappingAssignments(referee, matchA.getStartTime(), matchA.getEndTime(), 10L))
				.thenReturn(List.of());
		List<BatchMatchAssignmentItemRequest> assignments = List.of(
				new BatchMatchAssignmentItemRequest("10", "20"),
				new BatchMatchAssignmentItemRequest("11", "21"));

		MatchAssignmentException ex = assertThrows(
				MatchAssignmentException.class,
				() -> service.assignBatch("3", assignments));

		assertEquals(MatchAssignmentErrorCode.INVALID_ROLE, ex.getErrorCode());
		assertEquals(1, ex.getIndex());
		verify(matchRepository, never()).saveAll(any());
	}

	@Test
	void assignBatchFailsWhenItemIdFormatIsInvalid() {
		when(roundRepository.findById(3L)).thenReturn(Optional.of(buildRound(3L)));
		List<BatchMatchAssignmentItemRequest> assignments = List.of(
				new BatchMatchAssignmentItemRequest("not-a-number", "20"));

		MatchAssignmentException ex = assertThrows(
				MatchAssignmentException.class,
				() -> service.assignBatch("3", assignments));

		assertEquals(MatchAssignmentErrorCode.INVALID_ID_FORMAT, ex.getErrorCode());
		assertEquals(0, ex.getIndex());
		verify(matchRepository, never()).saveAll(any());
	}

	@Test
	void assignBatchFailsWhenMatchIdAppearsTwiceInPayload() {
		when(roundRepository.findById(3L)).thenReturn(Optional.of(buildRound(3L)));
		List<BatchMatchAssignmentItemRequest> assignments = List.of(
				new BatchMatchAssignmentItemRequest("10", "20"),
				new BatchMatchAssignmentItemRequest("10", "21"));

		MatchAssignmentException ex = assertThrows(
				MatchAssignmentException.class,
				() -> service.assignBatch("3", assignments));

		assertEquals(MatchAssignmentErrorCode.DUPLICATE_MATCH_IN_BATCH, ex.getErrorCode());
		assertEquals(1, ex.getIndex());
		verify(matchRepository, never()).saveAll(any());
	}

	@Test
	void assignBatchFailsWhenRoundDoesNotExist() {
		when(roundRepository.findById(3L)).thenReturn(Optional.empty());
		List<BatchMatchAssignmentItemRequest> assignments = List.of(
				new BatchMatchAssignmentItemRequest("10", "20"));

		MatchAssignmentException ex = assertThrows(
				MatchAssignmentException.class,
				() -> service.assignBatch("3", assignments));

		assertEquals(MatchAssignmentErrorCode.ROUND_NOT_FOUND, ex.getErrorCode());
		verify(matchRepository, never()).saveAll(any());
	}

	private Match buildMatch(Long id, MatchState state, Referee referee) {
		Match match = new Match();
		match.setId(id);
		match.setStartTime(LocalTime.of(10, 0));
		match.setEndTime(LocalTime.of(11, 0));
		match.setState(state);
		match.setReferee(referee);
		return match;
	}

	private Referee buildReferee(Long id) {
		Referee referee = new Referee();
		referee.setId(id);
		return referee;
	}

	private Round buildRound(Long id) {
		Round round = new Round();
		round.setId(id);
		round.setNumber(id.intValue());
		return round;
	}
}
