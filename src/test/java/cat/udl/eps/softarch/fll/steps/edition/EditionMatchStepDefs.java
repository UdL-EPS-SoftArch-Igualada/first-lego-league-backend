package cat.udl.eps.softarch.fll.steps.edition;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import cat.udl.eps.softarch.fll.domain.edition.Edition;
import cat.udl.eps.softarch.fll.domain.match.CompetitionTable;
import cat.udl.eps.softarch.fll.domain.match.Match;
import cat.udl.eps.softarch.fll.domain.match.Round;
import cat.udl.eps.softarch.fll.domain.team.Team;
import cat.udl.eps.softarch.fll.repository.edition.EditionRepository;
import cat.udl.eps.softarch.fll.repository.match.CompetitionTableRepository;
import cat.udl.eps.softarch.fll.repository.match.MatchRepository;
import cat.udl.eps.softarch.fll.repository.match.RoundRepository;
import cat.udl.eps.softarch.fll.repository.team.TeamRepository;
import cat.udl.eps.softarch.fll.steps.app.StepDefs;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

public class EditionMatchStepDefs {

	private final StepDefs stepDefs;
	private final EditionRepository editionRepository;
	private final TeamRepository teamRepository;
	private final MatchRepository matchRepository;
	private final RoundRepository roundRepository;
	private final CompetitionTableRepository tableRepository;

	private Long currentEditionId;

	public EditionMatchStepDefs(StepDefs stepDefs,
			EditionRepository editionRepository,
			TeamRepository teamRepository,
			MatchRepository matchRepository,
			RoundRepository roundRepository,
			CompetitionTableRepository tableRepository) {
		this.stepDefs = stepDefs;
		this.editionRepository = editionRepository;
		this.teamRepository = teamRepository;
		this.matchRepository = matchRepository;
		this.roundRepository = roundRepository;
		this.tableRepository = tableRepository;
	}

	@Before("@EditionMatches")
	public void setup() {
		matchRepository.deleteAll();
		teamRepository.deleteAll();
		roundRepository.deleteAll();
		tableRepository.deleteAll();
		editionRepository.deleteAll();
		stepDefs.result = null;
	}

	@Given("There is an edition with id for matches")
	public void thereIsAnEditionWithIdForMatches() {
		Edition edition = Edition.create(2025, "TestVenue", "Test edition for matches");
		edition = editionRepository.save(edition);
		currentEditionId = edition.getId();
	}

	@Given("the edition has teams with matches")
	public void theEditionHasTeamsWithMatches() {
		Edition edition = editionRepository.findById(currentEditionId).orElseThrow();

		Team teamA = new Team();
		teamA.setName("AlphaTeam");
		teamA.setCity("Barcelona");
		teamA.setFoundationYear(2010);
		teamA.setCategory("Challenge");
		teamA.setEdition(edition);
		teamA = teamRepository.save(teamA);

		Team teamB = new Team();
		teamB.setName("BetaTeam");
		teamB.setCity("Madrid");
		teamB.setFoundationYear(2012);
		teamB.setCategory("Challenge");
		teamB.setEdition(edition);
		teamB = teamRepository.save(teamB);

		CompetitionTable table = new CompetitionTable();
		table.setId("Table-EdMatch");
		table = tableRepository.save(table);

		Round round = new Round();
		round.setNumber(1);
		round.setEdition(edition);
		round = roundRepository.save(round);

		Match match1 = new Match();
		match1.setStartTime(LocalTime.of(10, 0));
		match1.setEndTime(LocalTime.of(10, 30));
		match1.setTeamA(teamA);
		match1.setTeamB(teamB);
		match1.setCompetitionTable(table);
		match1.setRound(round);
		matchRepository.save(match1);

		Match match2 = new Match();
		match2.setStartTime(LocalTime.of(11, 0));
		match2.setEndTime(LocalTime.of(11, 30));
		match2.setTeamA(teamA);
		match2.setCompetitionTable(table);
		match2.setRound(round);
		matchRepository.save(match2);
	}

	@Given("the edition has two teams in the same match")
	public void theEditionHasTwoTeamsInTheSameMatch() {
		Edition edition = editionRepository.findById(currentEditionId).orElseThrow();

		Team teamA = new Team();
		teamA.setName("GammaTeam");
		teamA.setCity("Valencia");
		teamA.setFoundationYear(2015);
		teamA.setCategory("Challenge");
		teamA.setEdition(edition);
		teamA = teamRepository.save(teamA);

		Team teamB = new Team();
		teamB.setName("DeltaTeam");
		teamB.setCity("Sevilla");
		teamB.setFoundationYear(2016);
		teamB.setCategory("Challenge");
		teamB.setEdition(edition);
		teamB = teamRepository.save(teamB);

		CompetitionTable table = new CompetitionTable();
		table.setId("Table-EdDup");
		table = tableRepository.save(table);

		Round round = new Round();
		round.setNumber(2);
		round.setEdition(edition);
		round = roundRepository.save(round);

		Match match = new Match();
		match.setStartTime(LocalTime.of(14, 0));
		match.setEndTime(LocalTime.of(14, 30));
		match.setTeamA(teamA);
		match.setTeamB(teamB);
		match.setCompetitionTable(table);
		match.setRound(round);
		matchRepository.save(match);
	}

	@When("I get matches for the edition")
	public void iGetMatchesForTheEdition() throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
			get("/editions/" + currentEditionId + "/matches")
				.with(user("admin"))
				.contentType(MediaType.APPLICATION_JSON)
		);
	}

	@When("I get matches for edition {long}")
	public void iGetMatchesForEdition(Long editionId) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
			get("/editions/" + editionId + "/matches")
				.with(user("admin"))
				.contentType(MediaType.APPLICATION_JSON)
		);
	}

	@Then("the edition matches response should contain {int} matches")
	public void theEditionMatchesResponseShouldContainMatches(int count) throws Exception {
		stepDefs.result
			.andExpect(jsonPath("$._embedded.matches").isArray())
			.andExpect(jsonPath("$._embedded.matches.length()").value(count));
	}

	@And("the edition matches should not contain duplicates")
	public void theEditionMatchesShouldNotContainDuplicates() throws Exception {
		String json = stepDefs.result.andReturn().getResponse().getContentAsString();
		java.util.List<String> matchIds = JsonPath.read(json, "$._embedded.matches[*].matchId");
		Set<String> uniqueIds = new HashSet<>(matchIds);
		if (uniqueIds.size() != matchIds.size()) {
			throw new AssertionError("Duplicate matches found in response: " + matchIds);
		}
	}

	@Then("the edition matches response should be empty")
	public void theEditionMatchesResponseShouldBeEmpty() throws Exception {
		stepDefs.result
			.andExpect(jsonPath("$._embedded.matches").isArray())
			.andExpect(jsonPath("$._embedded.matches").isEmpty());
	}

	@Then("the edition matches error should be {string}")
	public void theEditionMatchesErrorShouldBe(String errorCode) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.error").value(errorCode));
	}
}
