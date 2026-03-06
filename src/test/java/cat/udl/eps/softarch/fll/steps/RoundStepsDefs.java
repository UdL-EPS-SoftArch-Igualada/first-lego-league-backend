package cat.udl.eps.softarch.fll.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.Round;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class RoundStepsDefs {

	private Round round;
	private Match removedMatch;

	@Given("a new Round with number {int}")
	public void a_new_round_with_number(Integer number) {
		round = new Round();
		round.setNumber(number);
	}

	@When("I add {int} new matches to this round")
	public void i_add_new_matches_to_this_round(Integer count) {
		for (int i = 0; i < count; i++) {
			Match match = new Match();
			round.addMatch(match);
		}
	}

	@Given("the round has {int} matches")
	public void the_round_has_matches(Integer count) {
		i_add_new_matches_to_this_round(count);
	}

	@When("I remove one match from the round")
	public void i_remove_one_match_from_the_round() {
		if (!round.getMatches().isEmpty()) {
			removedMatch = round.getMatches().get(0);

			round.removeMatch(removedMatch);
		}
	}

	@Then("the round should contain {int} matches")
	public void the_round_should_contain_matches(Integer expectedCount) {
		assertEquals(expectedCount, round.getMatches().size(), "The number of matches in the round is incorrect");
	}

	@Then("the round should contain {int} match")
	public void the_round_should_contain_match(Integer expectedCount) {
		the_round_should_contain_matches(expectedCount);
	}

	@Then("each match should reference the round with number {int}")
	public void each_match_should_reference_the_round_with_number(Integer expectedNumber) {
		for (Match match : round.getMatches()) {
			assertNotNull(match.getRound(), "The match should have a round reference");
			assertEquals(expectedNumber, match.getRound().getNumber(),
					"The match is referencing the wrong round number");
		}
	}

	@Then("the removed match should no longer reference the round")
	public void the_removed_match_should_no_longer_reference_the_round() {
		assertNotNull(removedMatch, "A match should have been removed during the 'When' step");
		assertNull(removedMatch.getRound(),
				"The removed match should have its round reference set to null (Bidirectional consistency)");
	}
}
