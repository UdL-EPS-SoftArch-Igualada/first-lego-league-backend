package cat.udl.eps.softarch.fll.controller.match;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cat.udl.eps.softarch.fll.controller.dto.ApiErrorResponse;
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
				MatchScoreRegistrationService.ErrorCode.INVALID_SCORE_PAYLOAD,
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
	public ResponseEntity<ApiErrorResponse> handleRegistrationError(
		MatchScoreRegistrationService.RegistrationException ex,
		HttpServletRequest request) {
		return ResponseEntity.status(ex.getStatus())
			.body(ApiErrorResponse.of(ex.getErrorCode().name(), ex.getMessage(), request.getRequestURI()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidPayload(HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ApiErrorResponse.of(
				"INVALID_SCORE_PAYLOAD",
				"Invalid score payload",
				request.getRequestURI()));
	}

	public record RegisterMatchScoreRequest(Long matchId, MatchScorePayload score) {
	}

	public record MatchScorePayload(String teamAId, String teamBId, Integer teamAScore, Integer teamBScore) {
	}

	public record RegisterMatchScoreResponse(Long matchId, boolean resultSaved, boolean rankingUpdated) {
	}
}
