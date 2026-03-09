package cat.udl.eps.softarch.fll.service;

import cat.udl.eps.softarch.fll.domain.CompetitionTable;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.exception.MatchScheduleErrorCode;
import cat.udl.eps.softarch.fll.exception.MatchScheduleException;
import cat.udl.eps.softarch.fll.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class MatchScheduleValidationServiceTest {

	@Mock
	private MatchRepository matchRepository;

	@InjectMocks
	private MatchScheduleValidationService validationService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void validTimeRangeAndNoOverlap_doesNotThrowException() {
		Match match = createMatch("10:00", "11:00");
		when(matchRepository.findOverlappingMatchesByTable(any(), any(), any(), any()))
				.thenReturn(Collections.emptyList());

		assertDoesNotThrow(() -> validationService.validateForCreateOrUpdate(match));
	}

	@Test
	void startTimeEqualsEndTime_throwsInvalidTimeRange() {
		Match match = createMatch("10:00", "10:00");

		MatchScheduleException ex = assertThrows(MatchScheduleException.class, 
				() -> validationService.validateForCreateOrUpdate(match));
		assertEquals(MatchScheduleErrorCode.INVALID_TIME_RANGE, ex.getErrorCode());
	}

	@Test
	void startTimeAfterEndTime_throwsInvalidTimeRange() {
		Match match = createMatch("11:00", "10:00");

		MatchScheduleException ex = assertThrows(MatchScheduleException.class, 
				() -> validationService.validateForCreateOrUpdate(match));
		assertEquals(MatchScheduleErrorCode.INVALID_TIME_RANGE, ex.getErrorCode());
	}

	@Test
	void nullTimes_throwsInvalidTimeRange() {
		Match match = new Match(); 
		
		MatchScheduleException ex = assertThrows(MatchScheduleException.class, 
				() -> validationService.validateForCreateOrUpdate(match));
		assertEquals(MatchScheduleErrorCode.INVALID_TIME_RANGE, ex.getErrorCode());
	}

	@Test
	void tableTimeOverlap_throwsTableTimeOverlap() {
		Match match = createMatch("10:30", "11:30");
		CompetitionTable table = new CompetitionTable();
		table.setId("Table-1");
		match.setCompetitionTable(table);

		Match existingMatch = createMatch("10:00", "11:00");

		when(matchRepository.findOverlappingMatchesByTable(any(), any(), any(), any()))
				.thenReturn(List.of(existingMatch));

		MatchScheduleException ex = assertThrows(MatchScheduleException.class, 
				() -> validationService.validateForCreateOrUpdate(match));
		assertEquals(MatchScheduleErrorCode.TABLE_TIME_OVERLAP, ex.getErrorCode());
	}

	private Match createMatch(String start, String end) {
		Match match = new Match();
		match.setStartTime(LocalTime.parse(start));
		match.setEndTime(LocalTime.parse(end));
		return match;
	}
}