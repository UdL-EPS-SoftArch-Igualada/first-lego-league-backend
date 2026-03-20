package cat.udl.eps.softarch.fll.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import cat.udl.eps.softarch.fll.domain.Round;
import cat.udl.eps.softarch.fll.domain.Score;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.ScoreRepository;
import cat.udl.eps.softarch.fll.repository.match.MatchResultRepository;
import cat.udl.eps.softarch.fll.repository.match.RoundRepository;
import cat.udl.eps.softarch.fll.repository.team.TeamRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@RestController
@RequestMapping("/rounds")
public class RoundScoreController {

	private final RoundRepository roundRepository;
	private final TeamRepository teamRepository;
	private final MatchResultRepository matchResultRepository;
	private final ScoreRepository scoreRepository;

	public RoundScoreController(
			RoundRepository roundRepository,
			TeamRepository teamRepository,
			MatchResultRepository matchResultRepository,
			ScoreRepository scoreRepository
	) {
		this.roundRepository = roundRepository;
		this.teamRepository = teamRepository;
		this.matchResultRepository = matchResultRepository;
		this.scoreRepository = scoreRepository;
	}

	@GetMapping("/{id}/scores")
	public ResponseEntity<List<Score>> listScores(@PathVariable("id") Long roundId) {
		if (roundRepository.findById(roundId).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(scoreRepository.findByRound_Id(roundId));
	}

	@PostMapping("/{id}/scores")
	@PreAuthorize("hasRole('REFEREE')")
	public ResponseEntity<?> createScore(
			@PathVariable("id") Long roundId,
			@Valid @RequestBody CreateScoreRequest body
	) {
		Round round = roundRepository.findById(roundId).orElse(null);
		if (round == null) {
			return ResponseEntity.notFound().build();
		}

		String teamId = extractTeamId(body.getTeam());
		if (teamId == null || teamId.isBlank()) {
			return ResponseEntity.badRequest().body("Team is mandatory");
		}

		Team team = teamRepository.findById(teamId).orElse(null);
		if (team == null) {
			return ResponseEntity.badRequest().body("Team does not exist");
		}

		boolean participates = matchResultRepository.existsByTeam_NameAndMatch_Round_Id(teamId, roundId);
		if (!participates) {
			return ResponseEntity.badRequest().body("Team is not registered in the competition");
		}

		if (scoreRepository.existsByRound_IdAndTeam_Name(roundId, teamId)) {
			return ResponseEntity.badRequest().body("Team already has a score for this round");
		}

		Score score = new Score();
		score.setRound(round);
		score.setTeam(team);
		score.setPoints(body.getPoints());

		Score saved = scoreRepository.save(score);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{scoreId}")
				.buildAndExpand(saved.getId())
				.toUri();

		return ResponseEntity.created(location).body(saved);
	}

	private String extractTeamId(String teamUri) {
		if (teamUri == null) {
			return "";
		}

		String trimmed = teamUri.trim();
		int lastSlash = trimmed.lastIndexOf('/');
		return lastSlash >= 0 ? trimmed.substring(lastSlash + 1) : trimmed;
	}

	@Data
	public static class CreateScoreRequest {

		@NotNull
		private String team;

		@NotNull
		private Integer points;
	}
}
