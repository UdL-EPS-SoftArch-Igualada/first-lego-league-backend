package cat.udl.eps.softarch.fll.steps;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.util.UriUtils;
import cat.udl.eps.softarch.fll.domain.Edition;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.EditionRepository;
import cat.udl.eps.softarch.fll.repository.TeamRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ManageScientificProjectStepDefs {

	private final StepDefs stepDefs;
	private final TeamRepository teamRepository;
	private final EditionRepository editionRepository;
	private String latestScientificProjectUri;
	private Long trackedEditionId;

	public ManageScientificProjectStepDefs(
			StepDefs stepDefs,
			TeamRepository teamRepository,
			EditionRepository editionRepository) {
		this.stepDefs = stepDefs;
		this.teamRepository = teamRepository;
		this.editionRepository = editionRepository;
	}

	private ResultActions performCreateProject(
			Integer score,
			String comments,
			String teamUri,
			String editionUri) throws Exception {
		JSONObject payload = new JSONObject();
		payload.put("score", score);
		payload.put("comments", comments);
		if (teamUri != null) {
			payload.put("team", teamUri);
		}
		if (editionUri != null) {
			payload.put("edition", editionUri);
		}

		return stepDefs.mockMvc.perform(
				post("/scientificProjects")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload.toString())
						.characterEncoding(StandardCharsets.UTF_8)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()));
	}

	private String ensureTeamExists(String teamName) throws Exception {
		JSONObject teamJson = new JSONObject();
		teamJson.put("name", teamName);
		teamJson.put("city", "Igualada");
		teamJson.put("foundationYear", 2005);
		teamJson.put("category", "Challenge");

		MockHttpServletResponse response = stepDefs.mockMvc.perform(
				post("/teams")
						.contentType(MediaType.APPLICATION_JSON)
						.content(teamJson.toString())
						.characterEncoding(StandardCharsets.UTF_8)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andReturn().getResponse();

		if (response.getStatus() == 201) {
			return response.getHeader("Location");
		}
		if (response.getStatus() == 409) {
			return "http://localhost/teams/" + UriUtils.encodePathSegment(teamName, StandardCharsets.UTF_8);
		}
		throw new RuntimeException("Unable to create dependency team: " + response.getContentAsString());
	}

	private String createEdition() throws Exception {
		JSONObject editionJson = new JSONObject();
		editionJson.put("year", 2026 + Math.floorMod(UUID.randomUUID().hashCode(), 100));
		editionJson.put("venueName", "Venue-" + UUID.randomUUID().toString().substring(0, 8));
		editionJson.put("description", "Edition for scientific project tests");

		MockHttpServletResponse response = stepDefs.mockMvc.perform(
				post("/editions")
						.contentType(MediaType.APPLICATION_JSON)
						.content(editionJson.toString())
						.characterEncoding(StandardCharsets.UTF_8)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andReturn().getResponse();

		if (response.getStatus() != 201) {
			throw new RuntimeException("Unable to create dependency edition: " + response.getContentAsString());
		}
		return response.getHeader("Location");
	}

	private Long extractEditionId(String editionUri) {
		if (editionUri == null) {
			throw new IllegalArgumentException("editionUri must not be null");
		}
		String path = URI.create(editionUri).getPath();
		String[] parts = path.split("/");
		return Long.parseLong(parts[parts.length - 1]);
	}

	private String extractTeamName(String teamUri) {
		if (teamUri == null) {
			throw new IllegalArgumentException("teamUri must not be null");
		}
		String path = URI.create(teamUri).getPath();
		String[] parts = path.split("/");
		return UriUtils.decode(parts[parts.length - 1], StandardCharsets.UTF_8);
	}

	private void registerTeamInEdition(String teamUri, String editionUri) {
		String teamName = extractTeamName(teamUri);
		Long editionId = extractEditionId(editionUri);

		Team team = teamRepository.findByNameWithRegisteredEditions(teamName)
				.orElseThrow(() -> new IllegalStateException("Missing team in test setup: " + teamName));
		Edition edition = editionRepository.findById(editionId)
				.orElseThrow(() -> new IllegalStateException("Missing edition in test setup: " + editionId));

		team.registerEdition(edition);
		teamRepository.save(team);
	}

	private void captureLatestProjectUriIfCreated() throws Exception {
		if (stepDefs.result.andReturn().getResponse().getStatus() == 201) {
			latestScientificProjectUri = stepDefs.result.andReturn().getResponse().getHeader("Location");
		}
	}

	@When("I create a new scientific project with score {int} and comments {string} for team {string} and a valid edition")
	public void iCreateScientificProjectForTeamAndValidEdition(Integer score, String comments, String teamName) throws Exception {
		latestScientificProjectUri = null;
		String teamUri = ensureTeamExists(teamName);
		String editionUri = createEdition();
		registerTeamInEdition(teamUri, editionUri);
		stepDefs.result = performCreateProject(score, comments, teamUri, editionUri);
		captureLatestProjectUriIfCreated();
	}

	@When("I create a new scientific project with score {int} and comments {string} without team and with valid edition")
	public void iCreateScientificProjectWithoutTeamWithValidEdition(Integer score, String comments) throws Exception {
		latestScientificProjectUri = null;
		String editionUri = createEdition();
		stepDefs.result = performCreateProject(score, comments, null, editionUri);
		captureLatestProjectUriIfCreated();
	}

	@When("I create a new scientific project with score {int} and comments {string} without edition and with valid team {string}")
	public void iCreateScientificProjectWithoutEditionWithValidTeam(Integer score, String comments, String teamName) throws Exception {
		latestScientificProjectUri = null;
		String teamUri = ensureTeamExists(teamName);
		stepDefs.result = performCreateProject(score, comments, teamUri, null);
		captureLatestProjectUriIfCreated();
	}

	@When("I create a new scientific project with score {int} and comments {string} and invalid team with valid edition")
	public void iCreateScientificProjectWithInvalidTeamWithValidEdition(Integer score, String comments) throws Exception {
		latestScientificProjectUri = null;
		String editionUri = createEdition();
		String invalidTeamUri = "http://localhost/teams/" + UriUtils.encodePathSegment("UnknownTeam-" + UUID.randomUUID(), StandardCharsets.UTF_8);
		stepDefs.result = performCreateProject(score, comments, invalidTeamUri, editionUri);
		captureLatestProjectUriIfCreated();
	}

	@When("I create a new scientific project with score {int} and comments {string} and invalid edition for team {string}")
	public void iCreateScientificProjectWithInvalidEdition(Integer score, String comments, String teamName) throws Exception {
		latestScientificProjectUri = null;
		String teamUri = ensureTeamExists(teamName);
		String invalidEditionUri = "http://localhost/editions/999999999";
		stepDefs.result = performCreateProject(score, comments, teamUri, invalidEditionUri);
		captureLatestProjectUriIfCreated();
	}

	@When("I create a new scientific project with score {int} and comments {string} for unregistered team {string}")
	public void iCreateScientificProjectWithUnregisteredTeam(Integer score, String comments, String teamName) throws Exception {
		latestScientificProjectUri = null;
		String teamUri = ensureTeamExists(teamName);
		String editionUri = createEdition();
		stepDefs.result = performCreateProject(score, comments, teamUri, editionUri);
		captureLatestProjectUriIfCreated();
	}

	@Given("There is a scientific project with score {int} and comments {string} for team {string} and a valid edition")
	public void thereIsAScientificProjectForTeamAndEdition(Integer score, String comments, String teamName) throws Exception {
		String teamUri = ensureTeamExists(teamName);
		String editionUri = createEdition();
		registerTeamInEdition(teamUri, editionUri);
		ResultActions createAction = performCreateProject(score, comments, teamUri, editionUri);
		createAction.andExpect(status().isCreated());
		latestScientificProjectUri = createAction.andReturn().getResponse().getHeader("Location");
	}

	@Given("There is a scientific project with score {int} and comments {string} for team {string} in a tracked edition")
	public void thereIsAScientificProjectInATrackedEdition(Integer score, String comments, String teamName) throws Exception {
		trackedEditionId = null;
		String teamUri = ensureTeamExists(teamName);
		String editionUri = createEdition();
		registerTeamInEdition(teamUri, editionUri);
		trackedEditionId = extractEditionId(editionUri);
		ResultActions createAction = performCreateProject(score, comments, teamUri, editionUri);
		createAction.andExpect(status().isCreated());
		latestScientificProjectUri = createAction.andReturn().getResponse().getHeader("Location");
	}

	@When("I search for scientific projects with minimum score {int}")
	public void iSearchScientificProjectsByMinScore(Integer minScore) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/scientificProjects/search/findByScoreGreaterThanEqual")
						.param("minScore", minScore.toString())
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()));
	}

	@When("I search for scientific projects by team name {string}")
	public void iSearchScientificProjectsByTeamName(String teamName) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/scientificProjects/search/findByTeamName")
						.param("teamName", teamName)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()));
	}

	@When("I search for scientific projects by the tracked edition")
	public void iSearchScientificProjectsByTrackedEdition() throws Exception {
		if (trackedEditionId == null) {
			throw new IllegalStateException("No tracked edition is available in the current scenario.");
		}
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/scientificProjects/search/findByEditionId")
						.param("editionId", trackedEditionId.toString())
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()));
	}

	@When("I request the team of the latest scientific project")
	public void iRequestTheTeamOfTheLatestScientificProject() throws Exception {
		if (latestScientificProjectUri == null) {
			throw new IllegalStateException("No scientific project URI is available in the current scenario.");
		}
		String path = URI.create(latestScientificProjectUri).getPath() + "/team";
		stepDefs.result = stepDefs.mockMvc.perform(
				get(path)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()));
	}

	@When("I request the edition of the latest scientific project")
	public void iRequestTheEditionOfTheLatestScientificProject() throws Exception {
		if (latestScientificProjectUri == null) {
			throw new IllegalStateException("No scientific project URI is available in the current scenario.");
		}
		String path = URI.create(latestScientificProjectUri).getPath() + "/edition";
		stepDefs.result = stepDefs.mockMvc.perform(
				get(path)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()));
	}

	@Then("The response has a team link")
	public void theResponseHasATeamLink() throws Exception {
		stepDefs.result.andExpect(jsonPath("$._links.team.href").exists());
	}

	@Then("The response has an edition link")
	public void theResponseHasAnEditionLink() throws Exception {
		stepDefs.result.andExpect(jsonPath("$._links.edition.href").exists());
	}

	@And("The latest scientific project has a team relation endpoint")
	public void theLatestScientificProjectHasATeamRelationEndpoint() throws Exception {
		iRequestTheTeamOfTheLatestScientificProject();
		stepDefs.result.andExpect(status().isOk());
		stepDefs.result.andExpect(jsonPath("$._links.self.href").exists());
	}

	@And("The latest scientific project has an edition relation endpoint")
	public void theLatestScientificProjectHasAnEditionRelationEndpoint() throws Exception {
		iRequestTheEditionOfTheLatestScientificProject();
		stepDefs.result.andExpect(status().isOk());
		stepDefs.result.andExpect(jsonPath("$._links.self.href").exists());
	}

	@Then("The response contains {int} scientific project\\(s)")
	public void theResponseContainsNProjects(Integer count) throws Exception {
		stepDefs.result.andExpect(
				jsonPath("$._embedded.scientificProjects", hasSize(count)));
	}
}
