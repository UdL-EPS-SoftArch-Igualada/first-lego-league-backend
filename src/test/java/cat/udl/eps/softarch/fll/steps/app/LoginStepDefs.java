package cat.udl.eps.softarch.fll.steps.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import cat.udl.eps.softarch.fll.steps.StepDefs;
import org.springframework.http.MediaType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;

public class LoginStepDefs {
	private final StepDefs stepDefs;

	public LoginStepDefs(StepDefs stepDefs) {
		this.stepDefs = stepDefs;
	}

	@When("I check my identity")
	public void iCheckMyIdentity() throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/identity")
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}

	@And("The identity username is {string}")
	public void theIdentityUsernameIs(String username) throws Exception {
		stepDefs.result.andExpect(jsonPath("$._links.self.href", org.hamcrest.Matchers.endsWith("/" + username)));
	}
}
