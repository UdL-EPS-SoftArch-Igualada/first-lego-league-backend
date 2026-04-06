package cat.udl.eps.softarch.fll.controller.match;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cat.udl.eps.softarch.fll.dto.MatchSearchItemResponse;
import cat.udl.eps.softarch.fll.dto.MatchSearchPageResponse;
import cat.udl.eps.softarch.fll.service.MatchSearchService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchSearchController {

	private final MatchSearchService matchSearchService;

	@GetMapping("/filter")
	public ResponseEntity<Object> searchMatches(
		@RequestParam(name = "startFrom", required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startFrom,

		@RequestParam(name = "endTo", required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTo,

		@RequestParam(name = "tableId", required = false) String tableId,
		@RequestParam(name = "roundId", required = false) Long roundId,
		Pageable pageable
	) {
		try {
			Page<MatchSearchItemResponse> page =
				matchSearchService.searchMatches(startFrom, endTo, tableId, roundId, pageable);

			MatchSearchPageResponse response = new MatchSearchPageResponse();
			response.setPage(page.getNumber());
			response.setSize(page.getSize());
			response.setTotalElements(page.getTotalElements());
			response.setItems(page.getContent());

			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException ex) {
			return ResponseEntity.unprocessableContent()
				.body(Map.of(
					"errorCode", ex.getMessage(),
					"message", "Start time must not be after end time",
					"timestamp", java.time.Instant.now().toString(),
					"path", "/matches/filter"
				));
		}
	}

	@GetMapping("/search/findByTeam")
	public ResponseEntity<Object> findByTeam(
		@RequestParam(name = "team") String teamUri
	) {
		try {
			List<MatchSearchItemResponse> matches = matchSearchService.findByTeam(teamUri);
			var embedded = Map.of("matches", matches);
			return ResponseEntity.ok(Map.of("_embedded", embedded));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(404)
				.body(Map.of(
					"error", ex.getMessage(),
					"message", "The referenced team does not exist"
				));
		}
	}
}
