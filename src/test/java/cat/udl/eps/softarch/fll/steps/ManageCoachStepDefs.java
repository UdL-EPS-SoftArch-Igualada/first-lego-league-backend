package cat.udl.eps.softarch.fll.steps;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import java.util.HashMap;
import java.util.Map;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.http.MediaType;

public class ManageCoachStepDefs {
	private final StepDefs stepDefs;
	private String lastCoachUri;

	public ManageCoachStepDefs(StepDefs stepDefs) {
		this.stepDefs = stepDefs;
	}

	@When("I create a new coach with name {string}, email {string} and phone {string}")
	public void iCreateACoach(String name, String email, String phone) throws Exception {
		Map<String, String> payload = new HashMap<>();
		payload.put("name", name);
		payload.put("emailAddress", email);
		payload.put("phoneNumber", phone);

		stepDefs.result = stepDefs.mockMvc.perform(post("/coaches")
				.contentType(MediaType.APPLICATION_JSON)
				.content(stepDefs.mapper.writeValueAsString(payload))
				.with(AuthenticationStepDefs.authenticate()));

		lastCoachUri = stepDefs.result.andReturn().getResponse().getHeader("Location");
	}

	@Given("There is a coach with name {string} and email {string}")
	public void thereIsACoach(String name, String email) throws Exception {
		iCreateACoach(name, email, "123456789");
	}

	@When("I retrieve the coach with email {string}")
	public void iRetrieveCoach(String email) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(get("/coaches/search/findByEmailAddress?email={email}", email)
				.with(AuthenticationStepDefs.authenticate()));
	}

	@Then("The coach name is {string}")
	public void verifyCoachName(String name) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.name", is(name)));
	}

	@When("I update the coach {string} with new phone {string}")
	public void iUpdateCoach(String email, String newPhone) throws Exception {
		Map<String, String> payload = new HashMap<>();
		payload.put("phoneNumber", newPhone);

		stepDefs.result = stepDefs.mockMvc.perform(patch(lastCoachUri)
				.contentType(MediaType.APPLICATION_JSON)
				.content(stepDefs.mapper.writeValueAsString(payload))
				.with(AuthenticationStepDefs.authenticate()));
	}

	@And("The coach {string} has phone {string}")
	public void verifyCoachPhone(String email, String phone) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.phoneNumber", is(phone)));
	}

	@When("I delete the coach with email {string}")
	public void iDeleteCoach(String email) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(delete(lastCoachUri)
				.with(AuthenticationStepDefs.authenticate()));
	}
}
