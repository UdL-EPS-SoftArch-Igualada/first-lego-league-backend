package cat.udl.eps.softarch.fll.steps.app;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import cat.udl.eps.softarch.fll.domain.User;
import cat.udl.eps.softarch.fll.repository.identity.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class RegisterStepDefs {
	private final StepDefs stepDefs;
	private final UserRepository userRepository;

	public RegisterStepDefs(StepDefs stepDefs, UserRepository userRepository) {
		this.stepDefs = stepDefs;
		this.userRepository = userRepository;
	}

	@Given("^There is no registered user with username \"([^\"]*)\"$")
	public void thereIsNoRegisteredUserWithUsername(String user) {
		assertFalse(userRepository.existsById(user), "User \"" + user + "\"shouldn't exist");
	}

	@Given("^There is a registered user with username \"([^\"]*)\" and password \"([^\"]*)\" and email \"([^\"]*)\"$")
	public void thereIsARegisteredUserWithUsernameAndPasswordAndEmail(String username, String password, String email) {
		if (!userRepository.existsById(username)) {
			User user = new User();
			user.setEmail(email);
			user.setId(username);
			user.setPassword(password);
			user.encodePassword();
			userRepository.save(user);
		}
	}

	@Given("^There is a registered user with username \"([^\"]*)\", password \"([^\"]*)\", email \"([^\"]*)\" and roles \"([^\"]*)\"$")
	public void thereIsARegisteredUserWithUsernamePasswordEmailAndRoles(
			String username,
			String password,
			String email,
			String roles) {
		if (!userRepository.existsById(username)) {
			User user = new User();
			user.setEmail(email);
			user.setId(username);
			user.setPassword(password);
			user.setRoles(roles);
			user.encodePassword();
			userRepository.save(user);
		}
	}


	@And("^I can login with username \"([^\"]*)\" and password \"([^\"]*)\"$")
	public void iCanLoginWithUsernameAndPassword(String username, String password) throws Throwable {
		AuthenticationStepDefs.currentUsername = username;
		AuthenticationStepDefs.currentPassword = password;

		stepDefs.result = stepDefs.mockMvc.perform(
				get("/identity", username)
					.accept(MediaType.APPLICATION_JSON)
					.with(AuthenticationStepDefs.authenticate()))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@And("^I cannot login with username \"([^\"]*)\" and password \"([^\"]*)\"$")
	public void iCannotLoginWithUsernameAndPassword(String username, String password) throws Throwable {
		AuthenticationStepDefs.currentUsername = username;
		AuthenticationStepDefs.currentPassword = password;

		stepDefs.result = stepDefs.mockMvc.perform(
				get("/identity", username)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@When("^I register a new user with username \"([^\"]*)\", email \"([^\"]*)\" and password \"([^\"]*)\"$")
	public void iRegisterANewUserWithUsernameEmailAndPassword(String username, String email, String password) throws Throwable {
		User user = new User();
		user.setId(username);
		user.setEmail(email);

		stepDefs.result = stepDefs.mockMvc.perform(
				post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(new JSONObject(
								stepDefs.mapper.writeValueAsString(user)).put("password", password).toString())
						.characterEncoding(StandardCharsets.UTF_8)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}

	@And("^It has been created a user with username \"([^\"]*)\" and email \"([^\"]*)\", the password is not returned$")
	public void itHasBeenCreatedAUserWithUsername(String username, String email) throws Throwable {
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/users/{username}", username)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print())
				.andExpect(jsonPath("$.email", is(email)))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@And("^It has not been created a user with username \"([^\"]*)\"$")
	public void itHasNotBeenCreatedAUserWithUsername(String username) throws Throwable {
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/users/{username}", username)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andExpect(status().isNotFound());
	}
}
