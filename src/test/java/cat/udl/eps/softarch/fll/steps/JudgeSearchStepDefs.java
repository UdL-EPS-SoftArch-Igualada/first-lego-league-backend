package cat.udl.eps.softarch.fll.steps;

import cat.udl.eps.softarch.fll.domain.Judge;
import cat.udl.eps.softarch.fll.repository.JudgeRepository;
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

public class JudgeSearchStepDefs {

	private final StepDefs stepDefs;
	private final JudgeRepository judgeRepository;

	public JudgeSearchStepDefs(StepDefs stepDefs, JudgeRepository judgeRepository) {
		this.stepDefs = stepDefs;
		this.judgeRepository = judgeRepository;
	}

	@Given("judges exist with names {string} and {string}")
	public void judgesExistWithNames(String firstJudgeName, String secondJudgeName) {
		judgeRepository.deleteAll();
		String suffix = UUID.randomUUID().toString().substring(0, 8);

		Judge firstJudge = Judge.create(firstJudgeName, "judge.search.first." + suffix + "@example.com", "111111111");
		judgeRepository.save(firstJudge);

		Judge secondJudge = Judge.create(secondJudgeName, "judge.search.second." + suffix + "@example.com", "222222222");
		judgeRepository.save(secondJudge);
	}

	@When("I search judges by name containing {string}")
	public void iSearchJudgesByNameContaining(String name) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(get("/judges/search/findByNameContaining")
				.param("name", name)
				.accept(MediaType.APPLICATION_JSON))
			.andDo(print());
	}

	@And("^the judges search response should contain (\\d+) result[s]?$")
	public void judgesSearchResponseShouldContainCount(int expectedCount) throws Exception {
		String responseBody = stepDefs.result.andReturn().getResponse().getContentAsString();
		JsonNode root = stepDefs.mapper.readTree(responseBody);
		JsonNode judges = root.path("_embedded").path("judges");
		int actualCount = judges.isArray() ? judges.size() : 0;
		assertEquals(expectedCount, actualCount);
	}

	@And("the judges search response should include judge named {string}")
	public void judgesSearchResponseShouldIncludeJudgeNamed(String judgeName) throws Exception {
		stepDefs.result.andExpect(jsonPath("$._embedded.judges[*].name", hasItem(judgeName)));
	}
}
