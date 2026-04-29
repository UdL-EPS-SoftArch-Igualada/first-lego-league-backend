package cat.udl.eps.softarch.fll.controller.edition;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import cat.udl.eps.softarch.fll.dto.MatchSearchItemResponse;
import cat.udl.eps.softarch.fll.service.match.MatchSearchService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EditionMatchController {

	private final MatchSearchService matchSearchService;

	@GetMapping("/editions/{id}/matches")
	public ResponseEntity<Object> getMatchesByEdition(@PathVariable Long id) {
		try {
			List<MatchSearchItemResponse> matches = matchSearchService.findByEditionId(id);
			var embedded = Map.of("matches", matches);
			return ResponseEntity.ok(Map.of("_embedded", embedded));
		} catch (IllegalArgumentException ex) {
			if (!"EDITION_NOT_FOUND".equals(ex.getMessage())) {
				throw ex;
			}
			return ResponseEntity.status(404)
				.body(Map.of(
					"error", ex.getMessage(),
					"message", "The referenced edition does not exist"
				));
		}
	}
}
