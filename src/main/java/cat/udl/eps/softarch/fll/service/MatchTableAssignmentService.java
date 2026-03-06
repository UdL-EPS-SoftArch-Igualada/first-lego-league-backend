package cat.udl.eps.softarch.fll.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import cat.udl.eps.softarch.fll.controller.dto.MatchTableAssignmentResponse;
import cat.udl.eps.softarch.fll.domain.CompetitionTable;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.repository.CompetitionTableRepository;
import cat.udl.eps.softarch.fll.repository.MatchRepository;

@Service
public class MatchTableAssignmentService {

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

	private final MatchRepository matchRepository;
	private final CompetitionTableRepository competitionTableRepository;

	public MatchTableAssignmentService(
			MatchRepository matchRepository,
			CompetitionTableRepository competitionTableRepository) {
		this.matchRepository = matchRepository;
		this.competitionTableRepository = competitionTableRepository;
	}

	@Transactional
	public MatchTableAssignmentResponse assignTable(Long matchId, String tableIdentifier) {
		Match match = matchRepository.findByIdForUpdate(matchId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found: " + matchId));
		CompetitionTable table = competitionTableRepository.findByIdForUpdate(tableIdentifier)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found: " + tableIdentifier));

		validateMatchSchedule(match);
		validateTableAvailability(match, table);

		match.setCompetitionTable(table);
		Match savedMatch = matchRepository.save(match);

		return new MatchTableAssignmentResponse(
				savedMatch.getId(),
				savedMatch.getCompetitionTable().getId(),
				savedMatch.getStartTime().format(TIME_FORMATTER),
				savedMatch.getEndTime().format(TIME_FORMATTER));
	}

	private void validateMatchSchedule(Match match) {
		if (match.getStartTime() == null) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Match is missing start time");
		}
		if (match.getEndTime() == null) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Match is missing end time");
		}
		if (!match.getEndTime().isAfter(match.getStartTime())) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Match end time must be after start time");
		}
	}

	private void validateTableAvailability(Match match, CompetitionTable table) {
		List<Match> conflicts = matchRepository.findOverlappingAssignmentsForTable(
				table, match.getStartTime(), match.getEndTime(), match.getId());
		if (!conflicts.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Table has overlapping scheduled match");
		}
	}
}
