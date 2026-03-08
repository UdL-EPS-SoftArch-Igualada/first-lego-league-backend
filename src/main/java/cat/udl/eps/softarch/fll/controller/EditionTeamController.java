package cat.udl.eps.softarch.fll.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import cat.udl.eps.softarch.fll.service.EditionTeamRegistrationService;

@RestController
public class EditionTeamController {

	private final EditionTeamRegistrationService registrationService;

	public EditionTeamController(EditionTeamRegistrationService registrationService) {
		this.registrationService = registrationService;
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/editions/{editionId}/teams/{teamId}")
	public ResponseEntity<Map<String, String>> registerTeam(
			@PathVariable Long editionId, @PathVariable String teamId) {
		registrationService.registerTeam(editionId, teamId);
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
				"editionId", editionId.toString(),
				"teamId", teamId,
				"status", "REGISTERED"));
	}
}

