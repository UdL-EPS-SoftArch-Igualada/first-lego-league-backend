package cat.udl.eps.softarch.fll.steps;

import cat.udl.eps.softarch.fll.domain.CompetitionTable;
import cat.udl.eps.softarch.fll.domain.Referee;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestCompetitionTable {

	private CompetitionTable table;
	private Referee referee;
	private Exception exceptionCaptured;

	@Given("a new Competition Table with id {string}")
	public void a_new_competition_table_with_id(String id) {
		table = new CompetitionTable();
		table.setId(id);
	}

	@When("I add a referee named {string} to the table")
	public void i_add_a_referee_named(String name) {
		referee = Referee.create(name, "email@email.com", "123456789");
		table.addReferee(referee);
	}

	@When("I try to add another referee to the table")
	public void i_try_to_add_another_referee_to_the_table() {
		exceptionCaptured = assertThrows(IllegalStateException.class, () -> {
			table.addReferee(Referee.create("pep", "pep@email.com", "123456780"));
		});
	}

	@Then("the table should have {int} referee")
	public void the_table_should_have_referee(Integer count) {
		assertEquals(count, table.getReferees().size());
	}

	@Then("the referee {string} should be supervising {string}")
	public void the_referee_should_be_supervising(String refName, String tableId) {
		assertNotNull(referee.getSupervisesTable());
		assertEquals(tableId, referee.getSupervisesTable().getId());
	}

	@Given("the table already has {int} referees")
	public void the_table_already_has_referees(Integer count) {
		for (long i = 0; i < count; i++) {
			Referee ref = Referee.create("name" + i, "email" + i + "@email.com", "123456789");
			ref.setId(i);
			table.addReferee(ref);
		}
	}

	@Then("the validation should prevent adding a 4th referee")
	public void the_validation_should_prevent_adding_a_4th_referee() {
		exceptionCaptured = assertThrows(IllegalStateException.class, () -> {
			Referee newReferee = Referee.create("refereeName", "referee@email.com", "123456789");
			newReferee.setId(999L);
			table.addReferee(newReferee);
		});

		assertNotNull(exceptionCaptured);
		assertEquals("A table can have a maximum of 3 referees", exceptionCaptured.getMessage());
		assertEquals(3, table.getReferees().size());
	}
}
