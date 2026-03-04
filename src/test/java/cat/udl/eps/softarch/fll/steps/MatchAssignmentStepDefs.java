package cat.udl.eps.softarch.fll.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import cat.udl.eps.softarch.fll.domain.Floater;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.MatchState;
import cat.udl.eps.softarch.fll.domain.Referee;
import cat.udl.eps.softarch.fll.domain.Round;
import cat.udl.eps.softarch.fll.repository.FloaterRepository;
import cat.udl.eps.softarch.fll.repository.MatchRepository;
import cat.udl.eps.softarch.fll.repository.MatchResultRepository;
import cat.udl.eps.softarch.fll.repository.RefereeRepository;
import cat.udl.eps.softarch.fll.repository.RoundRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class MatchAssignmentStepDefs {
	private static final Long NON_EXISTENT_MATCH_ID = Long.MAX_VALUE;
	private static final AtomicInteger ROUND_NUMBER_SEQUENCE = new AtomicInteger(1);

	private final StepDefs stepDefs;
	private final MatchRepository matchRepository;
	private final RefereeRepository refereeRepository;
	private final FloaterRepository floaterRepository;
	private final MatchResultRepository matchResultRepository;
	private final RoundRepository roundRepository;

	private Match currentMatch;
	private Referee currentReferee;
	private Referee firstReferee;
	private Referee secondReferee;
	private Floater currentFloater;
	private Round currentRound;
	private final List<Match> batchMatches = new ArrayList<>();

	public MatchAssignmentStepDefs(
			StepDefs stepDefs,
			MatchRepository matchRepository,
			RefereeRepository refereeRepository,
			FloaterRepository floaterRepository,
			MatchResultRepository matchResultRepository,
			RoundRepository roundRepository) {
		this.stepDefs = stepDefs;
		this.matchRepository = matchRepository;
		this.refereeRepository = refereeRepository;
		this.floaterRepository = floaterRepository;
		this.matchResultRepository = matchResultRepository;
		this.roundRepository = roundRepository;
	}

	@Given("the match assignment system is empty")
	public void clearMatchAssignmentSystem() {
		matchResultRepository.deleteAll();
		matchRepository.deleteAll();
		roundRepository.deleteAll();
		floaterRepository.deleteAll();
		refereeRepository.deleteAll();
		currentMatch = null;
		currentReferee = null;
		firstReferee = null;
		secondReferee = null;
		currentFloater = null;
		currentRound = null;
		batchMatches.clear();
	}

	@Given("a scheduled match exists from {string} to {string}")
	public void createScheduledMatch(String startTime, String endTime) {
		currentMatch = createMatch(MatchState.SCHEDULED, startTime, endTime, null);
	}

	@Given("a finished match exists from {string} to {string}")
	public void createFinishedMatch(String startTime, String endTime) {
		currentMatch = createMatch(MatchState.FINISHED, startTime, endTime, null);
	}

	@Given("a referee volunteer exists")
	public void createReferee() {
		Referee referee = new Referee();
		referee.setName("Referee One");
		referee.setEmailAddress("ref" + System.nanoTime() + "@mail.com");
		referee.setPhoneNumber("123456789");
		currentReferee = refereeRepository.save(referee);
		if (firstReferee == null) {
			firstReferee = currentReferee;
		}
	}

	@Given("another referee volunteer exists")
	public void createAnotherReferee() {
		Referee referee = new Referee();
		referee.setName("Referee Two");
		referee.setEmailAddress("ref" + System.nanoTime() + "@mail.com");
		referee.setPhoneNumber("987654321");
		secondReferee = refereeRepository.save(referee);
	}

	@Given("a floater volunteer exists")
	public void createFloater() {
		Floater floater = new Floater();
		floater.setName("Floater One");
		floater.setEmailAddress("floater" + System.nanoTime() + "@mail.com");
		floater.setPhoneNumber("555555555");
		floater.setStudentCode("F" + System.nanoTime());
		currentFloater = floaterRepository.save(floater);
	}

	@Given("a scheduled match exists from {string} to {string} assigned to the first referee")
	public void createScheduledMatchAssignedToFirstReferee(String startTime, String endTime) {
		currentMatch = createMatch(MatchState.SCHEDULED, startTime, endTime, firstReferee);
	}

	@Given("a scheduled match exists from {string} to {string} assigned to the referee")
	public void createScheduledMatchAssignedToReferee(String startTime, String endTime) {
		createMatch(MatchState.SCHEDULED, startTime, endTime, currentReferee);
	}

	@Given("a round with two scheduled matches exists from {string}-{string} and {string}-{string}")
	public void createRoundWithTwoScheduledMatches(
			String firstStartTime,
			String firstEndTime,
			String secondStartTime,
			String secondEndTime) {
		currentRound = createRound();
		batchMatches.clear();
		batchMatches.add(createMatch(MatchState.SCHEDULED, firstStartTime, firstEndTime, null, currentRound));
		batchMatches.add(createMatch(MatchState.SCHEDULED, secondStartTime, secondEndTime, null, currentRound));
	}

	@Given("a round with overlapping scheduled matches exists from {string}-{string} and {string}-{string}")
	public void createRoundWithOverlappingScheduledMatches(
			String firstStartTime,
			String firstEndTime,
			String secondStartTime,
			String secondEndTime) {
		createRoundWithTwoScheduledMatches(firstStartTime, firstEndTime, secondStartTime, secondEndTime);
	}

	@When("I assign that referee to that match")
	public void assignCurrentRefereeToCurrentMatch() throws Exception {
		assign(currentMatch.getId().toString(), currentReferee.getId().toString());
	}

	@When("I assign that floater to that match")
	public void assignCurrentFloaterToCurrentMatch() throws Exception {
		assign(currentMatch.getId().toString(), currentFloater.getId().toString());
	}

	@When("I assign the second referee to that match")
	public void assignSecondRefereeToCurrentMatch() throws Exception {
		assign(currentMatch.getId().toString(), secondReferee.getId().toString());
	}

	@When("I assign referee id {string} to match id {string}")
	public void assignByIds(String refereeId, String matchId) throws Exception {
		assign(matchId, refereeId);
	}

	@When("I assign referee id {string} to that match")
	public void assignRefereeIdToCurrentMatch(String refereeId) throws Exception {
		assign(currentMatch.getId().toString(), refereeId);
	}

	@When("I assign referees in batch for that round")
	public void assignRefereesInBatchForRound() throws Exception {
		assignBatch(List.of(
				new BatchAssignment(batchMatches.get(0).getId(), firstReferee.getId()),
				new BatchAssignment(batchMatches.get(1).getId(), secondReferee.getId())));
	}

	@When("I assign the same referee in batch to both matches")
	public void assignSameRefereeInBatchToBothMatches() throws Exception {
		assignBatch(List.of(
				new BatchAssignment(batchMatches.get(0).getId(), firstReferee.getId()),
				new BatchAssignment(batchMatches.get(1).getId(), firstReferee.getId())));
	}

	@When("I assign one referee and one floater in batch for that round")
	public void assignOneRefereeAndOneFloaterInBatchForRound() throws Exception {
		assignBatch(List.of(
				new BatchAssignment(batchMatches.get(0).getId(), firstReferee.getId()),
				new BatchAssignment(batchMatches.get(1).getId(), currentFloater.getId())));
	}

	@When("I assign referees in batch for round id {string}")
	public void assignRefereesInBatchForRoundId(String roundId) throws Exception {
		assignBatchForRound(roundId, List.of(new BatchAssignment(NON_EXISTENT_MATCH_ID, currentReferee.getId())));
	}

	@Then("assignment response status is {string}")
	public void verifyAssignmentStatus(String expectedStatus) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.status").value(expectedStatus));
	}

	@And("assignment error code is {string}")
	public void verifyAssignmentErrorCode(String expectedErrorCode) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.error").value(expectedErrorCode));
	}

	@And("the match is assigned to that referee")
	public void verifyMatchAssignedToReferee() {
		Match persisted = matchRepository.findById(currentMatch.getId()).orElseThrow();
		assertNotNull(persisted.getReferee());
		assertEquals(currentReferee.getId(), persisted.getReferee().getId());
	}

	@And("both batch matches are assigned to their referees")
	public void verifyBothBatchMatchesAssignedToReferees() {
		Match firstPersisted = matchRepository.findById(batchMatches.get(0).getId()).orElseThrow();
		Match secondPersisted = matchRepository.findById(batchMatches.get(1).getId()).orElseThrow();

		assertNotNull(firstPersisted.getReferee());
		assertNotNull(secondPersisted.getReferee());
		assertEquals(firstReferee.getId(), firstPersisted.getReferee().getId());
		assertEquals(secondReferee.getId(), secondPersisted.getReferee().getId());
	}

	@And("none of the batch matches should have a referee assigned")
	public void verifyNoBatchMatchHasRefereeAssigned() {
		for (Match match : batchMatches) {
			Match persisted = matchRepository.findById(match.getId()).orElseThrow();
			assertNull(persisted.getReferee());
		}
	}

	@And("batch assignment error cause is {string}")
	public void verifyBatchAssignmentErrorCause(String expectedCause) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.details.cause").value(expectedCause));
	}

	private Match createMatch(MatchState state, String startTime, String endTime, Referee referee) {
		return createMatch(state, startTime, endTime, referee, null);
	}

	private Match createMatch(MatchState state, String startTime, String endTime, Referee referee, Round round) {
		Match match = new Match();
		match.setState(state);
		match.setStartTime(LocalTime.parse(startTime));
		match.setEndTime(LocalTime.parse(endTime));
		match.setReferee(referee);
		match.setRound(round);
		return matchRepository.save(match);
	}

	private Round createRound() {
		Round round = new Round();
		round.setNumber(ROUND_NUMBER_SEQUENCE.getAndIncrement());
		return roundRepository.save(round);
	}

	private void assign(String matchId, String refereeId) throws Exception {
		JSONObject request = new JSONObject();
		request.put("matchId", matchId);
		request.put("refereeId", refereeId);
		stepDefs.result = stepDefs.mockMvc.perform(
				post("/matchAssignments/assign")
						.with(AuthenticationStepDefs.authenticate())
						.contentType(MediaType.APPLICATION_JSON)
						.content(request.toString())
						.accept(MediaType.APPLICATION_JSON));
	}

	private void assignBatch(List<BatchAssignment> assignments) throws Exception {
		assignBatchForRound(String.valueOf(currentRound.getId()), assignments);
	}

	private void assignBatchForRound(String roundId, List<BatchAssignment> assignments) throws Exception {
		JSONObject request = new JSONObject();
		request.put("roundId", roundId);

		JSONArray payloadAssignments = new JSONArray();
		for (BatchAssignment assignment : assignments) {
			JSONObject item = new JSONObject();
			item.put("matchId", String.valueOf(assignment.matchId()));
			item.put("refereeId", String.valueOf(assignment.refereeId()));
			payloadAssignments.put(item);
		}
		request.put("assignments", payloadAssignments);

		stepDefs.result = stepDefs.mockMvc.perform(
				post("/matchAssignments/batch")
						.with(AuthenticationStepDefs.authenticate())
						.contentType(MediaType.APPLICATION_JSON)
						.content(request.toString())
						.accept(MediaType.APPLICATION_JSON));
	}

	private record BatchAssignment(Long matchId, Long refereeId) {}
}
