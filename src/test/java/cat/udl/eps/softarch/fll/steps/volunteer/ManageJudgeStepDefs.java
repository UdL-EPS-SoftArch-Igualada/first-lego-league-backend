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

import cat.udl.eps.softarch.fll.steps.app.AuthenticationStepDefs;
import cat.udl.eps.softarch.fll.steps.app.StepDefs;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ManageJudgeStepDefs {

	private final StepDefs stepDefs;
	private String currentJudgeUrl;

	public ManageJudgeStepDefs(StepDefs stepDefs) {
		this.stepDefs = stepDefs;
	}

	@When("I request to create a judge with name {string} and emailAddress {string} and phoneNumber {string} and expert {string}")
	public void i_request_to_create_a_judge(String name, String email, String phone, String expertStr) throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("name", name);
		body.put("emailAddress", email);
		body.put("phoneNumber", phone);
		body.put("expert", Boolean.parseBoolean(expertStr));

		stepDefs.result = stepDefs.mockMvc.perform(post("/judges")
				.contentType(MediaType.APPLICATION_JSON)
				.content(stepDefs.mapper.writeValueAsString(body))
				.characterEncoding(StandardCharsets.UTF_8)
				.with(AuthenticationStepDefs.authenticate()));
		
		saveUrlFromLocationHeader();
	}

	@Given("a judge exists with name {string} and emailAddress {string} and phoneNumber {string} and expert {string}")
	public void a_judge_exists(String name, String email, String phone, String expertStr) throws Exception {
		i_request_to_create_a_judge(name, email, phone, expertStr);
		stepDefs.result.andExpect(status().isCreated());
		assertNotNull(currentJudgeUrl, "Judge URL (Location header) should not be null after creation");
	}

	@When("I request to retrieve that judge")
	public void i_request_to_retrieve_that_judge() throws Exception {
		validateUrlIsPresent();
		stepDefs.result = stepDefs.mockMvc.perform(get(currentJudgeUrl)
				.with(AuthenticationStepDefs.authenticate()));
	}

	@When("I request to update the judge name to {string}")
	public void i_request_to_update_the_judge_name_to(String newName) throws Exception {
		validateUrlIsPresent();
		Map<String, Object> body = new HashMap<>();
		body.put("name", newName);
		
		stepDefs.result = stepDefs.mockMvc.perform(patch(currentJudgeUrl)
				.contentType(MediaType.APPLICATION_JSON)
				.content(stepDefs.mapper.writeValueAsString(body))
				.characterEncoding(StandardCharsets.UTF_8)
				.with(AuthenticationStepDefs.authenticate()));
	}

	@When("I request to delete that judge")
	public void i_request_to_delete_that_judge() throws Exception {
		validateUrlIsPresent();
		stepDefs.result = stepDefs.mockMvc.perform(delete(currentJudgeUrl)
				.with(AuthenticationStepDefs.authenticate()));
	}

	@Then("the judge API response status should be {int}")
	public void the_judge_api_response_status_should_be(int expectedStatus) throws Exception {
		stepDefs.result.andExpect(status().is(expectedStatus));
	}

	@Then("the response should contain name {string} and emailAddress {string} and phoneNumber {string} and expert {string}")
	public void the_response_should_contain(String name, String email, String phone, String expertStr) throws Exception {
		boolean isExpert = Boolean.parseBoolean(expertStr);
		
		stepDefs.result.andExpect(jsonPath("$.name").value(name))
					   .andExpect(jsonPath("$.emailAddress").value(email))
					   .andExpect(jsonPath("$.phoneNumber").value(phone))
					   .andExpect(jsonPath("$.expert").value(isExpert));
	}

	private void validateUrlIsPresent() {
		if (currentJudgeUrl == null) {
			throw new IllegalStateException("Missing currentJudgeUrl. Ensure a judge was successfully created first.");
		}
	}

	private void saveUrlFromLocationHeader() {
		MvcResult res = stepDefs.result.andReturn();
		String location = res.getResponse().getHeader("Location");
		if (location != null) {
			currentJudgeUrl = location;
		}
	}
}
