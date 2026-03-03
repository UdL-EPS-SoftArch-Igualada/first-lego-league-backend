package cat.udl.eps.softarch.fll.steps;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.MatchRepository;
import cat.udl.eps.softarch.fll.repository.TeamRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class MatchScoreRegistrationStepDefs {

	private final StepDefs stepDefs;
	private final TeamRepository teamRepository;
	private final MatchRepository matchRepository;
	private Match match;
	private Team teamA;
	private Team teamB;

	public MatchScoreRegistrationStepDefs(StepDefs stepDefs, TeamRepository teamRepository, MatchRepository matchRepository) {
		this.stepDefs = stepDefs;
		this.teamRepository = teamRepository;
		this.matchRepository = matchRepository;
	}

	@Given("^There is a finished match ready for score registration$")
	public void thereIsAFinishedMatchReadyForScoreRegistration() {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		teamA = createTeam("TeamA-" + suffix);
		teamB = createTeam("TeamB-" + suffix);

		match = new Match();
		match.setStartTime(LocalTime.of(10, 0));
		match.setEndTime(LocalTime.of(11, 0));
		match.setTeamA(teamA);
		match.setTeamB(teamB);
		match = matchRepository.save(match);
	}

	@When("^I register a final score of (-?\\d+) for team A and (-?\\d+) for team B$")
	public void iRegisterAFinalScoreForTeams(int teamAScore, int teamBScore) throws Throwable {
		JSONObject scorePayload = new JSONObject();
		scorePayload.put("teamAId", teamA.getId());
		scorePayload.put("teamBId", teamB.getId());
		scorePayload.put("teamAScore", teamAScore);
		scorePayload.put("teamBScore", teamBScore);

		JSONObject payload = new JSONObject();
		payload.put("matchId", match.getId());
		payload.put("score", scorePayload);

		stepDefs.result = stepDefs.mockMvc.perform(post("/matchResults/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload.toString())
				.characterEncoding(StandardCharsets.UTF_8)
				.accept(MediaType.APPLICATION_JSON)
				.with(AuthenticationStepDefs.authenticate()));
	}

	@When("^I try to create a match result directly with score (-?\\d+)$")
	public void iTryToCreateAMatchResultDirectlyWithScore(int score) throws Throwable {
		JSONObject payload = new JSONObject();
		payload.put("score", score);
		payload.put("team", "http://localhost/teams/" + teamA.getId());
		payload.put("match", "http://localhost/matches/" + match.getId());

		stepDefs.result = stepDefs.mockMvc.perform(post("/matchResults")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload.toString())
				.characterEncoding(StandardCharsets.UTF_8)
				.accept(MediaType.APPLICATION_JSON)
				.with(AuthenticationStepDefs.authenticate()));
	}

	@And("^The register response contains successful flags$")
	public void theRegisterResponseContainsSuccessfulFlags() throws Throwable {
		stepDefs.result
				.andExpect(jsonPath("$.matchId").value(match.getId()))
				.andExpect(jsonPath("$.resultSaved").value(true))
				.andExpect(jsonPath("$.rankingUpdated").value(true));
	}

	private Team createTeam(String teamName) {
		Team team = new Team();
		team.setName(teamName);
		team.setCity("Igualada");
		team.setFoundationYear(2000);
		team.setCategory("Junior");
		team.setEducationalCenter("EPS");
		team.setInscriptionDate(LocalDate.now());
		return teamRepository.save(team);
	}
}
