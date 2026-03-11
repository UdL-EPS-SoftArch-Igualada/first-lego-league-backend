package cat.udl.eps.softarch.fll.steps;

import cat.udl.eps.softarch.fll.domain.Referee;
import cat.udl.eps.softarch.fll.repository.RefereeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import java.util.UUID;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class RefereeSearchStepDefs {

	private final StepDefs stepDefs;
	private final RefereeRepository refereeRepository;

	public RefereeSearchStepDefs(StepDefs stepDefs, RefereeRepository refereeRepository) {
		this.stepDefs = stepDefs;
		this.refereeRepository = refereeRepository;
	}

	@Given("referees exist with names {string} and {string}")
	public void refereesExistWithNames(String firstRefereeName, String secondRefereeName) {
		refereeRepository.deleteAll();
		String suffix = UUID.randomUUID().toString().substring(0, 8);

		Referee firstReferee = Referee.create(firstRefereeName, "referee.search.first." + suffix + "@example.com", "333333333");
		refereeRepository.save(firstReferee);

		Referee secondReferee = Referee.create(secondRefereeName, "referee.search.second." + suffix + "@example.com", "444444444");
		refereeRepository.save(secondReferee);
	}

	@When("I search referees by name containing {string}")
	public void iSearchRefereesByNameContaining(String name) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(get("/referees/search/findByNameContaining")
				.param("name", name)
				.accept(MediaType.APPLICATION_JSON))
			.andDo(print());
	}

	@And("^the referees search response should contain (\\d+) result[s]?$")
	public void refereesSearchResponseShouldContainCount(int expectedCount) throws Exception {
		String responseBody = stepDefs.result.andReturn().getResponse().getContentAsString();
		JsonNode root = stepDefs.mapper.readTree(responseBody);
		JsonNode referees = root.path("_embedded").path("referees");
		int actualCount = referees.isArray() ? referees.size() : 0;
		assertEquals(expectedCount, actualCount);
	}

	@And("the referees search response should include referee named {string}")
	public void refereesSearchResponseShouldIncludeRefereeNamed(String refereeName) throws Exception {
		stepDefs.result.andExpect(jsonPath("$._embedded.referees[*].name", hasItem(refereeName)));
	}
}
