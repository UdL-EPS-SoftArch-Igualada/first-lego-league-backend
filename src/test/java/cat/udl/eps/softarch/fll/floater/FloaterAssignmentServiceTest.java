package cat.udl.eps.softarch.fll.floater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cat.udl.eps.softarch.fll.domain.team.Team;
import cat.udl.eps.softarch.fll.domain.edition.Edition;
import cat.udl.eps.softarch.fll.domain.edition.Venue;
import cat.udl.eps.softarch.fll.domain.volunteer.Floater;
import cat.udl.eps.softarch.fll.dto.AssignFloaterResponse;
import cat.udl.eps.softarch.fll.exception.TeamFloaterAssignmentException;
import cat.udl.eps.softarch.fll.repository.team.TeamRepository;
import cat.udl.eps.softarch.fll.service.floater.FloaterAssignmentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class FloaterAssignmentServiceTest {

	@Mock
	private TeamRepository teamRepository;

	@Mock
	private EntityManager entityManager;

	@InjectMocks
	private FloaterAssignmentService floaterAssignmentService;

	private Team team;
	private Floater floater;

	@BeforeEach
	void setUp() {
		Edition edition = Edition.create(2026, Venue.create("Igualada Arena", "Igualada"), "FLL Season");
		edition.setId(11L);

		team = Team.create("TeamA", "Igualada", 2000, "Junior");
		team.setEdition(edition);

		floater = Floater.create("Floater One", "floater@example.com", "123456789", "ST-42");
		floater.setId(42L);

		lenient().when(teamRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		ReflectionTestUtils.setField(floaterAssignmentService, "entityManager", entityManager);
	}

	@Test
	void assignFloaterShouldSucceedWhenAllValidationsPass() {
		when(entityManager.find(Team.class, "TeamA", LockModeType.PESSIMISTIC_WRITE)).thenReturn(team);
		when(entityManager.find(Floater.class, 42L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(floater);

		AssignFloaterResponse response = floaterAssignmentService.assignFloater("TeamA", 42L);

		assertEquals("TeamA", response.getTeamId());
		assertEquals(42L, response.getFloaterId());
		assertEquals(1, team.getFloaters().size());
		assertEquals(team, floater.getAssistedTeams().iterator().next());
		verify(teamRepository).save(team);
	}

	@Test
	void assignFloaterShouldThrowTeamNotFoundWhenTeamIsMissing() {
		when(entityManager.find(Team.class, "TeamA", LockModeType.PESSIMISTIC_WRITE)).thenReturn(null);

		TeamFloaterAssignmentException ex = assertThrows(
			TeamFloaterAssignmentException.class,
			() -> floaterAssignmentService.assignFloater("TeamA", 42L));

		assertEquals("TEAM_NOT_FOUND", ex.getErrorCode());
	}

	@Test
	void assignFloaterShouldThrowFloaterNotFoundWhenFloaterIsMissing() {
		when(entityManager.find(Team.class, "TeamA", LockModeType.PESSIMISTIC_WRITE)).thenReturn(team);
		when(entityManager.find(Floater.class, 42L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(null);

		TeamFloaterAssignmentException ex = assertThrows(
			TeamFloaterAssignmentException.class,
			() -> floaterAssignmentService.assignFloater("TeamA", 42L));

		assertEquals("FLOATER_NOT_FOUND", ex.getErrorCode());
	}

	@Test
	void assignFloaterShouldThrowFloaterAlreadyAssignedWhenFloaterIsAlreadyInTeam() {
		team.addFloater(floater);
		when(entityManager.find(Team.class, "TeamA", LockModeType.PESSIMISTIC_WRITE)).thenReturn(team);
		when(entityManager.find(Floater.class, 42L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(floater);

		TeamFloaterAssignmentException ex = assertThrows(
			TeamFloaterAssignmentException.class,
			() -> floaterAssignmentService.assignFloater("TeamA", 42L));

		assertEquals("FLOATER_ALREADY_ASSIGNED", ex.getErrorCode());
	}

	@Test
	void assignFloaterShouldThrowMaxFloatersReachedWhenTeamAlreadyHasTwoFloaters() {
		Floater floaterTwo = Floater.create("Floater Two", "floater2@example.com", "987654321", "ST-43");
		floaterTwo.setId(43L);
		Floater floaterThree = Floater.create("Floater Three", "floater3@example.com", "555555555", "ST-44");
		floaterThree.setId(44L);

		team.addFloater(floater);
		team.addFloater(floaterTwo);

		when(entityManager.find(Team.class, "TeamA", LockModeType.PESSIMISTIC_WRITE)).thenReturn(team);
		when(entityManager.find(Floater.class, 44L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(floaterThree);

		TeamFloaterAssignmentException ex = assertThrows(
			TeamFloaterAssignmentException.class,
			() -> floaterAssignmentService.assignFloater("TeamA", 44L));

		assertEquals("MAX_FLOATERS_REACHED", ex.getErrorCode());
	}
}
