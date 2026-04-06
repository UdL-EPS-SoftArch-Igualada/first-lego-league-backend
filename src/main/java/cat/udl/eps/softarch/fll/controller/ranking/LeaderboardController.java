package cat.udl.eps.softarch.fll.controller.ranking;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cat.udl.eps.softarch.fll.controller.ranking.dto.LeaderboardPageResponse;
import cat.udl.eps.softarch.fll.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/leaderboards")
@Tag(name = "Leaderboards", description = "Endpoints for retrieving competition leaderboards")
public class LeaderboardController {

	private final LeaderboardService leaderboardService;

	public LeaderboardController(LeaderboardService leaderboardService) {
		this.leaderboardService = leaderboardService;
	}

	@GetMapping("/editions/{editionId}")
	@Operation(summary = "Get leaderboard for a specific edition",
		description = "Sorting is fixed to totalScore DESC, matchesPlayed DESC, teamName ASC.")
	public LeaderboardPageResponse getEditionLeaderboard(
		@Parameter(description = "Edition identifier") @PathVariable Long editionId,
		@Parameter(description = "Page index (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
		@Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
		return leaderboardService.getEditionLeaderboard(editionId, page, size);
	}
}
