package cat.udl.eps.softarch.fll.steps.edition;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import cat.udl.eps.softarch.fll.steps.ManageEditionStepDefs;
import cat.udl.eps.softarch.fll.steps.app.AuthenticationStepDefs;
import cat.udl.eps.softarch.fll.steps.app.StepDefs;
import org.springframework.http.MediaType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;

public class EditionLifecycleStepDefs {

	private final StepDefs stepDefs;
	private final ManageEditionStepDefs manageEditionStepDefs;

	public EditionLifecycleStepDefs(StepDefs stepDefs, ManageEditionStepDefs manageEditionStepDefs) {
		this.stepDefs = stepDefs;
		this.manageEditionStepDefs = manageEditionStepDefs;
	}

	@When("I change the current edition state to {string}")
	public void iChangeTheCurrentEditionStateTo(String state) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				patch(editionUri() + "/state")
						.contentType(MediaType.APPLICATION_JSON)
						.content(stepDefs.mapper.writeValueAsString(Map.of("state", state)))
						.characterEncoding(StandardCharsets.UTF_8)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}

	@When("I change edition with id {long} state to {string}")
	public void iChangeEditionWithIdStateTo(Long editionId, String state) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				patch("/editions/" + editionId + "/state")
						.contentType(MediaType.APPLICATION_JSON)
						.content(stepDefs.mapper.writeValueAsString(Map.of("state", state)))
						.characterEncoding(StandardCharsets.UTF_8)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}

	@And("The current edition state is changed to {string}")
	public void currentEditionStateIsChangedTo(String state) throws Exception {
		iChangeTheCurrentEditionStateTo(state);
		stepDefs.result.andExpect(jsonPath("$.status", is("UPDATED")));
	}

	@And("The edition transition response has previous state {string} and new state {string}")
	public void editionTransitionResponseHasPreviousAndNewState(String previousState, String newState) throws Exception {
		stepDefs.result
				.andExpect(jsonPath("$.previousState", is(previousState)))
				.andExpect(jsonPath("$.newState", is(newState)));
	}

	@And("The edition transition response status is {string}")
	public void editionTransitionResponseStatusIs(String status) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.status", is(status)));
	}

	private String editionUri() {
		return manageEditionStepDefs.editionUri;
	}
}
