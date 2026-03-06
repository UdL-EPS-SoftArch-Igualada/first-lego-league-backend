package cat.udl.eps.softarch.fll.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import cat.udl.eps.softarch.fll.domain.CompetitionTable;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.Round;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Duration;
import java.time.LocalTime;


public class MatchStepsDefs {

	private Round round;
	private CompetitionTable table;
	private Match match;

	@Given("a Round exists")
	public void a_round_exists() {
		round = new Round();
		round.setNumber(1);
	}

	@Given("a Competition Table exists")
	public void a_competition_table_exists() {
		table = new CompetitionTable();
		table.setId("Table-1");
	}

	@When("I create a new match starting at {string} and ending at {string}")
	public void i_create_a_new_match_starting_at_and_ending_at(String startTimeStr, String endTimeStr) {
		match = new Match();

		match.setStartTime(LocalTime.parse(startTimeStr));
		match.setEndTime(LocalTime.parse(endTimeStr));

		round.addMatch(match);
		table.addMatch(match);
	}

	@Then("the match should be linked to the round and the table")
	public void the_match_should_be_linked_to_the_round_and_the_table() {

		assertNotNull(match.getRound(), "The match round should not be null");
		assertEquals(round, match.getRound(), "The match should be linked to the correct round");

		assertNotNull(match.getCompetitionTable(), "The match table should not be null");
		assertEquals(table, match.getCompetitionTable(), "The match should be linked to the correct table");

		assertEquals(1, round.getMatches().size(), "The round should contain the match");
		assertEquals(1, table.getMatches().size(), "The table should contain the match");
	}

	@Then("the match duration should be {string} minutes")
	public void the_match_duration_should_be_minutes(String expectedMinutesStr) {
		long expectedMinutes = Long.parseLong(expectedMinutesStr);

		Duration duration = Duration.between(match.getStartTime(), match.getEndTime());
		long actualMinutes = duration.toMinutes();

		assertEquals(expectedMinutes, actualMinutes, "The match duration should match the expected minutes");
	}
}
