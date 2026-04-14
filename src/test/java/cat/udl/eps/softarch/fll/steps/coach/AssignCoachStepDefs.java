package cat.udl.eps.softarch.fll.steps.coach;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import cat.udl.eps.softarch.fll.steps.app.AuthenticationStepDefs;
import cat.udl.eps.softarch.fll.steps.app.StepDefs;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import cat.udl.eps.softarch.fll.domain.Coach;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.team.CoachRepository;
import cat.udl.eps.softarch.fll.repository.team.TeamRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class AssignCoachStepDefs {

	private final StepDefs stepDefs;
	private final TeamRepository teamRepository;
	private final CoachRepository coachRepository;

	private final Map<String, Long> teamIdMap = new HashMap<>();
	private final Map<Integer, Integer> coachIdMap = new HashMap<>();

	public AssignCoachStepDefs(StepDefs stepDefs,
							   TeamRepository teamRepository,
							   CoachRepository coachRepository) {
		this.stepDefs = stepDefs;
		this.teamRepository = teamRepository;
		this.coachRepository = coachRepository;

	}

	@Before
	public void setUp() {
		stepDefs.result = null;
	}

	private ResultActions performAssignCoach(String teamId, Integer coachId) throws Exception {
		return stepDefs.mockMvc.perform(
			post("/teams/assign-coach")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"teamId\":\"" + teamId + "\",\"coachId\":" + coachId + "}")
				.characterEncoding(StandardCharsets.UTF_8)
				.accept(MediaType.APPLICATION_JSON)
				.with(AuthenticationStepDefs.authenticate())
		);
	}

	@Given("a team {string} exists")
	public void aTeamExists(String teamName) {
		Team team = Team.create(teamName, "Lleida", 2020, "FLL");
		team.setEducationalCenter("School");
		teamRepository.save(team);
		teamIdMap.put(teamName, team.getId());
	}

	@Given("a coach with id {int} exists")
	public void aCoachExists(Integer id) {
		Coach coach = new Coach();
		coach.setName("Coach" + id);
		coach.setEmailAddress("coach" + id + "@mail.com");
		coach.setPhoneNumber("123456789");
		coachRepository.save(coach);
		coachIdMap.put(id, coach.getId());
	}

	@Given("coach {int} is assigned to team {string}")
	public void coachAssignedToTeam(Integer coachId, String teamName) throws Exception {
		Long teamId = teamIdMap.get(teamName);
		performAssignCoach(String.valueOf(teamId), coachId).andExpect(status().isOk());
	}

	@When("I assign coach {int} to team {string}")
	public void assignCoach(Integer coachId, String teamName) throws Exception {
		Long teamId = teamIdMap.get(teamName);
		Integer persistedCoachId = coachIdMap.getOrDefault(coachId, coachId);
		stepDefs.result = performAssignCoach(String.valueOf(teamId), persistedCoachId);
	}

	@Then("the assignment is successful")
	public void assignmentSuccessful() throws Exception {
		stepDefs.result.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ASSIGNED"));
	}

	@Then("the error {word} is returned")
	public void errorReturned(String error) throws Exception {
		int expectedStatus = switch (error) {
			case "TEAM_NOT_FOUND", "COACH_NOT_FOUND" -> 404;
			case "COACH_ALREADY_ASSIGNED",
				"MAX_COACHES_PER_TEAM_REACHED",
				"MAX_TEAMS_PER_COACH_REACHED" -> 409;
			default -> 400;
		};

		stepDefs.result.andExpect(status().is(expectedStatus))
			.andExpect(jsonPath("$.error").value(error))
			.andExpect(jsonPath("$.message").exists())
			.andExpect(jsonPath("$.timestamp").exists())
			.andExpect(jsonPath("$.path").value("/teams/assign-coach"));
	}
}