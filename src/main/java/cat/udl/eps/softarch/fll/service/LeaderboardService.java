package cat.udl.eps.softarch.fll.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import cat.udl.eps.softarch.fll.controller.ranking.dto.LeaderboardItemResponse;
import cat.udl.eps.softarch.fll.controller.ranking.dto.LeaderboardPageResponse;
import cat.udl.eps.softarch.fll.repository.edition.EditionRepository;
import cat.udl.eps.softarch.fll.repository.match.MatchResultRepository;
import cat.udl.eps.softarch.fll.repository.ranking.projection.LeaderboardRowProjection;

@Service
public class LeaderboardService {

	private final EditionRepository editionRepository;
	private final MatchResultRepository matchResultRepository;

	public LeaderboardService(EditionRepository editionRepository, MatchResultRepository matchResultRepository) {
		this.editionRepository = editionRepository;
		this.matchResultRepository = matchResultRepository;
	}

	public LeaderboardPageResponse getEditionLeaderboard(Long editionId, int page, int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				"Invalid pagination: page must be >= 0 and size must be between 1 and 100");
		}
		long basePosition = (long) page * size;
		if (basePosition + size > Integer.MAX_VALUE) {
			throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				"Invalid pagination: page and size are too large");
		}

		if (!editionRepository.existsById(editionId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Edition not found: " + editionId);
		}

		Page<LeaderboardRowProjection> resultPage = matchResultRepository.findLeaderboardByEditionId(
			editionId, PageRequest.of(page, size));

		List<LeaderboardItemResponse> items = mapItems(resultPage, basePosition);

		return new LeaderboardPageResponse(
			editionId,
			page,
			size,
			resultPage.getTotalElements(),
			items);
	}

	private List<LeaderboardItemResponse> mapItems(Page<LeaderboardRowProjection> resultPage, long basePosition) {
		List<LeaderboardRowProjection> rows = resultPage.getContent();

		return java.util.stream.IntStream.range(0, rows.size())
			.mapToObj(index -> {
				LeaderboardRowProjection row = rows.get(index);
				int position = Math.toIntExact(basePosition + index + 1L);
				return new LeaderboardItemResponse(
					position,
					row.getTeamId(),
					row.getTeamName(),
					row.getTotalScore(),
					row.getMatchesPlayed());
			})
			.toList();
	}
}
