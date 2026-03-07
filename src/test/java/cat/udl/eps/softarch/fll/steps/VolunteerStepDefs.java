package cat.udl.eps.softarch.fll.steps;

import cat.udl.eps.softarch.fll.domain.DomainValidationException;
import cat.udl.eps.softarch.fll.domain.Floater;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.FloaterRepository;
import cat.udl.eps.softarch.fll.repository.TeamRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.validation.ConstraintViolationException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VolunteerStepDefs {

	private final FloaterRepository floaterRepository;
	private final TeamRepository teamRepository;

	private Floater currentFloater;
	private Team currentTeam;
	private Exception lastException;
	private List<Floater> searchResults;

	public VolunteerStepDefs(FloaterRepository floaterRepository, TeamRepository teamRepository) {
		this.floaterRepository = floaterRepository;
		this.teamRepository = teamRepository;
	}

	@Given("the volunteer system is empty")
	@Transactional
	public void clearVolunteerSystem() {
		teamRepository.deleteAll();
		floaterRepository.deleteAll();
		currentFloater = null;
		currentTeam = null;
		lastException = null;
		searchResults = null;
	}

	@When("I create a floater with name {string}, email {string}, phone {string} and student code {string}")
	public void createFloater(String name, String email, String phone, String studentCode) {
		currentFloater = Floater.create(name, email, phone, studentCode);
	}

	@When("I save the floater")
	public void saveFloater() {
		currentFloater = floaterRepository.save(currentFloater);
	}

	@Then("the floater with student code {string} should exist in the system")
	public void floaterShouldExist(String studentCode) {
		Optional<Floater> floater = floaterRepository.findByStudentCode(studentCode);
		assertTrue(floater.isPresent(), "Floater with student code " + studentCode + " should exist");
	}

	@Then("there should be {int} floaters in the system")
	public void verifyFloaterCount(int count) {
		assertEquals(count, floaterRepository.count());
	}


	@When("I try to assign the floater {string} to team {string}")
	@Transactional
	public void tryAssignFloaterToTeam(String studentCode, String teamName) {
		try {
			Floater floater = floaterRepository.findByStudentCode(studentCode).orElseThrow();
			Team team = teamRepository.findByName(teamName).orElseThrow();

			team.addFloater(floater);
			teamRepository.save(team);

		} catch (Exception e) {
			lastException = e;
		}
	}

	@When("I try to create a floater with name {string} and email {string} and phone {string} and student code {string}")
	public void tryCreateInvalidFloater(String name, String email, String phone, String studentCode) {
		try {
			Floater floater = Floater.create(name, email, phone, studentCode);
			floaterRepository.save(floater);
		} catch (Exception e) {
			lastException = e;
		}
	}

	@Then("the floater creation should fail with validation error")
	public void floaterCreationShouldFail() {
		assertNotNull(lastException);
		assertTrue(isValidationOrConstraintException(lastException),
			"Expected a validation or constraint exception but got: " + lastException.getClass().getName());
	}

	private boolean isValidationOrConstraintException(Throwable ex) {
		Throwable current = ex;
		while (current != null) {
			if (current instanceof ConstraintViolationException
				|| current instanceof org.springframework.dao.DataIntegrityViolationException
				|| current instanceof DomainValidationException) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	@Then("I should receive the error {string}")
	public void verifyErrorMessage(String expectedMessage) {
		assertNotNull(lastException, "No exception was captured");
		assertTrue(findMessageInExceptionChain(lastException, expectedMessage),
			"Error message '" + expectedMessage + "' not found in exception chain. Got: "
				+ lastException.getMessage());
	}

	private boolean findMessageInExceptionChain(Throwable ex, String expectedMessage) {
		Throwable current = ex;
		while (current != null) {
			if (current.getMessage() != null && current.getMessage().contains(expectedMessage)) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	@When("I update the floater phone number to {string}")
	public void updateFloaterPhone(String newPhone) {
		currentFloater.setPhoneNumber(newPhone);
		currentFloater = floaterRepository.save(currentFloater);
	}

	@Then("the floater with student code {string} should have phone {string}")
	public void verifyFloaterPhone(String studentCode, String expectedPhone) {
		Optional<Floater> floater = floaterRepository.findByStudentCode(studentCode);
		assertTrue(floater.isPresent(), "Floater not found");
		assertEquals(expectedPhone, floater.get().getPhoneNumber());
	}

	@When("I delete the floater with student code {string}")
	public void deleteFloater(String studentCode) {
		Floater floater = floaterRepository.findByStudentCode(studentCode).orElseThrow();
		floaterRepository.delete(floater);
	}

	@Then("the floater with student code {string} should not exist")
	public void floaterShouldNotExist(String studentCode) {
		Optional<Floater> floater = floaterRepository.findByStudentCode(studentCode);
		assertTrue(floater.isEmpty(), "Floater should not exist");
	}

	@When("I search for floaters with student code {string}")
	public void searchByStudentCode(String studentCode) {
		Optional<Floater> floater = floaterRepository.findByStudentCode(studentCode);
		searchResults = floater.map(List::of).orElse(List.of());
	}

	@When("I search for floaters with name containing {string}")
	public void searchByNameContaining(String text) {
		searchResults = floaterRepository.findByNameContainingIgnoreCase(text);
	}

	@Then("I should find {int} floater(s) in the results")
	public void verifySearchResultCount(int count) {
		assertNotNull(searchResults);
		assertEquals(count, searchResults.size());
	}

	@Given("a team named {string} from city {string} exists for floater assignment")
	public void createTeamForFloaterAssignment(String name, String city) {
		currentTeam = Team.create(name, city, 2020, "Challenge");
		currentTeam = teamRepository.save(currentTeam);
	}

	@When("I assign the floater {string} to team {string}")
	@Transactional
	public void assignFloaterToTeam(String studentCode, String teamName) {
		Floater floater = floaterRepository.findByStudentCode(studentCode).orElseThrow();
		Team team = teamRepository.findByName(teamName).orElseThrow();
		team.addFloater(floater);
		teamRepository.save(team);
	}

	@Then("the team {string} should have {int} floater(s) assigned")
	@Transactional
	public void verifyTeamFloaterCount(String teamName, int count) {
		Team team = teamRepository.findByName(teamName).orElseThrow();
		assertEquals(count, team.getFloaters().size());
	}

	@Then("the floater {string} should assist {int} teams")
	@Transactional
	public void verifyFloaterAssistsTeams(String studentCode, int count) {
		Optional<Floater> floater = floaterRepository.findByStudentCode(studentCode);
		assertTrue(floater.isPresent(), "Floater not found");
		assertEquals(count, floater.get().getAssistedTeams().size());
	}

	@When("I remove the floater {string} from team {string}")
	@Transactional
	public void removeFloaterFromTeam(String studentCode, String teamName) {
		Floater floater = floaterRepository.findByStudentCode(studentCode).orElseThrow();
		Team team = teamRepository.findByName(teamName).orElseThrow();
		team.removeFloater(floater);
		teamRepository.save(team);
	}
}
