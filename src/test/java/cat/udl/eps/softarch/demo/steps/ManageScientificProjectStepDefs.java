package cat.udl.eps.softarch.demo.steps;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import cat.udl.eps.softarch.demo.domain.ScientificProject;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ManageScientificProjectStepDefs {

	private final StepDefs stepDefs;

	public ManageScientificProjectStepDefs(StepDefs stepDefs) {
		this.stepDefs = stepDefs;
	}

	private ResultActions performCreateProject(Integer score, String comments) throws Exception {
		ScientificProject project = new ScientificProject();
		project.setScore(score);
		project.setComments(comments);

		return stepDefs.mockMvc.perform(
			post("/scientificProjects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(stepDefs.mapper.writeValueAsString(project))
				.characterEncoding(StandardCharsets.UTF_8)
				.accept(MediaType.APPLICATION_JSON)
				.with(AuthenticationStepDefs.authenticate()));
	}

	@When("I create a new scientific project with score {int} and comments {string}")
	public void iCreateScientificProject(Integer score, String comments) throws Exception {
		stepDefs.result = performCreateProject(score, comments);
	}

	@Given("There is a scientific project with score {int} and comments {string}")
	public void thereIsAScientificProject(Integer score, String comments) throws Exception {
		performCreateProject(score, comments).andExpect(status().isCreated());
	}

	@When("I search for scientific projects with minimum score {int}")
	public void iSearchScientificProjectsByMinScore(Integer minScore) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
			get("/scientificProjects/search/findByScoreGreaterThanEqual")
				.param("minScore", minScore.toString())
				.accept(MediaType.APPLICATION_JSON)
				.with(AuthenticationStepDefs.authenticate()));
	}

	@Then("The response contains {int} scientific project\\(s)")
	public void theResponseContainsNProjects(Integer count) throws Exception {
		stepDefs.result.andExpect(
			jsonPath("$._embedded.scientificProjects", hasSize(count)));
	}
}