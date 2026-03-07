package cat.udl.eps.softarch.fll.steps;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.MediaType;
import cat.udl.eps.softarch.fll.domain.Edition;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.MatchResult;
import cat.udl.eps.softarch.fll.domain.Round;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.EditionRepository;
import cat.udl.eps.softarch.fll.repository.MatchRepository;
import cat.udl.eps.softarch.fll.repository.MatchResultRepository;
import cat.udl.eps.softarch.fll.repository.RoundRepository;
import cat.udl.eps.softarch.fll.repository.TeamRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class LeaderboardStepDefs {
	private static final AtomicInteger ROUND_NUMBER_SEQUENCE = new AtomicInteger(10000);

	private final StepDefs stepDefs;
	private final EditionRepository editionRepository;
	private final RoundRepository roundRepository;
	private final MatchRepository matchRepository;
	private final MatchResultRepository matchResultRepository;
	private final TeamRepository teamRepository;

	private Long currentEditionId;
	private final Map<String, String> teamNameByAlias = new HashMap<>();

	public LeaderboardStepDefs(StepDefs stepDefs,
			EditionRepository editionRepository,
			RoundRepository roundRepository,
			MatchRepository matchRepository,
			MatchResultRepository matchResultRepository,
			TeamRepository teamRepository) {
		this.stepDefs = stepDefs;
		this.editionRepository = editionRepository;
		this.roundRepository = roundRepository;
		this.matchRepository = matchRepository;
		this.matchResultRepository = matchResultRepository;
		this.teamRepository = teamRepository;
	}

	@Given("an edition with leaderboard data exists")
	public void anEditionWithLeaderboardDataExists() {
		teamNameByAlias.clear();
		Edition edition = createEdition();
		Round round = createRoundForEdition(edition);

		Team teamA = createTeam("TeamA");
		Team teamB = createTeam("TeamB");
		Team teamC = createTeam("TeamC");

		createResult(round, teamA, 200);
		createResult(round, teamA, 180);
		createResult(round, teamA, 180);

		createResult(round, teamB, 170);
		createResult(round, teamB, 170);
		createResult(round, teamB, 160);

		createResult(round, teamC, 210);
		createResult(round, teamC, 200);
	}

	@Given("an edition with tie on score and different matches played exists")
	public void anEditionWithTieOnScoreAndDifferentMatchesPlayedExists() {
		teamNameByAlias.clear();
		Edition edition = createEdition();
		Round round = createRoundForEdition(edition);

		Team teamAlpha = createTeam("TeamAlpha");
		Team teamBeta = createTeam("TeamBeta");

		createResult(round, teamAlpha, 50);
		createResult(round, teamAlpha, 50);

		createResult(round, teamBeta, 100);
	}

	@Given("an empty edition exists")
	public void anEmptyEditionExists() {
		teamNameByAlias.clear();
		Edition edition = createEdition();
		createRoundForEdition(edition);
	}

	@Given("an edition with tie on score and matches played exists")
	public void anEditionWithTieOnScoreAndMatchesPlayedExists() {
		teamNameByAlias.clear();
		Edition edition = createEdition();
		Round round = createRoundForEdition(edition);

		Team teamAlpha = createTeam("TeamAlpha");
		Team teamBeta = createTeam("TeamBeta");

		createResult(round, teamAlpha, 60);
		createResult(round, teamAlpha, 40);

		createResult(round, teamBeta, 50);
		createResult(round, teamBeta, 50);
	}

	@When("I request leaderboard for that edition with page {int} and size {int}")
	public void iRequestLeaderboardForThatEditionWithPagination(int page, int size) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(get("/leaderboards/editions/" + currentEditionId)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.accept(MediaType.APPLICATION_JSON));
	}

	@When("I request leaderboard for non-existent edition {long} with page {int} and size {int}")
	public void iRequestLeaderboardForNonExistentEditionWithPagination(long editionId, int page, int size) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(get("/leaderboards/editions/" + editionId)
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.accept(MediaType.APPLICATION_JSON));
	}

	@When("I request leaderboard for a non-existent edition with page {int} and size {int}")
	public void iRequestLeaderboardForANonExistentEditionWithPagination(int page, int size) throws Exception {
		long nonExistingEditionId = Long.MAX_VALUE;
		while (editionRepository.existsById(nonExistingEditionId)) {
			nonExistingEditionId--;
		}
		iRequestLeaderboardForNonExistentEditionWithPagination(nonExistingEditionId, page, size);
	}

	@And("^leaderboard should contain (\\d+) item[s]?$")
	public void leaderboardShouldContainItems(int expectedSize) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.items.length()").value(expectedSize));
	}

	@And("leaderboard totalElements should be {int}")
	public void leaderboardTotalElementsShouldBe(int totalElements) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.totalElements").value(totalElements));
	}

	@And("leaderboard item at index {int} should have team {string}")
	public void leaderboardItemAtIndexShouldHaveTeam(int index, String teamAlias) throws Exception {
		String expectedName = teamNameByAlias.getOrDefault(teamAlias, teamAlias);
		stepDefs.result.andExpect(jsonPath("$.items[" + index + "].teamName").value(expectedName));
	}

	@And("leaderboard item at index {int} should have position {int}")
	public void leaderboardItemAtIndexShouldHavePosition(int index, int position) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.items[" + index + "].position").value(position));
	}

	private Edition createEdition() {
		Edition edition = new Edition();
		edition.setYear(2025 + ((UUID.randomUUID().hashCode() & 0x7fffffff) % 1000));
		edition.setVenueName("Venue-" + UUID.randomUUID().toString().substring(0, 6));
		edition.setDescription("Leaderboard test edition");
		edition = editionRepository.save(edition);
		currentEditionId = edition.getId();
		return edition;
	}

	private Round createRoundForEdition(Edition edition) {
		Round round = new Round();
		round.setNumber(ROUND_NUMBER_SEQUENCE.getAndIncrement());
		round.setEdition(edition);
		return roundRepository.save(round);
	}

	private Team createTeam(String teamAlias) {
		Team team = new Team();
		String teamName = teamAlias + "-" + UUID.randomUUID().toString().substring(0, 6);
		team.setName(teamName);
		team.setCity("Igualada");
		team.setFoundationYear(2000);
		team.setCategory("Challenge");
		team.setEducationalCenter("EPS");
		team.setInscriptionDate(LocalDate.now());
		teamNameByAlias.put(teamAlias, teamName);
		return teamRepository.save(team);
	}

	private void createResult(Round round, Team team, int score) {
		Team opponent = createTeam("Opponent-" + UUID.randomUUID().toString().substring(0, 4));
		Match match = new Match();
		match.setRound(round);
		match.setTeamA(team);
		match.setTeamB(opponent);
		match.setStartTime(LocalTime.of(10, 0));
		match.setEndTime(LocalTime.of(11, 0));
		match = matchRepository.save(match);

		MatchResult result = new MatchResult();
		result.setMatch(match);
		result.setTeam(team);
		result.setScore(score);
		matchResultRepository.save(result);
	}
}
