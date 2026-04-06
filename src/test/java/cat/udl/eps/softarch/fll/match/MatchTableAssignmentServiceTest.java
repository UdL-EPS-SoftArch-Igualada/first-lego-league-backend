package cat.udl.eps.softarch.fll.match;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalTime;
import java.util.Optional;

import cat.udl.eps.softarch.fll.service.MatchTableAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import cat.udl.eps.softarch.fll.controller.match.dto.MatchTableAssignmentResponse;
import cat.udl.eps.softarch.fll.domain.CompetitionTable;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.repository.match.CompetitionTableRepository;
import cat.udl.eps.softarch.fll.repository.match.MatchRepository;

@ExtendWith(MockitoExtension.class)
class MatchTableAssignmentServiceTest {

	@Mock
	private MatchRepository matchRepository;
	@Mock
	private CompetitionTableRepository competitionTableRepository;

	private MatchTableAssignmentService service;

	@BeforeEach
	void setUp() {
		service = new MatchTableAssignmentService(matchRepository, competitionTableRepository);
	}

	@Test
	void assignTableSuccess() {
		Match match = buildMatch(10L, "Table-0", "11:00", "11:20");
		CompetitionTable table = buildTable("Table-1");
		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(match));
		when(competitionTableRepository.findByIdForUpdate("Table-1")).thenReturn(Optional.of(table));
		when(matchRepository.existsOverlappingAssignmentsForTable(table, match.getStartTime(), match.getEndTime(), 10L))
			.thenReturn(false);
		when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

		MatchTableAssignmentResponse response = service.assignTable(10L, "Table-1");

		assertEquals(10L, response.matchId());
		assertEquals("Table-1", response.tableIdentifier());
		assertEquals("11:00", response.startTime());
		assertEquals("11:20", response.endTime());
		assertSame(table, match.getCompetitionTable());
		verify(matchRepository).save(match);
	}

	@Test
	void assignTableFailsWhenMatchNotFound() {
		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.assignTable(10L, "Table-1"));

		assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
	}

	@Test
	void assignTableFailsWhenTableNotFound() {
		Match match = buildMatch(10L, null, "11:00", "11:20");
		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(match));
		when(competitionTableRepository.findByIdForUpdate("Table-1")).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.assignTable(10L, "Table-1"));

		assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
	}

	@Test
	void assignTableFailsWhenScheduleOverlaps() {
		Match match = buildMatch(10L, null, "11:00", "11:20");
		CompetitionTable table = buildTable("Table-1");
		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(match));
		when(competitionTableRepository.findByIdForUpdate("Table-1")).thenReturn(Optional.of(table));
		when(matchRepository.existsOverlappingAssignmentsForTable(table, match.getStartTime(), match.getEndTime(), 10L))
			.thenReturn(true);

		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.assignTable(10L, "Table-1"));

		assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
	}

	@Test
	void assignTableAllowsReassignmentToAnotherTable() {
		Match match = buildMatch(10L, "Table-0", "11:00", "11:20");
		CompetitionTable newTable = buildTable("Table-2");
		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(match));
		when(competitionTableRepository.findByIdForUpdate("Table-2")).thenReturn(Optional.of(newTable));
		when(matchRepository.existsOverlappingAssignmentsForTable(newTable, match.getStartTime(), match.getEndTime(), 10L))
			.thenReturn(false);
		when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

		MatchTableAssignmentResponse response = service.assignTable(10L, "Table-2");

		assertEquals("Table-2", response.tableIdentifier());
		assertSame(newTable, match.getCompetitionTable());
	}

	@Test
	void assignTableFailsWhenMatchMissingStartTime() {
		Match match = new Match();
		match.setId(10L);
		match.setEndTime(LocalTime.parse("11:20"));
		CompetitionTable table = buildTable("Table-1");
		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(match));
		when(competitionTableRepository.findByIdForUpdate("Table-1")).thenReturn(Optional.of(table));

		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.assignTable(10L, "Table-1"));

		assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, ex.getStatusCode());
		assertEquals("Match is missing start time", ex.getReason());
	}

	@Test
	void assignTableFailsWhenMatchMissingEndTime() {
		Match match = new Match();
		match.setId(10L);
		match.setStartTime(LocalTime.parse("11:00"));
		CompetitionTable table = buildTable("Table-1");
		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(match));
		when(competitionTableRepository.findByIdForUpdate("Table-1")).thenReturn(Optional.of(table));

		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.assignTable(10L, "Table-1"));

		assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, ex.getStatusCode());
		assertEquals("Match is missing end time", ex.getReason());
	}

	@Test
	void assignTableFailsWhenMatchEndTimeIsNotAfterStartTime() {
		Match match = buildMatch(10L, null, "11:20", "11:00");
		CompetitionTable table = buildTable("Table-1");
		when(matchRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(match));
		when(competitionTableRepository.findByIdForUpdate("Table-1")).thenReturn(Optional.of(table));

		ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.assignTable(10L, "Table-1"));

		assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, ex.getStatusCode());
		assertEquals("Match end time must be after start time", ex.getReason());
	}

	private Match buildMatch(Long id, String tableId, String startTime, String endTime) {
		Match match = new Match();
		match.setId(id);
		match.setStartTime(LocalTime.parse(startTime));
		match.setEndTime(LocalTime.parse(endTime));
		if (tableId != null) {
			match.setCompetitionTable(buildTable(tableId));
		}
		return match;
	}

	private CompetitionTable buildTable(String id) {
		CompetitionTable table = new CompetitionTable();
		table.setId(id);
		return table;
	}
}
