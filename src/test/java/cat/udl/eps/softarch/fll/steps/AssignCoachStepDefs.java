package cat.udl.eps.softarch.fll.steps;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import cat.udl.eps.softarch.fll.domain.Coach;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.CoachRepository;
import cat.udl.eps.softarch.fll.repository.TeamRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;

public class AssignCoachStepDefs {

	private StepDefs stepDefs;
	private TeamRepository teamRepository;
	private CoachRepository coachRepository;
	private ObjectMapper mapper;

	private Map<String, String> teamIdMap = new HashMap<>();
	private Map<Integer, Integer> coachIdMap = new HashMap<>();

	public AssignCoachStepDefs(StepDefs stepDefs,
							   TeamRepository teamRepository,
							   CoachRepository coachRepository) {
		this.stepDefs = stepDefs;
		this.teamRepository = teamRepository;
		this.coachRepository = coachRepository;
		this.mapper = new ObjectMapper();

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
				.with(user("testuser").roles("COACH"))
		);
	}

	@Given("a team {string} exists")
	public void aTeamExists(String teamName) {
		Team team = new Team();
		team.setName(teamName);
		team.setCity("Lleida");
		team.setFoundationYear(2020);
		team.setCategory("FLL");
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
		String teamId = teamIdMap.get(teamName);
		performAssignCoach(teamId, coachId).andExpect(status().isOk());
	}

	@When("I assign coach {int} to team {string}")
	public void assignCoach(Integer coachId, String teamName) throws Exception {
		String teamId = teamIdMap.get(teamName);
		Integer persistedCoachId = coachIdMap.getOrDefault(coachId, coachId);
		stepDefs.result = performAssignCoach(teamId, persistedCoachId);
	}

	@Then("the assignment is successful")
	public void assignmentSuccessful() throws Exception {
		stepDefs.result.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ASSIGNED"));
	}

	@Then("the error {word} is returned")
	public void errorReturned(String error) throws Exception {
		int expectedStatus = switch (error) {
			case "COACH_ALREADY_ASSIGNED",
				"MAX_COACHES_PER_TEAM_REACHED",
				"MAX_TEAMS_PER_COACH_REACHED" -> 409;
			default -> 400;
		};

		stepDefs.result.andExpect(status().is(expectedStatus))
			.andExpect(jsonPath("$.error").value(error));
	}
}