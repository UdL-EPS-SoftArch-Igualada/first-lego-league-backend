package cat.udl.eps.softarch.fll.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import cat.udl.eps.softarch.fll.domain.CompetitionTable;
import cat.udl.eps.softarch.fll.domain.Referee;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CompetitionTableStepsDefs {

	private CompetitionTable table;
	private Referee namedReferee;
	private String namedRefereeName;
	private Exception thrownException;

	@Given("a new Competition Table with id {string}")
	public void a_new_competition_table_with_id(String tableId) {
		table = new CompetitionTable();
		table.setId(tableId);
	}

	@When("I add a referee named {string} to the table")
	public void i_add_a_referee_named_to_the_table(String name) {
		namedReferee = new Referee();
		namedRefereeName = name;
		table.addReferee(namedReferee);
	}

	@Then("the table should have {int} referee")
	public void the_table_should_have_referee(Integer expectedCount) {
		assertEquals(expectedCount, table.getReferees().size(),
				"The table does not have the expected number of referees");
	}

	@Then("the referee {string} should be supervising {string}")
	public void the_referee_should_be_supervising(String refereeName, String expectedTableId) {
		assertEquals(refereeName, namedRefereeName, "Referee name mismatch in context");

		assertNotNull(namedReferee.getSupervisesTable(), "The referee is not assigned to any table");
		assertEquals(expectedTableId, namedReferee.getSupervisesTable().getId(),
				"The referee is assigned to the wrong table");
	}

	@Given("the table already has {int} referees")
	public void the_table_already_has_referees(Integer count) {
		for (int i = 0; i < count; i++) {
			table.addReferee(new Referee());
		}
	}

	@When("I try to add another referee to the table")
	public void i_try_to_add_another_referee_to_the_table() {
		Referee extraReferee = new Referee();
		try {
			table.addReferee(extraReferee);
		} catch (Exception e) {
			thrownException = e;
		}
	}

	@Then("the validation should prevent adding a 4th referee")
	public void the_validation_should_prevent_adding_a_4th_referee() {
		assertNotNull(thrownException, "An exception should have been thrown when adding the 4th referee");
		assertTrue(thrownException instanceof IllegalStateException,
				"The exception should be an IllegalStateException");
		assertEquals("A table can have a maximum of 3 referees", thrownException.getMessage());
		assertEquals(3, table.getReferees().size(), "The table should strictly contain 3 referees");
	}
}
