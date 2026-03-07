package cat.udl.eps.softarch.fll.service;

import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.MatchResult;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.MatchRepository;
import cat.udl.eps.softarch.fll.repository.MatchResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchScoreRegistrationServiceTest {

	@Mock
	private MatchRepository matchRepository;

	@Mock
	private MatchResultRepository matchResultRepository;

	@Mock
	private RankingService rankingService;

	@InjectMocks
	private MatchScoreRegistrationService matchScoreRegistrationService;

	@Captor
	private ArgumentCaptor<List<MatchResult>> matchResultCaptor;

	private Match match;
	private Long matchId;
	private String teamAId;
	private String teamBId;
	private Integer teamAScore;
	private Integer teamBScore;

	@BeforeEach
	void setUp() {
		Team teamA = Team.create("Team-A", "City-A", 2000, "Junior");
		Team teamB = Team.create("Team-B", "City-A", 2000, "Junior");

		match = new Match();
		match.setId(1L);
		match.setStartTime(LocalTime.of(10, 0));
		match.setEndTime(LocalTime.of(11, 0));
		match.setTeamA(teamA);
		match.setTeamB(teamB);

		matchId = 1L;
		teamAId = "Team-A";
		teamBId = "Team-B";
		teamAScore = 120;
		teamBScore = 95;
	}

	@Test
	void registerMatchScoreShouldPersistTwoResultsAndRecalculateRanking() {
		when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
		when(matchResultRepository.existsByMatch(match)).thenReturn(false);

		matchScoreRegistrationService.registerMatchScore(matchId, teamAId, teamBId, teamAScore, teamBScore);

		verify(matchResultRepository).saveAllAndFlush(matchResultCaptor.capture());
		verify(rankingService).recalculateRanking();

		List<MatchResult> savedResults = matchResultCaptor.getValue();
		assertEquals(2, savedResults.size());
		assertEquals("Team-A", savedResults.get(0).getTeam().getId());
		assertEquals(120, savedResults.get(0).getScore());
		assertEquals("Team-B", savedResults.get(1).getTeam().getId());
		assertEquals(95, savedResults.get(1).getScore());
	}

	@Test
	void registerMatchScoreShouldThrowMatchNotFoundWhenMatchDoesNotExist() {
		when(matchRepository.findById(1L)).thenReturn(Optional.empty());

		MatchScoreRegistrationService.RegistrationException ex = assertThrows(
			MatchScoreRegistrationService.RegistrationException.class,
			() -> matchScoreRegistrationService.registerMatchScore(matchId, teamAId, teamBId, teamAScore, teamBScore));

		assertEquals(MatchScoreRegistrationService.ErrorCode.MATCH_NOT_FOUND, ex.getErrorCode());
		verify(matchResultRepository, never()).saveAllAndFlush(any());
		verify(rankingService, never()).recalculateRanking();
	}

	@Test
	void registerMatchScoreShouldThrowInvalidMatchStateWhenMatchHasNoStartTime() {
		match.setStartTime(null);
		when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

		MatchScoreRegistrationService.RegistrationException ex = assertThrows(
			MatchScoreRegistrationService.RegistrationException.class,
			() -> matchScoreRegistrationService.registerMatchScore(matchId, teamAId, teamBId, teamAScore, teamBScore));

		assertEquals(MatchScoreRegistrationService.ErrorCode.INVALID_MATCH_STATE, ex.getErrorCode());
		verify(matchResultRepository, never()).saveAllAndFlush(any());
		verify(rankingService, never()).recalculateRanking();
	}

	@Test
	void registerMatchScoreShouldThrowMatchNotFinishedWhenMatchHasNoEndTime() {
		match.setEndTime(null);
		when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

		MatchScoreRegistrationService.RegistrationException ex = assertThrows(
			MatchScoreRegistrationService.RegistrationException.class,
			() -> matchScoreRegistrationService.registerMatchScore(matchId, teamAId, teamBId, teamAScore, teamBScore));

		assertEquals(MatchScoreRegistrationService.ErrorCode.MATCH_NOT_FINISHED, ex.getErrorCode());
		verify(matchResultRepository, never()).saveAllAndFlush(any());
		verify(rankingService, never()).recalculateRanking();
	}

	@Test
	void registerMatchScoreShouldThrowResultAlreadyExistsWhenMatchAlreadyHasResults() {
		when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
		when(matchResultRepository.existsByMatch(match)).thenReturn(true);

		MatchScoreRegistrationService.RegistrationException ex = assertThrows(
			MatchScoreRegistrationService.RegistrationException.class,
			() -> matchScoreRegistrationService.registerMatchScore(matchId, teamAId, teamBId, teamAScore, teamBScore));

		assertEquals(MatchScoreRegistrationService.ErrorCode.RESULT_ALREADY_EXISTS, ex.getErrorCode());
		verify(matchResultRepository, never()).saveAllAndFlush(any());
		verify(rankingService, never()).recalculateRanking();
	}

	@Test
	void registerMatchScoreShouldThrowResultAlreadyExistsWhenConcurrentInsertViolatesUniqueConstraint() {
		when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
		when(matchResultRepository.existsByMatch(match)).thenReturn(false);
		when(matchResultRepository.saveAllAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

		MatchScoreRegistrationService.RegistrationException ex = assertThrows(
			MatchScoreRegistrationService.RegistrationException.class,
			() -> matchScoreRegistrationService.registerMatchScore(matchId, teamAId, teamBId, teamAScore, teamBScore));

		assertEquals(MatchScoreRegistrationService.ErrorCode.RESULT_ALREADY_EXISTS, ex.getErrorCode());
		verify(rankingService, never()).recalculateRanking();
	}

	@Test
	void registerMatchScoreShouldThrowInvalidScoreWhenScoreIsNegative() {
		teamAScore = -1;
		when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
		when(matchResultRepository.existsByMatch(match)).thenReturn(false);

		MatchScoreRegistrationService.RegistrationException ex = assertThrows(
			MatchScoreRegistrationService.RegistrationException.class,
			() -> matchScoreRegistrationService.registerMatchScore(matchId, teamAId, teamBId, teamAScore, teamBScore));

		assertEquals(MatchScoreRegistrationService.ErrorCode.INVALID_SCORE, ex.getErrorCode());
		verify(matchResultRepository, never()).saveAllAndFlush(any());
		verify(rankingService, never()).recalculateRanking();
	}

	@Test
	void registerMatchScoreShouldThrowInvalidScoreWhenScoreIsNull() {
		teamBScore = null;

		MatchScoreRegistrationService.RegistrationException ex = assertThrows(
			MatchScoreRegistrationService.RegistrationException.class,
			() -> matchScoreRegistrationService.registerMatchScore(matchId, teamAId, teamBId, teamAScore, teamBScore));

		assertEquals(MatchScoreRegistrationService.ErrorCode.INVALID_SCORE, ex.getErrorCode());
		verify(matchResultRepository, never()).saveAllAndFlush(any());
		verify(rankingService, never()).recalculateRanking();
	}

	@Test
	void registerMatchScoreShouldThrowTeamMismatchWhenTeamsDoNotMatchAssignedTeams() {
		teamAId = "Another-Team";
		when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
		when(matchResultRepository.existsByMatch(match)).thenReturn(false);

		MatchScoreRegistrationService.RegistrationException ex = assertThrows(
			MatchScoreRegistrationService.RegistrationException.class,
			() -> matchScoreRegistrationService.registerMatchScore(matchId, teamAId, teamBId, teamAScore, teamBScore));

		assertEquals(MatchScoreRegistrationService.ErrorCode.TEAM_MISMATCH, ex.getErrorCode());
		verify(matchResultRepository, never()).saveAllAndFlush(any());
		verify(rankingService, never()).recalculateRanking();
	}

	@Test
	void registerMatchScoreShouldSupportSwappedTeamOrderInInputPayload() {
		teamAId = "Team-B";
		teamBId = "Team-A";
		teamAScore = 40;
		teamBScore = 80;
		when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
		when(matchResultRepository.existsByMatch(match)).thenReturn(false);

		matchScoreRegistrationService.registerMatchScore(matchId, teamAId, teamBId, teamAScore, teamBScore);

		verify(matchResultRepository).saveAllAndFlush(matchResultCaptor.capture());
		List<MatchResult> savedResults = matchResultCaptor.getValue();

		assertEquals(80, savedResults.get(0).getScore());
		assertEquals(40, savedResults.get(1).getScore());
		assertNotEquals(savedResults.get(0).getTeam().getId(), savedResults.get(1).getTeam().getId());
	}
}
