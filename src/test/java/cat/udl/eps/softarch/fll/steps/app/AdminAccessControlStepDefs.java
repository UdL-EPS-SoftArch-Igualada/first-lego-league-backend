package cat.udl.eps.softarch.fll.steps.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import cat.udl.eps.softarch.fll.steps.StepDefs;
import org.springframework.http.MediaType;
import io.cucumber.java.en.When;

public class AdminAccessControlStepDefs {
	private final StepDefs stepDefs;

	public AdminAccessControlStepDefs(StepDefs stepDefs) {
		this.stepDefs = stepDefs;
	}

	@When("I retrieve the editions list")
	public void iRetrieveTheEditionsList() throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/editions")
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}
}
