package cat.udl.eps.softarch.demo.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import cat.udl.eps.softarch.demo.domain.Team;
import cat.udl.eps.softarch.demo.domain.TeamMember;
import cat.udl.eps.softarch.demo.repository.TeamMemberRepository;
import cat.udl.eps.softarch.demo.repository.TeamRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class TeamSteps {

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private TeamMemberRepository teamMemberRepository;

	private Team currentTeam;
	private Exception lastException;
	private List<Team> searchResults;

	@Given("the system is empty")
	public void clearDB() {
		teamMemberRepository.deleteAll();
		teamRepository.deleteAll();
	}

	@Given("I create a team named {string} from {string}")
	public void iCreateATeam(String name, String city) {
		currentTeam = new Team(name);
		currentTeam.setCity(city);
		currentTeam.setFoundationYear(2000);
		currentTeam.setCategory("Challenge");
	}

	@Given("I add a member named {string} with role {string}")
	public void iAddAMember(String name, String role) {
		TeamMember member = new TeamMember();
		member.setName(name);
		member.setRole(role);
		member.setBirthDate(java.time.LocalDate.of(2010, 1, 1));
		currentTeam.addMember(member);
	}

	@When("I save the team")
	public void iSaveTheTeam() {
		currentTeam = teamRepository.save(currentTeam);
	}

	@Then("the team {string} should exist in the system")
	public void theTeamShouldExist(String name) {
		assertTrue(teamRepository.existsById(name));
	}

	@Then("the team should have {int} members")
	@Transactional
	public void theTeamShouldHaveMembers(int count) {
		Team savedTeam = teamRepository.findById(currentTeam.getName()).orElseThrow();
		assertEquals(count, savedTeam.getMembers().size());
	}

	@When("I try to create an invalid team named {string} from {string}")
	public void tryCreateInvalid(String name, String city) {
		try {
			Team team = new Team(name);
			team.setCity(city);
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
		currentTeam = new Team(name);
		currentTeam.setCity("Igualada");
		currentTeam.setFoundationYear(2000);
		currentTeam.setCategory("Challenge");

		IntStream.range(0, count).forEach(i -> {
			TeamMember m = new TeamMember();
			m.setName("Member " + i);
			m.setRole("Student"); // El rol és obligatori
			m.setBirthDate(java.time.LocalDate.of(2010, 1, 1)); // La data és obligatòria
			currentTeam.addMember(m);
		});
		teamRepository.save(currentTeam);
	}

	@When("I try to add another member")
	@Transactional
	public void tryAddExtraMember() {
		try {
			Team loadedTeam = teamRepository.findById(currentTeam.getName()).orElseThrow();
			TeamMember extra = new TeamMember();
			extra.setName("Extra Member");
			extra.setRole("Substitute"); // Afegit
			extra.setBirthDate(java.time.LocalDate.of(2012, 5, 5)); // Afegit

			loadedTeam.addMember(extra);
			teamRepository.save(loadedTeam);
		} catch (Exception e) {
			lastException = e;
		}
	}

	@Then("I should receive an error {string}")
	public void verifyErrorMessage(String expectedMessage) {
		assertNotNull(lastException);
		assertTrue(lastException.getMessage().contains(expectedMessage)
				|| (lastException.getCause() != null
						&& lastException.getCause().getMessage().contains(expectedMessage)),
				"Error rebut incorrecte: " + lastException.getMessage());
	}

	@Given("I have a team {string} with a member {string}")
	public void createTeamWithOneMember(String teamName, String memberName) {
		iCreateATeam(teamName, "Igualada");
		iAddAMember(memberName, "Captain");
		iSaveTheTeam();
	}

	@When("I delete the team {string}")
	public void deleteTeam(String name) {
		teamRepository.deleteById(name);
	}

	@Then("the team {string} should not exist")
	public void teamShouldNotExist(String name) {
		assertFalse(teamRepository.existsById(name));
	}

	@Then("no members should exist in the system")
	public void noMembers() {
		assertEquals(0, teamMemberRepository.count());
	}

	@When("I change the city to {string}")
	public void iChangeTheCity(String newCity) {
		currentTeam = teamRepository.findById(currentTeam.getName()).orElseThrow();
		currentTeam.setCity(newCity);
		teamRepository.save(currentTeam);
	}

	@Then("the team {string} should be in {string}")
	public void verifyTeamCity(String name, String expectedCity) {
		Team t = teamRepository.findById(name).orElseThrow();
		assertEquals(expectedCity, t.getCity());
	}

	@When("I search for teams in {string}")
	public void searchByCity(String city) {
		searchResults = teamRepository.findByCity(city);
	}

	@Then("I should find {int} team in the list")
	public void verifySearchResults(int count) {
		assertEquals(count, searchResults.size());
	}
}
