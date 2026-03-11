package cat.udl.eps.softarch.fll.steps;

import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.TeamMemberRepository;
import cat.udl.eps.softarch.fll.repository.TeamRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriUtils;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TeamMemberStepDefs {

	private final StepDefs stepDefs;
	private final TeamRepository teamRepository;
	private final TeamMemberRepository teamMemberRepository;

	private String latestTeamMemberUri;
	private Long latestTeamMemberId;

	public TeamMemberStepDefs(StepDefs stepDefs, TeamRepository teamRepository, TeamMemberRepository teamMemberRepository) {
		this.stepDefs = stepDefs;
		this.teamRepository = teamRepository;
		this.teamMemberRepository = teamMemberRepository;
	}

	@Given("the team member API system is empty")
	public void clearTeamMemberApiSystem() {
		teamMemberRepository.deleteAll();
		teamRepository.deleteAll();
		stepDefs.result = null;
		latestTeamMemberUri = null;
		latestTeamMemberId = null;
	}

	@Given("a team with name {string} exists for team member management")
	public void aTeamWithNameExistsForTeamMemberManagement(String teamName) {
		if (teamRepository.existsById(teamName)) {
			return;
		}
		Team team = Team.create(teamName, "Igualada", 2005, "Challenge");
		teamRepository.save(team);
	}

	@When("I create a team member with name {string} birth date {string} and role {string} for team {string}")
	public void iCreateATeamMemberForTeam(String name, String birthDate, String role, String teamName) throws Exception {
		JSONObject payload = buildTeamMemberPayload(name, birthDate, role, teamName);

		stepDefs.result = stepDefs.mockMvc.perform(
				post("/teamMembers")
					.contentType(MediaType.APPLICATION_JSON)
					.content(payload.toString())
					.characterEncoding(StandardCharsets.UTF_8)
					.accept(MediaType.APPLICATION_JSON)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());

		captureLatestTeamMemberIfCreated();
	}

	@When("I try to create a team member missing {string} for team {string}")
	public void iTryToCreateATeamMemberMissingField(String field, String teamName) throws Exception {
		JSONObject payload =
			buildTeamMemberPayload("Valid Name", LocalDate.of(2010, 1, 1).toString(), "Builder", teamName);
		payload.remove(field);

		stepDefs.result = stepDefs.mockMvc.perform(
				post("/teamMembers")
					.contentType(MediaType.APPLICATION_JSON)
					.content(payload.toString())
					.characterEncoding(StandardCharsets.UTF_8)
					.accept(MediaType.APPLICATION_JSON)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@When("I retrieve the created team member by id")
	public void iRetrieveTheCreatedTeamMemberById() throws Exception {
		assertLatestTeamMemberIdPresent();
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/teamMembers/{id}", latestTeamMemberId)
					.accept(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@When("I list all team members")
	public void iListAllTeamMembers() throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/teamMembers")
					.accept(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@When("I search team members by role {string}")
	public void iSearchTeamMembersByRole(String role) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/teamMembers/search/findByRole")
						.param("role", role)
						.accept(MediaType.APPLICATION_JSON)
						.characterEncoding(StandardCharsets.UTF_8)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}

	@When("I delete the created team member")
	public void iDeleteTheCreatedTeamMember() throws Exception {
		assertLatestTeamMemberIdPresent();
		stepDefs.result = stepDefs.mockMvc.perform(
				delete("/teamMembers/{id}", latestTeamMemberId)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@When("I retrieve the deleted team member by id")
	public void iRetrieveTheDeletedTeamMemberById() throws Exception {
		iRetrieveTheCreatedTeamMemberById();
	}

	@Then("The created team member has name {string} and role {string}")
	public void theCreatedTeamMemberHasNameAndRole(String expectedName, String expectedRole) throws Exception {
		assertLatestTeamMemberUriPresent();
		stepDefs.mockMvc.perform(
				get(pathFromAbsoluteUri(latestTeamMemberUri))
					.accept(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name", is(expectedName)))
			.andExpect(jsonPath("$.role", is(expectedRole)));
	}

	@Then("The created team member is linked to team {string}")
	public void theCreatedTeamMemberIsLinkedToTeam(String expectedTeamName) throws Exception {
		assertLatestTeamMemberUriPresent();
		stepDefs.mockMvc.perform(
				get(pathFromAbsoluteUri(latestTeamMemberUri) + "/team")
					.accept(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", is(expectedTeamName)));
	}

	@Then("The response contains team member name {string} and role {string}")
	public void theResponseContainsTeamMemberNameAndRole(String expectedName, String expectedRole) throws Exception {
		stepDefs.result
			.andExpect(jsonPath("$.name", is(expectedName)))
			.andExpect(jsonPath("$.role", is(expectedRole)));
	}

	@And("The team member list contains name {string}")
	public void theTeamMemberListContainsName(String expectedName) throws Exception {
		stepDefs.result.andExpect(jsonPath("$._embedded.teamMembers[*].name", hasItem(is(expectedName))));
	}

	@And("The team member list is empty")
	public void theTeamMemberListIsEmpty() throws Exception {
		stepDefs.result.andExpect(jsonPath("$._embedded.teamMembers", hasSize(0)));
	}

	private JSONObject buildTeamMemberPayload(String name, String birthDate, String role, String teamName) throws Exception {
		JSONObject payload = new JSONObject();
		payload.put("uri", "/teamMembers/" + name.replace(" ", "-").toLowerCase());
		payload.put("name", name);
		payload.put("birthDate", birthDate);
		payload.put("role", role);
		payload.put("team", absoluteTeamUri(teamName));
		return payload;
	}

	private String absoluteTeamUri(String teamName) {
		return "http://localhost/teams/" + UriUtils.encodePathSegment(teamName, StandardCharsets.UTF_8);
	}

	private void captureLatestTeamMemberIfCreated() throws Exception {
		latestTeamMemberUri = null;
		latestTeamMemberId = null;
		if (stepDefs.result.andReturn().getResponse().getStatus() != 201) {
			return;
		}
		latestTeamMemberUri = stepDefs.result.andReturn().getResponse().getHeader("Location");
		if (latestTeamMemberUri == null || latestTeamMemberUri.isBlank()) {
			throw new IllegalStateException("Created team member response does not contain a Location header.");
		}
		String path = URI.create(latestTeamMemberUri).getPath();
		String idToken = path.substring(path.lastIndexOf('/') + 1);
		try {
			latestTeamMemberId = Long.valueOf(idToken);
		} catch (NumberFormatException e) {
			throw new IllegalStateException(
				"Could not parse team member ID from Location header: " + latestTeamMemberUri, e);
		}
	}

	private void assertLatestTeamMemberIdPresent() {
		if (latestTeamMemberId == null) {
			throw new IllegalStateException("No current team member id is available. Create a team member first.");
		}
	}

	private void assertLatestTeamMemberUriPresent() {
		if (latestTeamMemberUri == null || latestTeamMemberUri.isBlank()) {
			throw new IllegalStateException("No current team member URI is available. Create a team member first.");
		}
	}

	private String pathFromAbsoluteUri(String absoluteUri) {
		return URI.create(absoluteUri).getPath();
	}
}
