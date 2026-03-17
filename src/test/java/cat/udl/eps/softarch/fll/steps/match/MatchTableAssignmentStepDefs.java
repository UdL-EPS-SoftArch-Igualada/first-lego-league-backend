package cat.udl.eps.softarch.fll.steps.match;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import java.time.LocalTime;

import cat.udl.eps.softarch.fll.steps.app.AuthenticationStepDefs;
import cat.udl.eps.softarch.fll.steps.app.StepDefs;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import cat.udl.eps.softarch.fll.domain.CompetitionTable;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.repository.CompetitionTableRepository;
import cat.udl.eps.softarch.fll.repository.MatchRepository;
import cat.udl.eps.softarch.fll.repository.MatchResultRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class MatchTableAssignmentStepDefs {

	private final StepDefs stepDefs;
	private final MatchRepository matchRepository;
	private final CompetitionTableRepository competitionTableRepository;
	private final MatchResultRepository matchResultRepository;

	private Match currentMatch;

	public MatchTableAssignmentStepDefs(
			StepDefs stepDefs,
			MatchRepository matchRepository,
			CompetitionTableRepository competitionTableRepository,
			MatchResultRepository matchResultRepository) {
		this.stepDefs = stepDefs;
		this.matchRepository = matchRepository;
		this.competitionTableRepository = competitionTableRepository;
		this.matchResultRepository = matchResultRepository;
	}

	@Given("the match table assignment system is empty")
	public void clearMatchTableAssignmentSystem() {
		matchResultRepository.deleteAll();
		matchRepository.deleteAll();
		competitionTableRepository.deleteAll();
		currentMatch = null;
	}

	@Given("a scheduled match without table exists from {string} to {string}")
	public void createScheduledMatchWithoutTable(String startTime, String endTime) {
		Match match = new Match();
		match.setStartTime(LocalTime.parse(startTime));
		match.setEndTime(LocalTime.parse(endTime));
		currentMatch = matchRepository.save(match);
	}

	@Given("a competition table with identifier {string} exists")
	public void createCompetitionTable(String tableIdentifier) {
		CompetitionTable table = new CompetitionTable();
		table.setId(tableIdentifier);
		competitionTableRepository.save(table);
	}

	@Given("a scheduled match assigned to table {string} exists from {string} to {string}")
	public void createScheduledMatchAssignedToTable(String tableIdentifier, String startTime, String endTime) {
		createScheduledMatchAssignedToTableInternal(tableIdentifier, startTime, endTime, false);
	}

	@Given("a target scheduled match assigned to table {string} exists from {string} to {string}")
	public void createTargetScheduledMatchAssignedToTable(String tableIdentifier, String startTime, String endTime) {
		createScheduledMatchAssignedToTableInternal(tableIdentifier, startTime, endTime, true);
	}

	private void createScheduledMatchAssignedToTableInternal(
			String tableIdentifier,
			String startTime,
			String endTime,
			boolean setAsCurrent) {
		CompetitionTable table = competitionTableRepository.findById(tableIdentifier).orElseThrow();
		Match match = new Match();
		match.setStartTime(LocalTime.parse(startTime));
		match.setEndTime(LocalTime.parse(endTime));
		match.setCompetitionTable(table);
		Match saved = matchRepository.save(match);
		if (setAsCurrent) {
			currentMatch = saved;
		}
	}

	@When("I assign that match to table identifier {string}")
	public void assignCurrentMatchToTable(String tableIdentifier) throws Exception {
		if (currentMatch == null) {
			throw new IllegalStateException("No current match set. Ensure a match creation step runs first.");
		}
		assignMatchToTable(currentMatch.getId(), tableIdentifier);
	}

	@When("I assign non existing match id {string} to table identifier {string}")
	public void assignNonExistingMatchToTable(String matchId, String tableIdentifier) throws Exception {
		assignMatchToTable(Long.valueOf(matchId), tableIdentifier);
	}

	private void assignMatchToTable(Long matchId, String tableIdentifier) throws Exception {
		JSONObject request = new JSONObject();
		request.put("tableIdentifier", tableIdentifier);

		stepDefs.result = stepDefs.mockMvc.perform(
				post("/matches/" + matchId + "/assign-table")
						.with(AuthenticationStepDefs.authenticate())
						.contentType(MediaType.APPLICATION_JSON)
						.content(request.toString())
						.accept(MediaType.APPLICATION_JSON));
	}

	@And("the assigned table identifier is {string}")
	public void verifyAssignedTableIdentifier(String expectedIdentifier) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.tableIdentifier").value(expectedIdentifier));
	}
}
