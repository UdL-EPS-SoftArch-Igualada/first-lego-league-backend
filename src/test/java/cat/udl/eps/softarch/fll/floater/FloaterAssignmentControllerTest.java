package cat.udl.eps.softarch.fll.floater;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cat.udl.eps.softarch.fll.controller.team.TeamFloaterController;
import cat.udl.eps.softarch.fll.dto.AssignFloaterResponse;
import cat.udl.eps.softarch.fll.exception.TeamFloaterAssignmentException;
import cat.udl.eps.softarch.fll.service.floater.FloaterAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FloaterAssignmentControllerTest {

	private FloaterAssignmentService floaterAssignmentService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		floaterAssignmentService = mock(FloaterAssignmentService.class);
		TeamFloaterController controller = new TeamFloaterController(floaterAssignmentService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	void assignFloaterShouldReturnResponse() throws Exception {
		when(floaterAssignmentService.assignFloater("TeamA", 42L))
			.thenReturn(new AssignFloaterResponse("TeamA", 42L));

		mockMvc.perform(post("/teams/assign-floater")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"teamId\":\"TeamA\",\"floaterId\":42}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.teamId").value("TeamA"))
			.andExpect(jsonPath("$.floaterId").value(42));
	}

	@Test
	void assignFloaterShouldReturnBadRequestWhenFloaterDoesNotExist() throws Exception {
		when(floaterAssignmentService.assignFloater("TeamA", 42L))
			.thenThrow(new TeamFloaterAssignmentException("FLOATER_NOT_FOUND", "Floater not found"));

		mockMvc.perform(post("/teams/assign-floater")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"teamId\":\"TeamA\",\"floaterId\":42}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("FLOATER_NOT_FOUND"))
			.andExpect(jsonPath("$.path").value("/teams/assign-floater"));
	}
}
