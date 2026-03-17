package cat.udl.eps.softarch.fll.steps.app;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.nio.charset.StandardCharsets;

import cat.udl.eps.softarch.fll.steps.StepDefs;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import cat.udl.eps.softarch.fll.domain.Administrator;
import cat.udl.eps.softarch.fll.repository.AdministratorRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class ManageAdministratorStepDefs {
	private final StepDefs stepDefs;
	private final AdministratorRepository administratorRepository;

	public ManageAdministratorStepDefs(StepDefs stepDefs, AdministratorRepository administratorRepository) {
		this.stepDefs = stepDefs;
		this.administratorRepository = administratorRepository;
	}

	@When("I create a new administrator with username {string}, email {string} and password {string}")
	public void iCreateANewAdministrator(String username, String email, String password) throws Exception {
		Administrator admin = new Administrator();
		admin.setId(username);
		admin.setEmail(email);

		stepDefs.result = stepDefs.mockMvc.perform(
				post("/administrators")
						.contentType(MediaType.APPLICATION_JSON)
						.content(new JSONObject(
								stepDefs.mapper.writeValueAsString(admin)).put("password", password).toString())
						.characterEncoding(StandardCharsets.UTF_8)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()));
	}

	@When("I retrieve the administrator with username {string}")
	public void iRetrieveTheAdministrator(String username) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/administrators/{username}", username)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()));
	}

	@When("I delete the administrator with username {string}")
	public void iDeleteTheAdministrator(String username) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				delete("/administrators/{username}", username)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()));
	}

	@And("It has been created an administrator with username {string} and email {string}")
	public void itHasBeenCreatedAnAdministrator(String username, String email) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/administrators/{username}", username)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andExpect(jsonPath("$.email", is(email)))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@And("The retrieved administrator has email {string}")
	public void theRetrievedAdministratorHasEmail(String email) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.email", is(email)));
	}

	@And("It has not been created an administrator with username {string}")
	public void itHasNotBeenCreatedAnAdministrator(String username) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/administrators/{username}", username)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andExpect(status().isNotFound());
	}

	@Given("There is an administrator with username {string} and password {string} and email {string}")
	public void thereIsAnAdministrator(String username, String password, String email) {
		Administrator admin = administratorRepository.findById(username).orElseGet(Administrator::new);
		admin.setId(username);
		admin.setEmail(email);
		admin.setPassword(password);
		admin.encodePassword();
		administratorRepository.save(admin);
	}

	@When("I update the administrator with username {string} with new email {string}")
	public void iUpdateTheAdministratorWithUsernameWithNewEmail(String username, String newEmail) throws Exception {
		JSONObject payload = new JSONObject();
		payload.put("email", newEmail);

		stepDefs.result = stepDefs.mockMvc.perform(
				patch("/administrators/{username}", username)
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload.toString())
						.characterEncoding(StandardCharsets.UTF_8)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()));
	}

	@io.cucumber.java.en.Then("It has been updated an administrator with username {string} and email {string}")
	public void itHasBeenUpdatedAnAdministratorWithUsernameAndEmail(String username, String expectedEmail) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/administrators/{username}", username)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email", is(expectedEmail)));
	}
}
