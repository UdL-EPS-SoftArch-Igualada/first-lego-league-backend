package cat.udl.eps.softarch.fll.controller.edition;

import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cat.udl.eps.softarch.fll.controller.edition.dto.EditionVolunteersResponse;
import cat.udl.eps.softarch.fll.service.EditionVolunteerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/editions")
@RequiredArgsConstructor
public class EditionVolunteerController {

	private static final String ERROR_KEY = "error";
	private final EditionVolunteerService editionVolunteerService;

	@GetMapping("/{editionId}/volunteers")
	@PreAuthorize("isAuthenticated()")
	public EditionVolunteersResponse getVolunteersGroupedByType(@PathVariable Long editionId) {
		return editionVolunteerService.getVolunteersGroupedByType(editionId);
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(Map.of(ERROR_KEY, exception.getMessage()));
	}
}
