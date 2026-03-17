package cat.udl.eps.softarch.fll.edition;

import cat.udl.eps.softarch.fll.domain.Edition;
import cat.udl.eps.softarch.fll.domain.EditionOperation;
import cat.udl.eps.softarch.fll.domain.EditionState;
import cat.udl.eps.softarch.fll.exception.EditionLifecycleException;
import cat.udl.eps.softarch.fll.repository.EditionRepository;
import cat.udl.eps.softarch.fll.service.EditionLifecycleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EditionLifecycleServiceTest {

	@Mock
	private EditionRepository editionRepository;

	@InjectMocks
	private EditionLifecycleService editionLifecycleService;

	private Edition edition;

	@BeforeEach
	void setUp() {
		edition = Edition.create(2026, "Test Edition", "description");
		edition.setId(1L);
		edition.setState(EditionState.DRAFT);
	}

	@Test
	void changeStateShouldAllowDraftToOpen() {
		when(editionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(edition));

		EditionLifecycleService.TransitionResult result = editionLifecycleService.changeState(1L, EditionState.OPEN);

		assertEquals(EditionState.DRAFT, result.previousState());
		assertEquals(EditionState.OPEN, result.newState());
		assertEquals(EditionState.OPEN, edition.getState());
		verify(editionRepository).save(edition);
	}

	@Test
	void changeStateShouldAllowOpenToClosed() {
		edition.setState(EditionState.OPEN);
		when(editionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(edition));

		EditionLifecycleService.TransitionResult result = editionLifecycleService.changeState(1L, EditionState.CLOSED);

		assertEquals(EditionState.OPEN, result.previousState());
		assertEquals(EditionState.CLOSED, result.newState());
		assertEquals(EditionState.CLOSED, edition.getState());
		verify(editionRepository).save(edition);
	}

	@Test
	void changeStateShouldRejectOpenToDraft() {
		edition.setState(EditionState.OPEN);
		when(editionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(edition));

		EditionLifecycleException ex = assertThrows(
			EditionLifecycleException.class,
			() -> editionLifecycleService.changeState(1L, EditionState.DRAFT));

		assertEquals("INVALID_EDITION_STATE_TRANSITION", ex.getError());
	}

	@Test
	void changeStateShouldRejectTransitionsFromClosed() {
		edition.setState(EditionState.CLOSED);
		when(editionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(edition));

		EditionLifecycleException ex = assertThrows(
			EditionLifecycleException.class,
			() -> editionLifecycleService.changeState(1L, EditionState.OPEN));

		assertEquals("INVALID_EDITION_STATE_TRANSITION", ex.getError());
	}

	@Test
	void changeStateShouldRejectClosedToDraft() {
		edition.setState(EditionState.CLOSED);
		when(editionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(edition));

		EditionLifecycleException ex = assertThrows(
			EditionLifecycleException.class,
			() -> editionLifecycleService.changeState(1L, EditionState.DRAFT));

		assertEquals("INVALID_EDITION_STATE_TRANSITION", ex.getError());
	}

	@Test
	void changeStateShouldRejectSameStateTransition() {
		when(editionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(edition));

		EditionLifecycleException ex = assertThrows(
			EditionLifecycleException.class,
			() -> editionLifecycleService.changeState(1L, EditionState.DRAFT));

		assertEquals("INVALID_EDITION_STATE_TRANSITION", ex.getError());
	}

	@Test
	void changeStateShouldThrowNotFoundWhenEditionMissing() {
		when(editionRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

		EditionLifecycleException ex = assertThrows(
			EditionLifecycleException.class,
			() -> editionLifecycleService.changeState(1L, EditionState.OPEN));

		assertEquals("EDITION_NOT_FOUND", ex.getError());
	}

	@Test
	void changeStateShouldTreatNullStateAsDraft() {
		edition.setState(null);
		when(editionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(edition));

		EditionLifecycleService.TransitionResult result = editionLifecycleService.changeState(1L, EditionState.OPEN);

		assertEquals(EditionState.DRAFT, result.previousState());
		assertEquals(EditionState.OPEN, result.newState());
		assertEquals(EditionState.OPEN, edition.getState());
		verify(editionRepository).save(edition);
	}

	@Test
	void assertOperationAllowedShouldAllowTeamRegistrationWhenOpen() {
		edition.setState(EditionState.OPEN);

		assertDoesNotThrow(() -> editionLifecycleService.assertOperationAllowed(edition, EditionOperation.TEAM_REGISTRATION));
	}

	@Test
	void assertOperationAllowedShouldRejectTeamRegistrationWhenDraft() {
		EditionLifecycleException ex = assertThrows(
			EditionLifecycleException.class,
			() -> editionLifecycleService.assertOperationAllowed(edition, EditionOperation.TEAM_REGISTRATION));

		assertEquals("EDITION_OPERATION_NOT_ALLOWED", ex.getError());
	}

	@Test
	void assertOperationAllowedShouldRejectTeamRegistrationWhenClosed() {
		edition.setState(EditionState.CLOSED);

		EditionLifecycleException ex = assertThrows(
			EditionLifecycleException.class,
			() -> editionLifecycleService.assertOperationAllowed(edition, EditionOperation.TEAM_REGISTRATION));

		assertEquals("EDITION_OPERATION_NOT_ALLOWED", ex.getError());
	}

	@Test
	void assertOperationAllowedShouldTreatNullStateAsDraft() {
		edition.setState(null);

		EditionLifecycleException ex = assertThrows(
			EditionLifecycleException.class,
			() -> editionLifecycleService.assertOperationAllowed(edition, EditionOperation.TEAM_REGISTRATION));

		assertEquals("EDITION_OPERATION_NOT_ALLOWED", ex.getError());
		assertEquals("Operation TEAM_REGISTRATION is not allowed when edition is in state DRAFT", ex.getMessage());
	}
}
