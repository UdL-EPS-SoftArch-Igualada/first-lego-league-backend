package cat.udl.eps.softarch.fll.steps.edition;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import java.time.LocalTime;
import java.util.UUID;

import cat.udl.eps.softarch.fll.steps.app.AuthenticationStepDefs;
import cat.udl.eps.softarch.fll.steps.app.StepDefs;
import org.springframework.http.MediaType;
import cat.udl.eps.softarch.fll.domain.CompetitionTable;
import cat.udl.eps.softarch.fll.domain.Edition;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.MatchState;
import cat.udl.eps.softarch.fll.domain.Round;
import cat.udl.eps.softarch.fll.repository.CompetitionTableRepository;
import cat.udl.eps.softarch.fll.repository.EditionRepository;
import cat.udl.eps.softarch.fll.repository.MatchRepository;
import cat.udl.eps.softarch.fll.repository.RoundRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class EditionCompetitionTableStepDefs {

	private final StepDefs stepDefs;
	private final EditionRepository editionRepository;
	private final RoundRepository roundRepository;
	private final MatchRepository matchRepository;
	private final CompetitionTableRepository competitionTableRepository;

	private Long targetEditionId;
	private String targetEditionTableIdentifier;
	private String otherEditionTableIdentifier;

	public EditionCompetitionTableStepDefs(
		StepDefs stepDefs,
		EditionRepository editionRepository,
		RoundRepository roundRepository,
		MatchRepository matchRepository,
		CompetitionTableRepository competitionTableRepository) {
		this.stepDefs = stepDefs;
		this.editionRepository = editionRepository;
		this.roundRepository = roundRepository;
		this.matchRepository = matchRepository;
		this.competitionTableRepository = competitionTableRepository;
	}

	@Given("an edition competition table dataset exists")
	public void anEditionCompetitionTableDatasetExists() {
		matchRepository.deleteAll();
		roundRepository.deleteAll();
		competitionTableRepository.deleteAll();
		editionRepository.deleteAll();

		Edition targetEdition = createEdition("Target tables");
		Edition otherEdition = createEdition("Other tables");
		targetEditionId = targetEdition.getId();

		Round targetRound = createRound(targetEdition, 1001);
		Round otherRound = createRound(otherEdition, 2001);

		CompetitionTable targetTable = createTable("Table-A-" + shortSuffix());
		CompetitionTable otherTable = createTable("Table-B-" + shortSuffix());
		targetEditionTableIdentifier = targetTable.getId();
		otherEditionTableIdentifier = otherTable.getId();

		createMatch(targetRound, targetTable, "11:00", "11:20");
		createMatch(targetRound, targetTable, "11:30", "11:50");
		createMatch(otherRound, otherTable, "12:00", "12:20");
	}

	@Given("an edition without competition tables exists")
	public void anEditionWithoutCompetitionTablesExists() {
		matchRepository.deleteAll();
		roundRepository.deleteAll();
		competitionTableRepository.deleteAll();
		editionRepository.deleteAll();

		Edition edition = createEdition("Empty tables");
		targetEditionId = edition.getId();
		createRound(edition, 3001);
	}

	@Given("an edition competition table dataset with non-scheduled matches exists")
	public void anEditionCompetitionTableDatasetWithNonScheduledMatchesExists() {
		matchRepository.deleteAll();
		roundRepository.deleteAll();
		competitionTableRepository.deleteAll();
		editionRepository.deleteAll();

		Edition targetEdition = createEdition("Target tables");
		targetEditionId = targetEdition.getId();

		Round targetRound = createRound(targetEdition, 1001);
		CompetitionTable scheduledTable = createTable("Table-A-" + shortSuffix());
		CompetitionTable nonScheduledTable = createTable("Table-C-" + shortSuffix());
		targetEditionTableIdentifier = scheduledTable.getId();
		otherEditionTableIdentifier = nonScheduledTable.getId();

		createMatch(targetRound, scheduledTable, "11:00", "11:20");
		createMatch(targetRound, nonScheduledTable, "12:00", "12:20", MatchState.FINISHED);
		createMatch(targetRound, scheduledTable, "12:30", "12:50", MatchState.IN_PROGRESS);
	}

	@When("I request competition tables for the target edition")
	public void iRequestCompetitionTablesForTheTargetEdition() throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(get("/editions/{editionId}/tables", targetEditionId)
				.accept(MediaType.APPLICATION_JSON)
				.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@When("I request competition tables for edition id {long}")
	public void iRequestCompetitionTablesForEditionId(long editionId) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(get("/editions/{editionId}/tables", editionId)
				.accept(MediaType.APPLICATION_JSON)
				.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@And("the competition table overview contains {int} table with {int} matches")
	public void theCompetitionTableOverviewContainsTableWithMatches(int tables, int matches) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.length()").value(tables));
		if (tables > 0) {
			stepDefs.result.andExpect(jsonPath("$[0].matches.length()").value(matches));
		}
	}

	@And("the competition table overview includes the target table identifier")
	public void theCompetitionTableOverviewIncludesTheTargetTableIdentifier() throws Exception {
		stepDefs.result.andExpect(jsonPath("$[*].identifier", hasItem(targetEditionTableIdentifier)));
	}

	@And("the competition table overview does not include tables from other editions")
	public void theCompetitionTableOverviewDoesNotIncludeTablesFromOtherEditions() throws Exception {
		stepDefs.result.andExpect(jsonPath("$[*].identifier", not(hasItem(otherEditionTableIdentifier))));
	}

	@And("the competition table overview includes match times {string} and {string}")
	public void theCompetitionTableOverviewIncludesMatchTimes(String startTime, String endTime) throws Exception {
		stepDefs.result
			.andExpect(jsonPath("$[0].matches[0].startTime").value(startTime))
			.andExpect(jsonPath("$[0].matches[0].endTime").value(endTime));
	}

	@And("the competition table overview error is {string}")
	public void theCompetitionTableOverviewErrorIs(String error) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.error").value(error));
	}

	private Edition createEdition(String prefix) {
		Edition edition = Edition.create(2026, prefix + " " + UUID.randomUUID(), prefix);
		return editionRepository.save(edition);
	}

	private Round createRound(Edition edition, int number) {
		Round round = new Round();
		round.setEdition(edition);
		round.setNumber(number);
		return roundRepository.save(round);
	}

	private CompetitionTable createTable(String identifier) {
		CompetitionTable table = new CompetitionTable();
		table.setId(identifier);
		return competitionTableRepository.save(table);
	}

	private void createMatch(Round round, CompetitionTable table, String startTime, String endTime) {
		createMatch(round, table, startTime, endTime, MatchState.SCHEDULED);
	}

	private void createMatch(Round round, CompetitionTable table, String startTime, String endTime, MatchState state) {
		Match match = new Match();
		match.setRound(round);
		match.setCompetitionTable(table);
		match.setStartTime(LocalTime.parse(startTime));
		match.setEndTime(LocalTime.parse(endTime));
		match.setState(state);
		matchRepository.save(match);
	}

	private String shortSuffix() {
		return UUID.randomUUID().toString().substring(0, 6);
	}
}
