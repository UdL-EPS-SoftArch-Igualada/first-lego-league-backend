package cat.udl.eps.softarch.fll.steps.award;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import cat.udl.eps.softarch.fll.steps.app.AuthenticationStepDefs;
import cat.udl.eps.softarch.fll.steps.app.StepDefs;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.repository.MatchRepository;
import com.fasterxml.jackson.databind.JsonNode;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class AwardAndMatchStepDefs {

	private final StepDefs stepDefs;
	private final MatchRepository matchRepository;
	private String teamUri;
	private String editionUri;
	private String matchUri;

	public AwardAndMatchStepDefs(StepDefs stepDefs, MatchRepository matchRepository) {
		this.stepDefs = stepDefs;
		this.matchRepository = matchRepository;
	}

	@Given("^The dependencies exist$")
	public void theDependenciesExist() throws Throwable {
		String suffix = UUID.randomUUID().toString().substring(0, 8);

		JSONObject editionJson = new JSONObject();
		editionJson.put("year", 2025 + Math.abs(suffix.hashCode() % 100));
		editionJson.put("venueName", "EPS Igualada");
		editionJson.put("description", "Test Edition");

		var edRes = stepDefs.mockMvc.perform(post("/editions")
			.contentType(MediaType.APPLICATION_JSON)
			.content(editionJson.toString())
			.characterEncoding(StandardCharsets.UTF_8)
			.with(httpBasic("admin", "password"))).andReturn().getResponse();

		if (edRes.getStatus() != 201 || edRes.getHeader("Location") == null) {
			throw new RuntimeException("ERROR CREANT EDITION: " + edRes.getContentAsString());
		}
		editionUri = edRes.getHeader("Location");

		JSONObject teamJson = new JSONObject();
		teamJson.put("name", "TeamA-" + suffix);
		teamJson.put("city", "Igualada");
		teamJson.put("foundationYear", 2000);
		teamJson.put("category", "Junior");

		var teamRes = stepDefs.mockMvc.perform(post("/teams")
			.contentType(MediaType.APPLICATION_JSON)
			.content(teamJson.toString())
			.characterEncoding(StandardCharsets.UTF_8)
			.with(httpBasic("admin", "password"))).andReturn().getResponse();

		if (teamRes.getStatus() != 201 || teamRes.getHeader("Location") == null) {
			throw new RuntimeException("ERROR CREANT TEAM: " + teamRes.getContentAsString());
		}
		teamUri = teamRes.getHeader("Location");

		Match match = new Match();
		match = matchRepository.save(match);
		matchUri = "http://localhost/matches/" + match.getId();
	}

	@When("^I request the match results list$")
	public void iRequestTheMatchResultsList() throws Throwable {
		var request = get("/matchResults")
			.accept(MediaType.APPLICATION_JSON)
			.with(AuthenticationStepDefs.authenticate());
		stepDefs.result = stepDefs.mockMvc.perform(request);
	}

	@When("^I request the awards list$")
	public void iRequestTheAwardsList() throws Throwable {
		var request = get("/awards")
			.accept(MediaType.APPLICATION_JSON)
			.with(AuthenticationStepDefs.authenticate());
		stepDefs.result = stepDefs.mockMvc.perform(request);
	}

	@When("^I create a match result with score (-?\\d+)$")
	public void iCreateAMatchResultWithScore(int score) throws Throwable {
		JSONObject payload = new JSONObject();
		payload.put("score", score);
		payload.put("team", teamUri);
		payload.put("match", matchUri);

		var request = post("/matchResults")
			.contentType(MediaType.APPLICATION_JSON)
			.content(payload.toString())
			.characterEncoding(StandardCharsets.UTF_8)
			.accept(MediaType.APPLICATION_JSON)
			.with(AuthenticationStepDefs.authenticate());

		stepDefs.result = stepDefs.mockMvc.perform(request);
	}

	@When("^I create an award with name \"([^\"]*)\"$")
	public void iCreateAnAwardWithName(String name) throws Throwable {
		JSONObject payload = new JSONObject();
		payload.put("name", name + "-" + UUID.randomUUID().toString().substring(0, 4));
		payload.put("winner", teamUri);
		payload.put("edition", editionUri);

		var request = post("/awards")
			.contentType(MediaType.APPLICATION_JSON)
			.content(payload.toString())
			.characterEncoding(StandardCharsets.UTF_8)
			.accept(MediaType.APPLICATION_JSON)
			.with(AuthenticationStepDefs.authenticate());

		stepDefs.result = stepDefs.mockMvc.perform(request);
	}

	@When("^I create an award with no name$")
	public void iCreateAnAwardWithNoName() throws Throwable {
		JSONObject payload = new JSONObject();
		payload.put("winner", teamUri);
		payload.put("edition", editionUri);

		var request = post("/awards")
			.contentType(MediaType.APPLICATION_JSON)
			.content(payload.toString())
			.characterEncoding(StandardCharsets.UTF_8)
			.accept(MediaType.APPLICATION_JSON)
			.with(AuthenticationStepDefs.authenticate());

		stepDefs.result = stepDefs.mockMvc.perform(request);
	}

	@Given("A team exists with name {string} and an award {string}")
	public void aTeamExistsWithNameAndAnAward(String teamName, String awardName) throws Throwable {
		String suffix = UUID.randomUUID().toString().substring(0, 8);

		JSONObject editionJson = new JSONObject();
		editionJson.put("year", 2025 + Math.abs(suffix.hashCode() % 100));
		editionJson.put("venueName", "EPS Igualada");
		editionJson.put("description", "Test Edition");

		var edRes = stepDefs.mockMvc.perform(post("/editions")
			.contentType(MediaType.APPLICATION_JSON)
			.content(editionJson.toString())
			.characterEncoding(StandardCharsets.UTF_8)
			.with(httpBasic("admin", "password"))).andReturn().getResponse();
			if (edRes.getStatus() != 201 || edRes.getHeader("Location") == null) {
				throw new RuntimeException("Failed to create edition: " + edRes.getContentAsString());
			}
		editionUri = edRes.getHeader("Location");

		JSONObject teamJson = new JSONObject();
		teamJson.put("name", teamName);
		teamJson.put("city", "Igualada");
		teamJson.put("foundationYear", 2000);
		teamJson.put("category", "Junior");

		var teamRes = stepDefs.mockMvc.perform(post("/teams")
			.contentType(MediaType.APPLICATION_JSON)
			.content(teamJson.toString())
			.characterEncoding(StandardCharsets.UTF_8)
			.with(httpBasic("admin", "password"))).andReturn().getResponse();
			if (teamRes.getStatus() != 201 || teamRes.getHeader("Location") == null) {
				throw new RuntimeException("Failed to create team: " + teamRes.getContentAsString());
			}
		teamUri = teamRes.getHeader("Location");

		JSONObject awardJson = new JSONObject();
		awardJson.put("name", awardName);
		awardJson.put("winner", teamUri);
		awardJson.put("edition", editionUri);

		stepDefs.mockMvc.perform(post("/awards")
			.contentType(MediaType.APPLICATION_JSON)
			.content(awardJson.toString())
			.characterEncoding(StandardCharsets.UTF_8)
			.with(httpBasic("admin", "password")));
	}

	@When("I search awards by winner name containing {string}")
	public void iSearchAwardsByWinnerNameContaining(String name) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/awards/search/findByWinnerNameContainingIgnoreCase")
					.param("name", name)
					.accept(MediaType.APPLICATION_JSON))
			.andDo(print());
	}

	@And("^The award search response should contain (\\d+) result[s]?$")
	public void awardSearchResponseShouldContainCount(int expectedCount) throws Exception {
		String responseBody = stepDefs.result.andReturn().getResponse().getContentAsString();
		JsonNode root = stepDefs.mapper.readTree(responseBody);
		JsonNode awards = root.path("_embedded").path("awards");
		int actualCount = awards.isArray() ? awards.size() : 0;
		assertEquals(expectedCount, actualCount);
	}

	@And("The award search response should include award named {string}")
	public void awardSearchResponseShouldIncludeAwardNamed(String awardName) throws Exception {
		stepDefs.result.andExpect(jsonPath("$._embedded.awards[*].name", hasItem(awardName)));
	}
}