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
import io.cucumber.java.en.Given;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ManageScientificProjectStepDefs {

	private final StepDefs stepDefs;
	private String latestScientificProjectUri;

	public ManageScientificProjectStepDefs(StepDefs stepDefs) {
		this.stepDefs = stepDefs;
	}

	private ResultActions performCreateProject(Integer score, String comments, String teamUri, boolean includeTeam) throws Exception {
		JSONObject payload = new JSONObject();
		payload.put("score", score);
		payload.put("comments", comments);
		if (includeTeam) {
			payload.put("team", teamUri);
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

	@When("I create a new scientific project with score {int} and comments {string} for team {string}")
	public void iCreateScientificProjectForTeam(Integer score, String comments, String teamName) throws Exception {
		latestScientificProjectUri = null;
		String teamUri = ensureTeamExists(teamName);
		stepDefs.result = performCreateProject(score, comments, teamUri, true);
		if (stepDefs.result.andReturn().getResponse().getStatus() == 201) {
			latestScientificProjectUri = stepDefs.result.andReturn().getResponse().getHeader("Location");
		}
	}

	@When("I create a new scientific project with score {int} and comments {string} without team")
	public void iCreateScientificProjectWithoutTeam(Integer score, String comments) throws Exception {
		latestScientificProjectUri = null;
		stepDefs.result = performCreateProject(score, comments, null, false);
		if (stepDefs.result.andReturn().getResponse().getStatus() == 201) {
			latestScientificProjectUri = stepDefs.result.andReturn().getResponse().getHeader("Location");
		}
	}

	@When("I create a new scientific project with score {int} and comments {string} and invalid team")
	public void iCreateScientificProjectWithInvalidTeam(Integer score, String comments) throws Exception {
		latestScientificProjectUri = null;
		String invalidTeamUri = "non-existing-" + UUID.randomUUID();
		stepDefs.result = performCreateProject(score, comments, invalidTeamUri, true);
		if (stepDefs.result.andReturn().getResponse().getStatus() == 201) {
			latestScientificProjectUri = stepDefs.result.andReturn().getResponse().getHeader("Location");
		}
	}

	@Given("There is a scientific project with score {int} and comments {string} for team {string}")
	public void thereIsAScientificProjectForTeam(Integer score, String comments, String teamName) throws Exception {
		String teamUri = ensureTeamExists(teamName);
		ResultActions createAction = performCreateProject(score, comments, teamUri, true);
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

	@Then("The response has a team link")
	public void theResponseHasATeamLink() throws Exception {
		stepDefs.result.andExpect(jsonPath("$._links.team.href").exists());
	}

	@And("The latest scientific project has a team relation endpoint")
	public void theLatestScientificProjectHasATeamRelationEndpoint() throws Exception {
		iRequestTheTeamOfTheLatestScientificProject();
		stepDefs.result.andExpect(status().isOk());
		stepDefs.result.andExpect(jsonPath("$._links.self.href").exists());
	}

	@Then("The response contains {int} scientific project\\(s)")
	public void theResponseContainsNProjects(Integer count) throws Exception {
		stepDefs.result.andExpect(
				jsonPath("$._embedded.scientificProjects", hasSize(count)));
	}
}
