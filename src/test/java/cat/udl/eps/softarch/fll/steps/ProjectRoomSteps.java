package cat.udl.eps.softarch.fll.steps;

import cat.udl.eps.softarch.fll.domain.Judge;
import cat.udl.eps.softarch.fll.domain.ProjectRoom;
import cat.udl.eps.softarch.fll.repository.JudgeRepository;
import cat.udl.eps.softarch.fll.repository.ProjectRoomRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectRoomSteps {

	private final StepDefs stepDefs;
	private final ProjectRoomRepository roomRepository;
	private final JudgeRepository judgeRepository;
	private final Map<String, Long> judgeIdMap = new HashMap<>();

	public ProjectRoomSteps(StepDefs stepDefs, ProjectRoomRepository roomRepository, JudgeRepository judgeRepository) {
		this.stepDefs = stepDefs;
		this.roomRepository = roomRepository;
		this.judgeRepository = judgeRepository;
	}

	@Given("a project room {string} exists")
	public void a_project_room_exists(String roomId) {
		ProjectRoom room = new ProjectRoom();
		room.setRoomNumber(roomId);
		room.setPanelists(new ArrayList<>());
		roomRepository.save(room);
	}

	@Given("a judge {string} exists")
	public void a_judge_exists(String judgeAlias) {
		Judge judge = Judge.create("Test Judge " + judgeAlias, "judge_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com", "123456789");
		judge = judgeRepository.save(judge);
		judgeIdMap.put(judgeAlias, judge.getId());
	}

	@Given("the room {string} already has a manager")
	public void the_room_already_has_a_manager(String roomId) {
		ProjectRoom room = roomRepository.findById(roomId).orElseThrow();
		Judge manager = Judge.create("Manager", "manager@test.com", "000");
		manager = judgeRepository.save(manager);
		room.setManagedByJudge(manager);
		roomRepository.save(room);
	}

	@Given("the room {string} already has {int} panelists")
	public void the_room_already_has_panelists(String roomId, int count) {
		ProjectRoom room = roomRepository.findById(roomId).orElseThrow();
		for (int i = 0; i < count; i++) {
			Judge panelist = Judge.create("Panelist " + i, "p" + UUID.randomUUID().toString().substring(0, 4) + "@test.com", "111");
			panelist.setMemberOfRoom(room);
			judgeRepository.save(panelist);
		}
	}

	@Given("judge {string} is already assigned to room {string}")
	public void judge_is_already_assigned_to_room(String judgeAlias, String roomId) {
		ProjectRoom room = roomRepository.findById(roomId).orElseThrow();
		Long realId = judgeIdMap.get(judgeAlias);
		Judge judge = judgeRepository.findById(realId).orElseThrow();
		judge.setMemberOfRoom(room);
		judgeRepository.save(judge);
	}

	@When("I request to assign judge {string} to room {string} with isManager {word}")
	public void i_request_to_assign_judge_to_room_with_is_manager(String judgeAlias, String roomId, String isManagerStr) throws Throwable {
		boolean isManager = Boolean.parseBoolean(isManagerStr);
		Long realJudgeId = judgeIdMap.getOrDefault(judgeAlias, -1L);

		String jsonPayload = String.format(
			"{\"roomId\": \"%s\", \"judgeId\": \"%s\", \"isManager\": %b}",
			roomId, realJudgeId, isManager
		);

		stepDefs.result = stepDefs.mockMvc.perform(post("/project-rooms/assign-judge")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonPayload)
			.with(user("admin").roles("ADMIN")));
	}

	@Then("the response status should be {int}")
	public void the_response_status_should_be(int expectedStatus) throws Throwable {
		stepDefs.result.andExpect(status().is(expectedStatus));
	}

	@Then("the response role should be {string}")
	public void the_response_role_should_be(String expectedRole) throws Throwable {
		stepDefs.result.andExpect(jsonPath("$.role").value(expectedRole))
			.andExpect(jsonPath("$.status").value("ASSIGNED"));
	}

	@Then("the response error should be {string}")
	public void the_response_error_should_be(String expectedError) throws Throwable {
		stepDefs.result.andExpect(jsonPath("$.error").value(expectedError));
	}
}