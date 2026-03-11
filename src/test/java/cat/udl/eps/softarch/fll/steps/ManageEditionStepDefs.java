package cat.udl.eps.softarch.fll.steps;

import cat.udl.eps.softarch.fll.domain.Edition;
import cat.udl.eps.softarch.fll.repository.EditionRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ManageEditionStepDefs {
	private final StepDefs stepDefs;
	private final EditionRepository editionRepository;
	String editionUri;

	public ManageEditionStepDefs(StepDefs stepDefs, EditionRepository editionRepository) {
		this.stepDefs = stepDefs;
		this.editionRepository = editionRepository;
	}

	@When("I create a new edition with year {int}, venue {string} and description {string}")
	public void iCreateANewEdition(int year, String venue, String description) throws Exception {
		Edition edition = Edition.create(year, venue, description);

		stepDefs.result = stepDefs.mockMvc.perform(
				post("/editions")
					.contentType(MediaType.APPLICATION_JSON)
					.content(stepDefs.mapper.writeValueAsString(edition))
					.characterEncoding(StandardCharsets.UTF_8)
					.accept(MediaType.APPLICATION_JSON)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());

		editionUri = stepDefs.result.andReturn().getResponse().getHeader("Location");
	}

	@And("The edition has year {int}, venue {string} and description {string}")
	public void theEditionHasFields(int year, String venue, String description) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get(editionUri)
					.accept(MediaType.APPLICATION_JSON)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print())
			.andExpect(jsonPath("$.year", is(year)))
			.andExpect(jsonPath("$.venueName", is(venue)))
			.andExpect(jsonPath("$.description", is(description)));
	}

	@Given("There is an edition with year {int}, venue {string} and description {string}")
	public void thereIsAnEdition(int year, String venue, String description) {
		Edition edition = Edition.create(year, venue, description);
		edition = editionRepository.save(edition);
		editionUri = "/editions/" + edition.getId();
	}

	@When("I retrieve the edition")
	public void iRetrieveTheEdition() throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get(editionUri)
					.accept(MediaType.APPLICATION_JSON)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@When("I update the edition venue to {string}")
	public void iUpdateTheEditionVenue(String venue) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				patch(editionUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(stepDefs.mapper.writeValueAsString(Map.of("venueName", venue)))
					.characterEncoding(StandardCharsets.UTF_8)
					.accept(MediaType.APPLICATION_JSON)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@And("The edition has venue {string}")
	public void theEditionHasVenue(String venue) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get(editionUri)
					.accept(MediaType.APPLICATION_JSON)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print())
			.andExpect(jsonPath("$.venueName", is(venue)));
	}

	@When("I delete the edition")
	public void iDeleteTheEdition() throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				delete(editionUri)
					.accept(MediaType.APPLICATION_JSON)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@And("The edition has been deleted")
	public void theEditionHasBeenDeleted() throws Exception {
		stepDefs.mockMvc.perform(
				get(editionUri)
					.accept(MediaType.APPLICATION_JSON)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print())
			.andExpect(status().isNotFound());
	}
}
