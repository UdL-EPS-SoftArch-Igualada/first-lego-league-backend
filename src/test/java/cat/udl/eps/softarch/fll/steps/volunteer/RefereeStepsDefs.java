package cat.udl.eps.softarch.fll.steps.volunteer;

import cat.udl.eps.softarch.fll.domain.CompetitionTable;
import cat.udl.eps.softarch.fll.domain.volunteer.Referee;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class RefereeStepsDefs {

	private Referee referee;
	private CompetitionTable table;

	@Given("a new referee")
	public void a_new_referee() {
		referee = Referee.create("Default Referee", "pep@gmail.com", "123456789");
	}

	@When("I set the referee as an expert")
	public void i_set_the_referee_as_an_expert() {
		referee.setExpert(true);
	}

	@Then("the referee should be recognized as an expert")
	public void the_referee_should_be_recognized_as_an_expert() {
		assertTrue(referee.isExpert(), "The referee should be marked as an expert");
	}

	@Given("a competition table exists with ID {string}")
	public void a_competition_table_exists_with_id(String tableId) {
		table = new CompetitionTable();
		table.setId(tableId);
	}

	@When("I assign the referee to supervise {string}")
	public void i_assign_the_referee_to_supervise(String tableId) {
		assertEquals(tableId, table.getId(), "Table ID mismatch in context");

		table.addReferee(referee);
	}

	@Then("the referee should reference {string} as their assigned table")
	public void the_referee_should_reference_as_their_assigned_table(String expectedTableId) {
		assertNotNull(referee.getSupervisesTable(), "The referee should have an assigned table");
		assertEquals(expectedTableId, referee.getSupervisesTable().getId(), "The assigned table ID is incorrect");
	}

	@Then("the table {string} should list the referee in its staff")
	public void the_table_should_list_the_referee_in_its_staff(String expectedTableId) {
		assertEquals(expectedTableId, table.getId(), "Table ID mismatch in context");

		assertEquals(1, table.getReferees().size(), "The table should have exactly 1 referee assigned");

		boolean containsReferee = table.getReferees().stream().anyMatch(r -> r == referee);
		assertTrue(containsReferee, "The table's staff list should contain this exact referee instance");
	}
}
