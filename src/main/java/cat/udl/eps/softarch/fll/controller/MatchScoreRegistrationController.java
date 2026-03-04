package cat.udl.eps.softarch.fll.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cat.udl.eps.softarch.fll.service.MatchScoreRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/matchResults")
@Tag(name = "Match Result Registration", description = "Custom endpoint to register final match scores")
public class MatchScoreRegistrationController {

	private final MatchScoreRegistrationService matchScoreRegistrationService;

	public MatchScoreRegistrationController(MatchScoreRegistrationService matchScoreRegistrationService) {
		this.matchScoreRegistrationService = matchScoreRegistrationService;
	}

	@PostMapping("/register")
	@Operation(summary = "Register final score for a match and recalculate ranking")
	public RegisterMatchScoreResponse registerFinalScore(@RequestBody RegisterMatchScoreRequest request) {
		if (request == null || request.score() == null) {
			throw new MatchScoreRegistrationService.RegistrationException(
					MatchScoreRegistrationService.ErrorCode.INVALID_SCORE,
					"Invalid score payload");
		}

		Long matchId = request.matchId();
		MatchScorePayload score = request.score();
		matchScoreRegistrationService.registerMatchScore(
				matchId,
				score.teamAId(),
				score.teamBId(),
				score.teamAScore(),
				score.teamBScore());
		return new RegisterMatchScoreResponse(matchId, true, true);
	}

	@ExceptionHandler(MatchScoreRegistrationService.RegistrationException.class)
	public ResponseEntity<MatchScoreErrorResponse> handleRegistrationError(MatchScoreRegistrationService.RegistrationException ex) {
		return ResponseEntity.status(ex.getStatus()).body(new MatchScoreErrorResponse(ex.getErrorCode().name(), ex.getMessage()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<MatchScoreErrorResponse> handleInvalidPayload(Exception ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new MatchScoreErrorResponse(MatchScoreRegistrationService.ErrorCode.INVALID_SCORE.name(), "Invalid score payload"));
	}

	public record RegisterMatchScoreRequest(Long matchId, MatchScorePayload score) {
	}

	public record MatchScorePayload(String teamAId, String teamBId, Integer teamAScore, Integer teamBScore) {
	}

	public record RegisterMatchScoreResponse(Long matchId, boolean resultSaved, boolean rankingUpdated) {
	}

	public record MatchScoreErrorResponse(String error, String message) {
	}
}
