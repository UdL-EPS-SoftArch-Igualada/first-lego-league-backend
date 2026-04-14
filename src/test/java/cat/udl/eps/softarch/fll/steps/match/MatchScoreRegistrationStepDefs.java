package cat.udl.eps.softarch.fll.steps.match;

import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.MatchResult;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.match.MatchRepository;
import cat.udl.eps.softarch.fll.repository.match.MatchResultRepository;
import cat.udl.eps.softarch.fll.repository.team.TeamRepository;
import cat.udl.eps.softarch.fll.steps.app.AuthenticationStepDefs;
import cat.udl.eps.softarch.fll.steps.app.StepDefs;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import io.cucumber.java.en.Then;

public class MatchScoreRegistrationStepDefs {

	private final StepDefs stepDefs;
	private final TeamRepository teamRepository;
	private final MatchRepository matchRepository;
	private final MatchResultRepository matchResultRepository;
	private Match match;
	private Team teamA;
	private Team teamB;

	public MatchScoreRegistrationStepDefs(StepDefs stepDefs,
										  TeamRepository teamRepository,
										  MatchRepository matchRepository,
										  MatchResultRepository matchResultRepository) {
		this.stepDefs = stepDefs;
		this.teamRepository = teamRepository;
		this.matchRepository = matchRepository;
		this.matchResultRepository = matchResultRepository;
	}

	@Given("^There is a finished match ready for score registration$")
	public void thereIsAFinishedMatchReadyForScoreRegistration() {
		createDefaultTeams();
		match = createMatch(LocalTime.of(10, 0), LocalTime.of(11, 0), teamA, teamB);
	}

	@Given("^There is an unfinished match ready for score registration$")
	public void thereIsAnUnfinishedMatchReadyForScoreRegistration() {
		createDefaultTeams();
		match = createMatch(LocalTime.of(10, 0), null, teamA, teamB);
	}

	@Given("^There is a match with invalid time range ready for score registration$")
	public void thereIsAMatchWithInvalidTimeRangeReadyForScoreRegistration() {
		createDefaultTeams();
		match = createMatch(LocalTime.of(11, 0), LocalTime.of(10, 0), teamA, teamB);
	}

	@Given("^There is already a registered score for that match$")
	public void thereIsAlreadyARegisteredScoreForThatMatch() {
		MatchResult existingResult = MatchResult.create(100, match, teamA);
		matchResultRepository.save(existingResult);
	}

	@When("^I register a final score of (-?\\d+) for team A and (-?\\d+) for team B$")
	public void iRegisterAFinalScoreForTeams(int teamAScore, int teamBScore) throws Throwable {
		postRegisterScorePayload(match.getId(), String.valueOf(teamA.getId()), String.valueOf(teamB.getId()), teamAScore, teamBScore);
	}

	@When("^I register a score for a non existing match$")
	public void iRegisterAScoreForANonExistingMatch() throws Throwable {
		postRegisterScorePayload(Long.MAX_VALUE, String.valueOf(teamA.getId()), String.valueOf(teamB.getId()), 120, 95);
	}

	@When("^I register a final score with mismatched teams$")
	public void iRegisterAFinalScoreWithMismatchedTeams() throws Throwable {
		Team outsider = createTeam("Outsider-" + UUID.randomUUID().toString().substring(0, 8));
		postRegisterScorePayload(match.getId(), String.valueOf(outsider.getId()), String.valueOf(teamB.getId()), 120, 95);
	}

	@When("^I register a final score using the same team for both sides$")
	public void iRegisterAFinalScoreUsingTheSameTeamForBothSides() throws Throwable {
		postRegisterScorePayload(match.getId(), String.valueOf(teamA.getId()), String.valueOf(teamA.getId()), 120, 95);
	}

	@When("^I register a final score with null score payload$")
	public void iRegisterAFinalScoreWithNullScorePayload() throws Throwable {
		JSONObject payload = new JSONObject();
		payload.put("matchId", match.getId());
		payload.put("score", JSONObject.NULL);
		postRegisterRawPayload(payload.toString());
	}

	@When("^I register a final score with invalid score format$")
	public void iRegisterAFinalScoreWithInvalidScoreFormat() throws Throwable {
		String payload = "{\"matchId\":" + match.getId()
			+ ",\"score\":{\"teamAId\":\"" + teamA.getId()
			+ "\",\"teamBId\":\"" + teamB.getId()
			+ "\",\"teamAScore\":\"invalid\",\"teamBScore\":95}}";
		postRegisterRawPayload(payload);
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

	@Then("^The match score error response has code \"([^\"]*)\"$")
	public void theMatchScoreErrorResponseHasCode(String errorCode) throws Throwable {
		stepDefs.result
			.andExpect(jsonPath("$.error").value(errorCode))
			.andExpect(jsonPath("$.timestamp").exists())
			.andExpect(jsonPath("$.path").value("/matchResults/register"));
	}

	private void postRegisterScorePayload(Long matchId, String payloadTeamAId, String payloadTeamBId, int payloadTeamAScore, int payloadTeamBScore)
		throws Throwable {
		JSONObject scorePayload = new JSONObject();
		scorePayload.put("teamAId", payloadTeamAId);
		scorePayload.put("teamBId", payloadTeamBId);
		scorePayload.put("teamAScore", payloadTeamAScore);
		scorePayload.put("teamBScore", payloadTeamBScore);

		JSONObject payload = new JSONObject();
		payload.put("matchId", matchId);
		payload.put("score", scorePayload);
		postRegisterRawPayload(payload.toString());
	}

	private void postRegisterRawPayload(String payload) throws Throwable {
		stepDefs.result = stepDefs.mockMvc.perform(post("/matchResults/register")
			.contentType(MediaType.APPLICATION_JSON)
			.content(payload)
			.characterEncoding(StandardCharsets.UTF_8)
			.accept(MediaType.APPLICATION_JSON)
			.with(AuthenticationStepDefs.authenticate()));
	}

	private void createDefaultTeams() {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		teamA = createTeam("TeamA-" + suffix);
		teamB = createTeam("TeamB-" + suffix);
	}

	private Match createMatch(LocalTime startTime, LocalTime endTime, Team assignedTeamA, Team assignedTeamB) {
		Match createdMatch = new Match();
		createdMatch.setStartTime(startTime);
		createdMatch.setEndTime(endTime);
		createdMatch.setTeamA(assignedTeamA);
		createdMatch.setTeamB(assignedTeamB);
		return matchRepository.save(createdMatch);
	}

	private Team createTeam(String teamName) {
		Team team = Team.create(teamName, "Igualada", 2000, "Junior");
		team.setEducationalCenter("EPS");
		team.setInscriptionDate(LocalDate.now());
		return teamRepository.save(team);
	}
}