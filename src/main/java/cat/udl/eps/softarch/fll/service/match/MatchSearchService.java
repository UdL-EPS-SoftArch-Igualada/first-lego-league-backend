package cat.udl.eps.softarch.fll.service.match;

import java.time.LocalTime;
import java.util.List;
import cat.udl.eps.softarch.fll.dto.MatchSearchItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.match.MatchRepository;
import cat.udl.eps.softarch.fll.repository.match.specification.MatchSpecifications;
import cat.udl.eps.softarch.fll.repository.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class MatchSearchService {

	private final MatchRepository matchRepository;
	private final TeamRepository teamRepository;

	public Page<MatchSearchItemResponse> searchMatches(
		LocalTime startFrom,
		LocalTime endTo,
		String tableId,
		Long roundId,
		Pageable pageable) {

		validateTimeRange(startFrom, endTo);

		if (pageable.getSort().isUnsorted()) {
			pageable = PageRequest.of(
				pageable.getPageNumber(),
				pageable.getPageSize(),
				Sort.by("startTime").ascending()
					.and(Sort.by("id").ascending())
			);
		}

		Specification<Match> spec =
			Specification.where(MatchSpecifications.timeOverlap(startFrom, endTo))
				.and(MatchSpecifications.hasTable(tableId))
				.and(MatchSpecifications.hasRound(roundId));

		Page<Match> page = matchRepository.findAll(spec, pageable);

		return page.map(this::toDto);
	}

	private void validateTimeRange(LocalTime startFrom, LocalTime endTo) {
		if (startFrom != null && endTo != null && startFrom.isAfter(endTo)) {
			throw new IllegalArgumentException("INVALID_TIME_FILTER_RANGE");
		}
	}

	public List<MatchSearchItemResponse> findByTeam(String teamId) {
		Long id;
		try {
			if (teamId.contains("/")) {
				String idStr = teamId.substring(teamId.lastIndexOf('/') + 1);
				id = Long.parseLong(idStr);
			} else {
				id = Long.parseLong(teamId);
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid team ID format: " + teamId);
		}

		Team team = teamRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("TEAM_NOT_FOUND"));

		return matchRepository.findByTeam(team).stream()
			.map(this::toDto)
			.toList();
	}

	private MatchSearchItemResponse toDto(Match match) {
		if (match.getCompetitionTable() == null) {
			throw new IllegalStateException("Match " + match.getId() + " has no competition table");
		}
		if (match.getRound() == null) {
			throw new IllegalStateException("Match " + match.getId() + " has no round");
		}

		MatchSearchItemResponse dto = new MatchSearchItemResponse();
		dto.setMatchId(match.getId().toString());
		dto.setStartTime(match.getStartTime());
		dto.setEndTime(match.getEndTime());
		dto.setTableId(match.getCompetitionTable().getId());
		dto.setRoundId(match.getRound().getId());
		return dto;
	}
}