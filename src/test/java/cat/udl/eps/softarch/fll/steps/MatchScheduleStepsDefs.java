package cat.udl.eps.softarch.fll.steps;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import cat.udl.eps.softarch.fll.domain.CompetitionTable;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.repository.MatchRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.persistence.EntityManager;

public class MatchScheduleStepsDefs {

	private final StepDefs stepDefs;
	private final MatchRepository matchRepository;
	private final EntityManager entityManager;

	public MatchScheduleStepsDefs(StepDefs stepDefs, MatchRepository matchRepository, EntityManager entityManager) {
		this.stepDefs = stepDefs;
		this.matchRepository = matchRepository;
		this.entityManager = entityManager;
	}

	@Given("a competition table {string} exists")
	@Transactional
	public void a_competition_table_exists(String tableId) {
		CompetitionTable table = entityManager.find(CompetitionTable.class, tableId);
		if (table == null) {
			table = new CompetitionTable();
			table.setId(tableId);
			entityManager.persist(table);
		}
	}

	@Given("a valid match exists on {string} from {string} to {string}")
	@Transactional
	public void a_valid_match_exists(String tableId, String startTime, String endTime) {
		CompetitionTable table = entityManager.find(CompetitionTable.class, tableId);
		Match match = new Match();
		match.setCompetitionTable(table);
		match.setStartTime(LocalTime.parse(startTime));
		match.setEndTime(LocalTime.parse(endTime));
		matchRepository.save(match);
	}

	@When("I request to create a match on {string} from {string} to {string}")
	public void i_request_to_create_a_match(String tableId, String startTime, String endTime) throws Throwable {
		String jsonPayload = String.format(
				"{\"startTime\": \"%s\", \"endTime\": \"%s\", \"competitionTable\": \"/competitionTables/%s\"}",
				startTime, endTime, tableId
		);

		stepDefs.result = stepDefs.mockMvc.perform(post("/matches")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonPayload)
				.characterEncoding(StandardCharsets.UTF_8)
				.accept(MediaType.APPLICATION_JSON)
				.with(user("admin").roles("ADMIN")));
	}

	@Then("the match scheduling response status should be {int}")
	public void the_match_scheduling_response_status_should_be(int expectedStatus) throws Throwable {
		stepDefs.result.andExpect(status().is(expectedStatus));
	}

	@Then("the match scheduling response error should be {string}")
	public void the_match_scheduling_response_error_should_be(String expectedError) throws Throwable {
		stepDefs.result.andExpect(jsonPath("$.error").value(expectedError));
	}
}