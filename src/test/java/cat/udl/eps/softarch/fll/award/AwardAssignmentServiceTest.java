package cat.udl.eps.softarch.fll.award;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import cat.udl.eps.softarch.fll.domain.Award;
import cat.udl.eps.softarch.fll.domain.Edition;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.dto.AssignAwardResponse;
import cat.udl.eps.softarch.fll.exception.AwardAssignmentException;
import cat.udl.eps.softarch.fll.repository.ranking.AwardRepository;
import cat.udl.eps.softarch.fll.repository.team.TeamRepository;
import cat.udl.eps.softarch.fll.service.award.AwardAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AwardAssignmentServiceTest {

	@Mock
	private AwardRepository awardRepository;

	@Mock
	private TeamRepository teamRepository;

	@InjectMocks
	private AwardAssignmentService awardAssignmentService;

	private Edition edition;
	private Team team;
	private Award award;

	@BeforeEach
	void setUp() {
		edition = Edition.create(2026, "Igualada Arena", "FLL Season");
		edition.setId(11L);

		team = Team.create("TeamA", "Igualada", 2000, "Junior");
		team.setEdition(edition);

		award = Award.create("Best Robot", edition);
		award.setId(21L);

		when(awardRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void assignAwardShouldSucceedWhenAllValidationsPass() {
		when(awardRepository.findByIdForUpdate(21L)).thenReturn(Optional.of(award));
		when(teamRepository.findByNameForUpdate("TeamA")).thenReturn(Optional.of(team));

		AssignAwardResponse response = awardAssignmentService.assignAward("21", "TeamA");

		assertEquals("21", response.awardId());
		assertEquals("TeamA", response.teamId());
		assertEquals("AWARDED", response.status());
		assertEquals(team, award.getWinner());
		verify(awardRepository).saveAndFlush(award);
	}

	@Test
	void assignAwardShouldThrowAwardNotFoundWhenAwardIsMissing() {
		when(awardRepository.findByIdForUpdate(21L)).thenReturn(Optional.empty());

		AwardAssignmentException ex = assertThrows(
			AwardAssignmentException.class,
			() -> awardAssignmentService.assignAward("21", "TeamA"));

		assertEquals("AWARD_NOT_FOUND", ex.getErrorCode());
	}

	@Test
	void assignAwardShouldThrowTeamNotFoundWhenTeamIsMissing() {
		when(awardRepository.findByIdForUpdate(21L)).thenReturn(Optional.of(award));
		when(teamRepository.findByNameForUpdate("TeamA")).thenReturn(Optional.empty());

		AwardAssignmentException ex = assertThrows(
			AwardAssignmentException.class,
			() -> awardAssignmentService.assignAward("21", "TeamA"));

		assertEquals("TEAM_NOT_FOUND", ex.getErrorCode());
	}

	@Test
	void assignAwardShouldThrowAwardAlreadyAssignedWhenWinnerExists() {
		award.setWinner(team);
		when(awardRepository.findByIdForUpdate(21L)).thenReturn(Optional.of(award));
		when(teamRepository.findByNameForUpdate("TeamA")).thenReturn(Optional.of(team));

		AwardAssignmentException ex = assertThrows(
			AwardAssignmentException.class,
			() -> awardAssignmentService.assignAward("21", "TeamA"));

		assertEquals("AWARD_ALREADY_ASSIGNED", ex.getErrorCode());
	}

	@Test
	void assignAwardShouldThrowEditionMismatchWhenTeamIsInDifferentEdition() {
		Edition otherEdition = Edition.create(2025, "Other Arena", "FLL Season");
		otherEdition.setId(12L);
		team.setEdition(otherEdition);

		when(awardRepository.findByIdForUpdate(21L)).thenReturn(Optional.of(award));
		when(teamRepository.findByNameForUpdate("TeamA")).thenReturn(Optional.of(team));

		AwardAssignmentException ex = assertThrows(
			AwardAssignmentException.class,
			() -> awardAssignmentService.assignAward("21", "TeamA"));

		assertEquals("EDITION_MISMATCH", ex.getErrorCode());
	}

	@Test
	void assignAwardShouldAllowSameTeamToReceiveMultipleDifferentAwards() {
		Award secondAward = Award.create("Best Design", edition);
		secondAward.setId(22L);

		when(awardRepository.findByIdForUpdate(21L)).thenReturn(Optional.of(award));
		when(awardRepository.findByIdForUpdate(22L)).thenReturn(Optional.of(secondAward));
		when(teamRepository.findByNameForUpdate("TeamA")).thenReturn(Optional.of(team));

		AssignAwardResponse firstResponse = awardAssignmentService.assignAward("21", "TeamA");
		AssignAwardResponse secondResponse = awardAssignmentService.assignAward("22", "TeamA");

		assertEquals("AWARDED", firstResponse.status());
		assertEquals("AWARDED", secondResponse.status());
		assertEquals(team, award.getWinner());
		assertEquals(team, secondAward.getWinner());
		verify(awardRepository, times(2)).saveAndFlush(any(Award.class));
	}
}
