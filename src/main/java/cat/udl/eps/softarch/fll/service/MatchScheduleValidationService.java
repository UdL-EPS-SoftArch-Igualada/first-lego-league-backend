package cat.udl.eps.softarch.fll.service;

import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.exception.MatchScheduleErrorCode;
import cat.udl.eps.softarch.fll.exception.MatchScheduleException;
import cat.udl.eps.softarch.fll.repository.MatchRepository;


@Service
public class MatchScheduleValidationService {

	private final MatchRepository matchRepository;

	public MatchScheduleValidationService(MatchRepository matchRepository) {
		this.matchRepository = matchRepository;
	}


	@Transactional(readOnly = true)
	public void validateForCreateOrUpdate(Match match) {
		validateTimeRange(match.getStartTime(), match.getEndTime());

		if (match.getCompetitionTable() != null && match.getCompetitionTable().getId() != null) {
			validateNoTableOverlap(
					match.getCompetitionTable().getId(),
					match.getStartTime(),
					match.getEndTime(),
					match.getId()
			);
		}
	}

	public void validateTimeRange(LocalTime startTime, LocalTime endTime) {
		if (startTime == null || endTime == null) {
			throw new MatchScheduleException(
					MatchScheduleErrorCode.INVALID_TIME_RANGE,
					"Match start and end times cannot be null"
			);
		}

		if (!startTime.isBefore(endTime)) {
			throw new MatchScheduleException(
					MatchScheduleErrorCode.INVALID_TIME_RANGE,
					"Match start time must be strictly before end time"
			);
		}
	}

	public void validateNoTableOverlap(String tableId, LocalTime newStartTime, LocalTime newEndTime, Long currentMatchId) {
		List<Match> overlaps = matchRepository.findOverlappingMatchesByTable(
				tableId,
				newStartTime,
				newEndTime,
				currentMatchId
		);

		if (!overlaps.isEmpty()) {
			throw new MatchScheduleException(
					MatchScheduleErrorCode.TABLE_TIME_OVERLAP,
					"The match schedule overlaps with an existing match on the same table"
			);
		}
	}
}