package cat.udl.eps.softarch.fll.steps.match;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import cat.udl.eps.softarch.fll.domain.match.Match;
import cat.udl.eps.softarch.fll.domain.match.MatchState;
import cat.udl.eps.softarch.fll.repository.match.MatchRepository;
import cat.udl.eps.softarch.fll.steps.app.AuthenticationStepDefs;
import cat.udl.eps.softarch.fll.steps.app.StepDefs;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.http.MediaType;

public class MatchLifecycleStepDefs {

	private final StepDefs stepDefs;
	private final MatchRepository matchRepository;

	private Long targetMatchId;

	public MatchLifecycleStepDefs(StepDefs stepDefs, MatchRepository matchRepository) {
		this.stepDefs = stepDefs;
		this.matchRepository = matchRepository;
	}

	@Given("There is a match in state {string}")
	public void thereIsAMatchInState(String state) {
		matchRepository.deleteAll();
		Match match = new Match();
		match.setState(MatchState.valueOf(state));
		targetMatchId = matchRepository.save(match).getId();
	}

	@When("I change the match state to {string}")
	public void iChangeTheMatchStateTo(String state) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				patch("/matches/{id}/state", targetMatchId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(stepDefs.mapper.writeValueAsString(Map.of("state", state)))
					.characterEncoding(StandardCharsets.UTF_8)
					.accept(MediaType.APPLICATION_JSON)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@When("I change match with id {long} state to {string}")
	public void iChangeMatchWithIdStateTo(Long matchId, String state) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				patch("/matches/{id}/state", matchId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(stepDefs.mapper.writeValueAsString(Map.of("state", state)))
					.characterEncoding(StandardCharsets.UTF_8)
					.accept(MediaType.APPLICATION_JSON)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@And("The match transition response has previous state {string} and new state {string}")
	public void matchTransitionResponseHasPreviousAndNewState(String previousState, String newState) throws Exception {
		stepDefs.result
			.andExpect(jsonPath("$.previousState", is(previousState)))
			.andExpect(jsonPath("$.newState", is(newState)));
	}
}