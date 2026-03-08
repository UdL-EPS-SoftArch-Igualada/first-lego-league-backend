package cat.udl.eps.softarch.fll.steps;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.time.LocalTime;
import org.springframework.http.MediaType;
import cat.udl.eps.softarch.fll.domain.CompetitionTable;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.Round;
import cat.udl.eps.softarch.fll.repository.MatchRepository;
import cat.udl.eps.softarch.fll.repository.RoundRepository;
import cat.udl.eps.softarch.fll.repository.CompetitionTableRepository;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;

public class MatchSearchStepDefs {

	private StepDefs stepDefs;
	private MatchRepository matchRepository;
	private RoundRepository roundRepository;
	private CompetitionTableRepository tableRepository;

	private Long currentRoundId;

	public MatchSearchStepDefs(StepDefs stepDefs,
							   MatchRepository matchRepository,
							   RoundRepository roundRepository,
							   CompetitionTableRepository tableRepository) {
		this.stepDefs = stepDefs;
		this.matchRepository = matchRepository;
		this.roundRepository = roundRepository;
		this.tableRepository = tableRepository;
	}

	@Before("@MatchSearch")
	public void setup() {
		matchRepository.deleteAll();
		roundRepository.deleteAll();
		tableRepository.deleteAll();
		stepDefs.result = null;
	}

	@Given("the database contains matches for search")
	public void theDatabaseContainsMatchesForSearch() {

		CompetitionTable table = new CompetitionTable();
		table.setId("Table-01");
		table = tableRepository.save(table);

		Round round = new Round();
		round = roundRepository.save(round);
		currentRoundId = round.getId();

		Match match1 = new Match();
		match1.setStartTime(LocalTime.of(10, 0));
		match1.setEndTime(LocalTime.of(11, 0));
		match1.setCompetitionTable(table);
		match1.setRound(round);

		Match match2 = new Match();
		match2.setStartTime(LocalTime.of(11, 15));
		match2.setEndTime(LocalTime.of(12, 0));
		match2.setCompetitionTable(table);
		match2.setRound(round);

		matchRepository.save(match1);
		matchRepository.save(match2);
	}

	@When("I search matches with no filters")
	public void iSearchMatchesWithNoFilters() throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
			get("/matches/filter")
				.param("page", "0")
				.param("sort", "startTime,asc")
				.param("sort", "id,asc")
				.with(user("admin"))
				.contentType(MediaType.APPLICATION_JSON)
		);
	}

	@When("I search matches with table {string} and round {int}")
	public void iSearchMatchesWithTableAndRound(String tableId, Integer roundNumber) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
			get("/matches/filter")
				.param("tableId", tableId)
				.param("roundId", String.valueOf(currentRoundId))
				.with(user("admin"))
				.contentType(MediaType.APPLICATION_JSON)
		);
	}

	@When("I search matches between {string} and {string}")
	public void iSearchMatchesBetween(String start, String end) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
			get("/matches/filter")
				.param("startFrom", start)
				.param("endTo", end)
				.with(user("admin"))
				.contentType(MediaType.APPLICATION_JSON)
		);
	}

	@When("I search matches with table {string} between {string} and {string}")
	public void iSearchMatchesWithTableBetween(String tableId, String start, String end) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
			get("/matches/filter")
				.param("tableId", tableId)
				.param("startFrom", start)
				.param("endTo", end)
				.with(user("admin"))
				.contentType(MediaType.APPLICATION_JSON)
		);
	}



	@Then("the match response status should be {int}")
	public void theResponseStatusShouldBe(Integer statusCode) throws Exception {
		stepDefs.result.andExpect(status().is(statusCode));
	}

	@Then("the response should contain matches")
	public void theResponseShouldContainMatchesInOrder() throws Exception {
		stepDefs.result
			.andExpect(jsonPath("$.items[0].startTime").value("10:00:00"))
			.andExpect(jsonPath("$.items[1].startTime").value("11:15:00"))
			.andExpect(jsonPath("$.page").value(0))
			.andExpect(jsonPath("$.totalElements").value(2));
	}

	@Then("the error code should be \"INVALID_TIME_FILTER_RANGE\"")
	public void theErrorCodeShouldBeInvalidTimeFilterRange() throws Exception {
		stepDefs.result.andExpect(status().isUnprocessableEntity());
		stepDefs.result.andExpect(jsonPath("$.errorCode").value("INVALID_TIME_FILTER_RANGE"));
	}
}