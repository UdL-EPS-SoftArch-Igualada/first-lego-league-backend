package cat.udl.eps.softarch.fll.steps.team;

import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.domain.TeamMember;
import cat.udl.eps.softarch.fll.repository.team.TeamMemberRepository;
import cat.udl.eps.softarch.fll.repository.team.TeamRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TeamSteps {

	private final TeamRepository teamRepository;
	private final TeamMemberRepository teamMemberRepository;

	private Team currentTeam;
	private Exception lastException;
	private List<Team> searchResults;

	public TeamSteps(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository) {
		this.teamRepository = teamRepository;
		this.teamMemberRepository = teamMemberRepository;
	}

	@Given("the system is empty")
	public void clearDB() {
		teamMemberRepository.deleteAll();
		teamRepository.deleteAll();
		currentTeam = null;
		lastException = null;
		searchResults = null;
	}

	@Given("I create a team named {string} from {string}")
	public void iCreateATeam(String name, String city) {
		currentTeam = Team.create(name, city, 2000, "Challenge");
	}

	@Given("I add a member named {string} with role {string}")
	public void iAddAMember(String name, String role) {
		TeamMember.create(name, role, LocalDate.of(2010, 1, 1), currentTeam);
	}

	@When("I save the team")
	public void iSaveTheTeam() {
		currentTeam = teamRepository.save(currentTeam);
	}

	@Then("the team {string} should exist in the system")
	public void theTeamShouldExist(String name) {
		assertTrue((BooleanSupplier) teamRepository.findbyName(name));
	}

	@Then("the team should have {int} members")
	@Transactional
	public void theTeamShouldHaveMembers(int count) {
		Team savedTeam = teamRepository.findById(currentTeam.getId()).orElseThrow();
		assertEquals(count, savedTeam.getMembers().size());
	}

	@When("I try to create an invalid team named {string} from {string}")
	public void tryCreateInvalid(String name, String city) {
		try {
			Team team = Team.create(name, city, 2000, "Challenge");
			teamRepository.save(team);
		} catch (Exception e) {
			lastException = e;
		}
	}

	@Then("the system should reject the team with an error")
	public void checkError() {
		assertNotNull(lastException, "Expected an exception but none was thrown");
	}

	@Given("I have a team named {string} with {int} members")
	public void createTeamWithManyMembers(String name, int count) {
		currentTeam = Team.create(name, "Igualada", 2000, "Challenge");

		IntStream.range(0, count).forEach(i -> {
			TeamMember.create("Member " + i, "Student", LocalDate.of(2010, 1, 1), currentTeam);
		});
		teamRepository.save(currentTeam);
	}

	@When("I try to add another member")
	@Transactional
	public void tryAddExtraMember() {
		try {
			Team loadedTeam = teamRepository.findById(currentTeam.getId()).orElseThrow();
			TeamMember.create("Extra Member", "Substitute", LocalDate.of(2012, 5, 5), loadedTeam);
			teamRepository.save(loadedTeam);
		} catch (Exception e) {
			lastException = e;
		}
	}

	@Then("I should receive an error {string}")
	public void verifyErrorMessage(String expectedMessage) {
		assertNotNull(lastException, "No exception was captured");
		boolean found = false;
		Throwable current = lastException;
		while (current != null) {
			if (current.getMessage() != null && current.getMessage().contains(expectedMessage)) {
				found = true;
				break;
			}
			current = current.getCause();
		}
		assertTrue(found, "Error message '" + expectedMessage + "' not found in exception chain. Got: "
			+ lastException.getMessage());
	}

	@Given("I have a team {string} with a member {string}")
	public void createTeamWithOneMember(String teamName, String memberName) {
		iCreateATeam(teamName, "Igualada");
		iAddAMember(memberName, "Captain");
		iSaveTheTeam();
	}

	@When("I delete the team {string}")
	public void deleteTeam(Long name) {
		teamRepository.deleteById(name);
	}

	@Then("the team {string} should not exist")
	public void teamShouldNotExist(Long id) {
		assertFalse(teamRepository.existsById(id));
	}

	@Then("no members should exist in the system")
	public void verifyNoMembersExist() {
		assertEquals(0, teamMemberRepository.count());
	}

	@When("I change the city to {string}")
	public void iChangeTheCity(String newCity) {
		currentTeam = teamRepository.findById(currentTeam.getId()).orElseThrow();
		currentTeam.setCity(newCity);
		teamRepository.save(currentTeam);
	}

	@Then("the team {string} should be in {string}")
	public void verifyTeamCity(Long name, String expectedCity) {
		Team t = teamRepository.findById(name).orElseThrow();
		assertEquals(expectedCity, t.getCity());
	}

	@When("I search for teams in {string}")
	public void searchByCity(String city) {
		searchResults = teamRepository.findByCity(city);
	}

	@Then("I should find {int} team in the list")
	public void verifySearchResults(int count) {
		assertNotNull(searchResults, "Search results is null. Did you perform a search?");
		assertEquals(count, searchResults.size());
	}
}