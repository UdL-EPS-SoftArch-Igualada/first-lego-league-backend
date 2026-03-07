package cat.udl.eps.softarch.fll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import cat.udl.eps.softarch.fll.controller.dto.EditionVolunteersResponse;
import cat.udl.eps.softarch.fll.domain.Floater;
import cat.udl.eps.softarch.fll.domain.Judge;
import cat.udl.eps.softarch.fll.domain.Referee;
import cat.udl.eps.softarch.fll.repository.EditionRepository;
import cat.udl.eps.softarch.fll.repository.FloaterRepository;
import cat.udl.eps.softarch.fll.repository.JudgeRepository;
import cat.udl.eps.softarch.fll.repository.RefereeRepository;

@ExtendWith(MockitoExtension.class)
class EditionVolunteerServiceTest {

	@Mock
	private EditionRepository editionRepository;

	@Mock
	private RefereeRepository refereeRepository;

	@Mock
	private JudgeRepository judgeRepository;

	@Mock
	private FloaterRepository floaterRepository;

	@InjectMocks
	private EditionVolunteerService editionVolunteerService;

	@Test
	void getVolunteersGroupedByTypeReturnsGroupedAndMappedResponse() {
		Referee referee = new Referee();
		referee.setId(1L);
		referee.setName("Ref One");
		referee.setEmailAddress("ref1@example.com");
		referee.setPhoneNumber("111111111");

		Judge judge = new Judge();
		judge.setId(2L);
		judge.setName("Judge One");
		judge.setEmailAddress("judge1@example.com");
		judge.setPhoneNumber("222222222");

		Floater floater = new Floater();
		floater.setId(3L);
		floater.setName("Floater One");
		floater.setEmailAddress("floater1@example.com");
		floater.setPhoneNumber("333333333");
		floater.setStudentCode("STU-1");

		when(editionRepository.existsById(10L)).thenReturn(true);
		when(refereeRepository.findByEditionId(10L)).thenReturn(List.of(referee));
		when(judgeRepository.findByEditionId(10L)).thenReturn(List.of(judge));
		when(floaterRepository.findByEditionId(10L)).thenReturn(List.of(floater));

		EditionVolunteersResponse response = editionVolunteerService.getVolunteersGroupedByType(10L);

		assertEquals(1, response.referees().size());
		assertEquals(1L, response.referees().get(0).id());
		assertEquals("Ref One", response.referees().get(0).name());
		assertEquals("ref1@example.com", response.referees().get(0).emailAddress());
		assertEquals("111111111", response.referees().get(0).phoneNumber());

		assertEquals(1, response.judges().size());
		assertEquals(2L, response.judges().get(0).id());
		assertEquals("Judge One", response.judges().get(0).name());
		assertEquals("judge1@example.com", response.judges().get(0).emailAddress());
		assertEquals("222222222", response.judges().get(0).phoneNumber());

		assertEquals(1, response.floaters().size());
		assertEquals(3L, response.floaters().get(0).id());
		assertEquals("Floater One", response.floaters().get(0).name());
		assertEquals("floater1@example.com", response.floaters().get(0).emailAddress());
		assertEquals("333333333", response.floaters().get(0).phoneNumber());
		verify(refereeRepository).findByEditionId(10L);
		verify(judgeRepository).findByEditionId(10L);
		verify(floaterRepository).findByEditionId(10L);
	}

	@Test
	void getVolunteersGroupedByTypeReturnsEmptyListsWhenNoVolunteersExist() {
		when(editionRepository.existsById(10L)).thenReturn(true);
		when(refereeRepository.findByEditionId(10L)).thenReturn(List.of());
		when(judgeRepository.findByEditionId(10L)).thenReturn(List.of());
		when(floaterRepository.findByEditionId(10L)).thenReturn(List.of());

		EditionVolunteersResponse response = editionVolunteerService.getVolunteersGroupedByType(10L);

		assertEquals(0, response.referees().size());
		assertEquals(0, response.judges().size());
		assertEquals(0, response.floaters().size());
	}

	@Test
	void getVolunteersGroupedByTypeThrowsWhenEditionDoesNotExist() {
		when(editionRepository.existsById(99L)).thenReturn(false);

		NoSuchElementException exception = assertThrows(
				NoSuchElementException.class,
				() -> editionVolunteerService.getVolunteersGroupedByType(99L));

		assertEquals("EDITION_NOT_FOUND", exception.getMessage());
		verifyNoInteractions(refereeRepository, judgeRepository, floaterRepository);
	}
}
