package cat.udl.eps.softarch.fll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import cat.udl.eps.softarch.fll.controller.dto.LeaderboardPageResponse;
import cat.udl.eps.softarch.fll.repository.EditionRepository;
import cat.udl.eps.softarch.fll.repository.MatchResultRepository;
import cat.udl.eps.softarch.fll.repository.projection.LeaderboardRowProjection;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

	@Mock
	private EditionRepository editionRepository;

	@Mock
	private MatchResultRepository matchResultRepository;

	@InjectMocks
	private LeaderboardService leaderboardService;

	@Test
	void getEditionLeaderboardShouldThrowNotFoundWhenEditionDoesNotExist() {
		when(editionRepository.existsById(999L)).thenReturn(false);

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> leaderboardService.getEditionLeaderboard(999L, 0, 10));

		assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
		verify(matchResultRepository, never()).findLeaderboardByEditionId(any(), any());
	}

	@Test
	void getEditionLeaderboardShouldThrowBadRequestWhenPaginationIsInvalid() {
		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> leaderboardService.getEditionLeaderboard(2025L, -1, 0));

		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		verify(editionRepository, never()).existsById(any());
		verify(matchResultRepository, never()).findLeaderboardByEditionId(any(), any());
	}

	@Test
	void getEditionLeaderboardShouldThrowBadRequestWhenPaginationOverflowsPositionCalculation() {
		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> leaderboardService.getEditionLeaderboard(2025L, Integer.MAX_VALUE, 2));

		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		verify(editionRepository, never()).existsById(any());
		verify(matchResultRepository, never()).findLeaderboardByEditionId(any(), any());
	}

	@Test
	void getEditionLeaderboardShouldThrowBadRequestWhenPaginationOverflowsWithSizeOne() {
		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> leaderboardService.getEditionLeaderboard(2025L, Integer.MAX_VALUE, 1));

		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		verify(editionRepository, never()).existsById(any());
		verify(matchResultRepository, never()).findLeaderboardByEditionId(any(), any());
	}

	@Test
	void getEditionLeaderboardShouldReturnEmptyPageWhenEditionHasNoResults() {
		when(editionRepository.existsById(2025L)).thenReturn(true);
		Page<LeaderboardRowProjection> emptyPage = Page.empty(PageRequest.of(0, 10));
		when(matchResultRepository.findLeaderboardByEditionId(2025L, PageRequest.of(0, 10))).thenReturn(emptyPage);

		LeaderboardPageResponse response = leaderboardService.getEditionLeaderboard(2025L, 0, 10);

		assertEquals(2025L, response.editionId());
		assertEquals(0, response.page());
		assertEquals(10, response.size());
		assertEquals(0, response.totalElements());
		assertEquals(0, response.items().size());
	}

	@Test
	void getEditionLeaderboardShouldMapRowsAndCalculateGlobalPositions() {
		when(editionRepository.existsById(2025L)).thenReturn(true);
		List<LeaderboardRowProjection> rows = List.of(
				row("TeamA", "TeamA", 560L, 3L),
				row("TeamB", "TeamB", 500L, 3L));
		Page<LeaderboardRowProjection> page = new PageImpl<>(rows, PageRequest.of(1, 2), 6);
		when(matchResultRepository.findLeaderboardByEditionId(2025L, PageRequest.of(1, 2))).thenReturn(page);

		LeaderboardPageResponse response = leaderboardService.getEditionLeaderboard(2025L, 1, 2);

		assertEquals(6, response.totalElements());
		assertEquals(2, response.items().size());
		assertEquals(3, response.items().get(0).position());
		assertEquals(4, response.items().get(1).position());
		assertEquals("TeamA", response.items().get(0).teamName());
		assertEquals("TeamB", response.items().get(1).teamName());
	}

	private LeaderboardRowProjection row(String teamId, String teamName, Long totalScore, Long matchesPlayed) {
		LeaderboardRowProjection projection = mock(LeaderboardRowProjection.class);
		when(projection.getTeamId()).thenReturn(teamId);
		when(projection.getTeamName()).thenReturn(teamName);
		when(projection.getTotalScore()).thenReturn(totalScore);
		when(projection.getMatchesPlayed()).thenReturn(matchesPlayed);
		return projection;
	}
}
