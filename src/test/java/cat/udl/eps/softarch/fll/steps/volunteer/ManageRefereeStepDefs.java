package cat.udl.eps.softarch.fll.steps.volunteer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import cat.udl.eps.softarch.fll.steps.app.AuthenticationStepDefs;
import cat.udl.eps.softarch.fll.steps.app.StepDefs;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ManageRefereeStepDefs {

	private final StepDefs stepDefs;
	private String currentRefereeUrl;

	public ManageRefereeStepDefs(StepDefs stepDefs) {
		this.stepDefs = stepDefs;
	}

	@When("I request to create a referee with name {string} and emailAddress {string} and phoneNumber {string} and expert {string}")
	public void iRequestToCreateAReferee(String name, String email, String phone, String expertStr) throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("name", name);
		body.put("emailAddress", email);
		body.put("phoneNumber", phone);
		body.put("expert", Boolean.parseBoolean(expertStr));

		stepDefs.result = stepDefs.mockMvc.perform(post("/referees")
			.contentType(MediaType.APPLICATION_JSON)
			.content(stepDefs.mapper.writeValueAsString(body))
			.characterEncoding(StandardCharsets.UTF_8)
			.with(AuthenticationStepDefs.authenticate()));

		saveUrlFromLocationHeader();
	}

	@Given("a referee exists with name {string} and emailAddress {string} and phoneNumber {string} and expert {string}")
	public void aRefereeExists(String name, String email, String phone, String expertStr) throws Exception {
		iRequestToCreateAReferee(name, email, phone, expertStr);
		stepDefs.result.andExpect(status().isCreated());
		assertNotNull(currentRefereeUrl, "Referee URL (Location header) should not be null after creation");
	}

	@When("I request to retrieve that referee")
	public void iRequestToRetrieveThatReferee() throws Exception {
		validateUrlIsPresent();
		stepDefs.result = stepDefs.mockMvc.perform(get(currentRefereeUrl)
			.with(AuthenticationStepDefs.authenticate()));
	}

	@When("I request to update the referee name to {string}")
	public void iRequestToUpdateTheRefereeNameTo(String newName) throws Exception {
		validateUrlIsPresent();
		Map<String, Object> body = new HashMap<>();
		body.put("name", newName);

		stepDefs.result = stepDefs.mockMvc.perform(patch(currentRefereeUrl)
			.contentType(MediaType.APPLICATION_JSON)
			.content(stepDefs.mapper.writeValueAsString(body))
			.characterEncoding(StandardCharsets.UTF_8)
			.with(AuthenticationStepDefs.authenticate()));
	}

	@When("I request to delete that referee")
	public void iRequestToDeleteThatReferee() throws Exception {
		validateUrlIsPresent();
		stepDefs.result = stepDefs.mockMvc.perform(delete(currentRefereeUrl)
			.with(AuthenticationStepDefs.authenticate()));
	}

	@Then("the referee API response status should be {int}")
	public void theRefereeApiResponseStatusShouldBe(int expectedStatus) throws Exception {
		stepDefs.result.andExpect(status().is(expectedStatus));
	}

	@Then("the referee response should contain name {string} and emailAddress {string} and phoneNumber {string} and expert {string}")
	public void theRefereeResponseShouldContain(String name, String email, String phone, String expertStr) throws Exception {
		boolean isExpert = Boolean.parseBoolean(expertStr);

		stepDefs.result.andExpect(jsonPath("$.name").value(name))
			.andExpect(jsonPath("$.emailAddress").value(email))
			.andExpect(jsonPath("$.phoneNumber").value(phone))
			.andExpect(jsonPath("$.expert").value(isExpert));
	}

	private void validateUrlIsPresent() {
		if (currentRefereeUrl == null) {
			throw new IllegalStateException("Missing currentRefereeUrl. Ensure a referee was successfully created first.");
		}
	}

	private void saveUrlFromLocationHeader() {
		MvcResult res = stepDefs.result.andReturn();
		String location = res.getResponse().getHeader("Location");
		if (location != null) {
			currentRefereeUrl = location;
		}
	}
}
