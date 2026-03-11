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
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.MatchRepository;
import cat.udl.eps.softarch.fll.repository.RoundRepository;
import cat.udl.eps.softarch.fll.repository.CompetitionTableRepository;
import cat.udl.eps.softarch.fll.repository.TeamRepository;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;

public class MatchSearchStepDefs {

	private StepDefs stepDefs;
	private MatchRepository matchRepository;
	private RoundRepository roundRepository;
	private CompetitionTableRepository tableRepository;
	private TeamRepository teamRepository;

	private Long currentRoundId;

	public MatchSearchStepDefs(StepDefs stepDefs,
							   MatchRepository matchRepository,
							   RoundRepository roundRepository,
							   CompetitionTableRepository tableRepository,
							   TeamRepository teamRepository) {
		this.stepDefs = stepDefs;
		this.matchRepository = matchRepository;
		this.roundRepository = roundRepository;
		this.tableRepository = tableRepository;
		this.teamRepository = teamRepository;
	}

	@Before("@MatchSearch")
	public void setup() {
		matchRepository.deleteAll();
		roundRepository.deleteAll();
		tableRepository.deleteAll();
		stepDefs.result = null;
	}

	@Before("@FindByTeam")
	public void setupFindByTeam() {
		matchRepository.deleteAll();
		teamRepository.deleteAll();
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

	@Given("a team {string} exists with matches")
	public void aTeamExistsWithMatches(String teamName) {
		Team team = new Team();
		team.setName(teamName);
		team.setCity("Barcelona");
		team.setFoundationYear(2010);
		team.setCategory("Challenge");
		teamRepository.save(team);

		CompetitionTable table = new CompetitionTable();
		table.setId("Table-FindByTeam");
		table = tableRepository.save(table);

		Round round = new Round();
		round.setNumber(99);
		round = roundRepository.save(round);
		currentRoundId = round.getId();

		Match match = new Match();
		match.setStartTime(LocalTime.of(10, 0));
		match.setEndTime(LocalTime.of(10, 30));
		match.setTeamA(team);
		match.setCompetitionTable(table);
		match.setRound(round);
		matchRepository.save(match);
	}

	@Given("a team {string} exists with no matches")
	public void aTeamExistsWithNoMatches(String teamName) {
		Team team = new Team();
		team.setName(teamName);
		team.setCity("Lleida");
		team.setFoundationYear(2012);
		team.setCategory("Challenge");
		teamRepository.save(team);
	}

	@When("I search matches for team {string}")
	public void iSearchMatchesForTeam(String teamUri) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
			get("/matches/search/findByTeam")
				.param("team", teamUri)
				.with(user("admin"))
				.contentType(MediaType.APPLICATION_JSON)
		);
	}

	@Then("the team matches response should contain matches for {string}")
	public void theTeamMatchesResponseShouldContainMatchesFor(String teamName) throws Exception {
		stepDefs.result
			.andExpect(jsonPath("$._embedded.matches").isArray())
			.andExpect(jsonPath("$._embedded.matches[0]").exists());
	}

	@Then("the team matches response should be empty")
	public void theTeamMatchesResponseShouldBeEmpty() throws Exception {
		stepDefs.result
			.andExpect(jsonPath("$._embedded.matches").isArray())
			.andExpect(jsonPath("$._embedded.matches").isEmpty());
	}

	@Then("the team matches error should be {string}")
	public void theTeamMatchesErrorShouldBe(String errorCode) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.error").value(errorCode));
	}
}
