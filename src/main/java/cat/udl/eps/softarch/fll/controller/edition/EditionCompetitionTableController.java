package cat.udl.eps.softarch.fll.controller.edition;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cat.udl.eps.softarch.fll.controller.edition.dto.EditionCompetitionTableResponse;
import cat.udl.eps.softarch.fll.exception.EditionCompetitionTableNotFoundException;
import cat.udl.eps.softarch.fll.service.EditionCompetitionTableService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/editions")
@RequiredArgsConstructor
public class EditionCompetitionTableController {

	private static final String ERROR_KEY = "error";
	private final EditionCompetitionTableService editionCompetitionTableService;

	@GetMapping("/{editionId}/tables")
	public List<EditionCompetitionTableResponse> getCompetitionTables(@PathVariable Long editionId) {
		return editionCompetitionTableService.getTablesByEdition(editionId);
	}

	@ExceptionHandler(EditionCompetitionTableNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(Map.of(ERROR_KEY, "EDITION_NOT_FOUND"));
	}
}
