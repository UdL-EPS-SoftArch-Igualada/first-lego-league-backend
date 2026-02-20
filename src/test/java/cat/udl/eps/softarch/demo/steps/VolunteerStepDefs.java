package cat.udl.eps.softarch.demo.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cat.udl.eps.softarch.demo.domain.Floater;
import cat.udl.eps.softarch.demo.domain.Team;
import cat.udl.eps.softarch.demo.repository.FloaterRepository;
import cat.udl.eps.softarch.demo.repository.TeamRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class VolunteerStepDefs {

	private final FloaterRepository floaterRepository;
    private final TeamRepository teamRepository;

    private Floater currentFloater;
    private Team currentTeam;
    private Exception lastException;
    private List<Floater> searchResults;

    public VolunteerStepDefs(
            FloaterRepository floaterRepository,
            TeamRepository teamRepository) {
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
        currentFloater = new Floater();
        currentFloater.setName(name);
        currentFloater.setEmailAddress(email);
        currentFloater.setPhoneNumber(phone);
        currentFloater.setStudentCode(studentCode);
    }

    @When("I save the floater")
    public void saveFloater() {
        currentFloater = floaterRepository.save(currentFloater);
    }

    @Then("the floater with student code {string} should exist in the system")
    public void floaterShouldExist(String studentCode) {
        List<Floater> floaters = floaterRepository.findByStudentCode(studentCode);
        assertFalse(floaters.isEmpty(), "Floater with student code " + studentCode + " should exist");
    }

    @Then("there should be {int} floaters in the system")
    public void verifyFloaterCount(int count) {
        assertEquals(count, floaterRepository.count());
    }

    @When("I try to create a floater with name {string} and email {string} and phone {string} and student code {string}")
    public void tryCreateInvalidFloater(String name, String email, String phone, String studentCode) {
        try {
            Floater floater = new Floater();
            floater.setName(name);
            floater.setEmailAddress(email);
            floater.setPhoneNumber(phone);
            floater.setStudentCode(studentCode);
            floaterRepository.save(floater);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the floater creation should fail with validation error")
    public void floaterCreationShouldFail() {
        assertNotNull(lastException, "Expected validation exception but none was thrown");
    }

    @When("I update the floater phone number to {string}")
    public void updateFloaterPhone(String newPhone) {
        currentFloater.setPhoneNumber(newPhone);
        currentFloater = floaterRepository.save(currentFloater);
    }

    @Then("the floater with student code {string} should have phone {string}")
    public void verifyFloaterPhone(String studentCode, String expectedPhone) {
        List<Floater> floaters = floaterRepository.findByStudentCode(studentCode);
        assertFalse(floaters.isEmpty(), "Floater not found");
        assertEquals(expectedPhone, floaters.get(0).getPhoneNumber());
    }

    @When("I delete the floater with student code {string}")
    public void deleteFloater(String studentCode) {
        List<Floater> floaters = floaterRepository.findByStudentCode(studentCode);
        if (!floaters.isEmpty()) {
            floaterRepository.delete(floaters.get(0));
        }
    }

    @Then("the floater with student code {string} should not exist")
    public void floaterShouldNotExist(String studentCode) {
        List<Floater> floaters = floaterRepository.findByStudentCode(studentCode);
        assertTrue(floaters.isEmpty(), "Floater should not exist");
    }

    @When("I search for floaters with student code {string}")
    public void searchByStudentCode(String studentCode) {
        searchResults = floaterRepository.findByStudentCode(studentCode);
    }

    @When("I search for floaters with name containing {string}")
    public void searchByNameContaining(String text) {
        searchResults = floaterRepository.findByNameContainingIgnoreCase(text);
    }

	@Then("I should find {int} floater(s) in the results")
	public void verifySearchResultCount(int count) {
        assertNotNull(searchResults, "Search results is null");
        assertEquals(count, searchResults.size());
    }

    @Given("a team named {string} from city {string} exists for floater assignment")
    public void createTeamForFloaterAssignment(String name, String city) {
        currentTeam = new Team(name);
        currentTeam.setCity(city);
        currentTeam.setFoundationYear(2020);
        currentTeam.setCategory("Challenge");
        currentTeam = teamRepository.save(currentTeam);
    }

    @When("I assign the floater {string} to team {string}")
    @Transactional
    public void assignFloaterToTeam(String studentCode, String teamName) {
		List<Floater> floaters = floaterRepository.findByStudentCode(studentCode);
		Team team = teamRepository.findByName(teamName).orElseThrow();
		if (!floaters.isEmpty()) {
			   team.addFloater(floaters.get(0));
				teamRepository.save(team);
		}
    }

    @Then("the team {string} should have {int} floater(s) assigned")
    @Transactional
    public void verifyTeamFloaterCount(String teamName, int count) {
        Team team = teamRepository.findByName(teamName).orElseThrow();
        assertEquals(count, team.getFloaters().size());
    }

    @When("I try to assign the floater {string} to team {string}")
    @Transactional
    public void tryAssignFloaterToTeam(String studentCode, String teamName) {
        try {
		   assignFloaterToTeam(studentCode, teamName);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("I should receive the error {string}")
    public void verifyErrorMessage(String expectedMessage) {
        assertNotNull(lastException, "No exception was captured");
        boolean found = findMessageInExceptionChain(lastException, expectedMessage);
        assertTrue(found, "Error message '" + expectedMessage + "' not found. Got: " + lastException.getMessage());
    }

    private boolean findMessageInExceptionChain(Throwable exception, String expectedMessage) {
        Throwable current = exception;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains(expectedMessage)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    @Then("the floater {string} should assist {int} teams")
    @Transactional
    public void verifyFloaterAssistsTeams(String studentCode, int count) {
        List<Floater> floaters = floaterRepository.findByStudentCode(studentCode);
        assertFalse(floaters.isEmpty(), "Floater not found");
        assertEquals(count, floaters.get(0).getAssistedTeams().size());
    }

    @When("I remove the floater {string} from team {string}")
    @Transactional
    public void removeFloaterFromTeam(String studentCode, String teamName) {
        List<Floater> floaters = floaterRepository.findByStudentCode(studentCode);
        Team team = teamRepository.findByName(teamName).orElseThrow();

        if (!floaters.isEmpty()) {
            team.removeFloater(floaters.get(0));
            teamRepository.save(team);
        }
    }
}


