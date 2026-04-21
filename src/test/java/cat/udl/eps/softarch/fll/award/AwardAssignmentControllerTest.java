package cat.udl.eps.softarch.fll.award;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cat.udl.eps.softarch.fll.controller.award.AwardAssignmentController;
import cat.udl.eps.softarch.fll.dto.AssignAwardResponse;
import cat.udl.eps.softarch.fll.exception.AwardAssignmentException;
import cat.udl.eps.softarch.fll.service.award.AwardAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AwardAssignmentControllerTest {

	private AwardAssignmentService awardAssignmentService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		awardAssignmentService = mock(AwardAssignmentService.class);
		AwardAssignmentController controller = new AwardAssignmentController(awardAssignmentService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	void assignAwardShouldReturnAwardedResponse() throws Exception {
		when(awardAssignmentService.assignAward("21", "TeamA"))
			.thenReturn(new AssignAwardResponse("21", "TeamA", "AWARDED"));

		mockMvc.perform(post("/awards/assign")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"awardId\":\"21\",\"teamId\":\"TeamA\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.awardId").value("21"))
			.andExpect(jsonPath("$.teamId").value("TeamA"))
			.andExpect(jsonPath("$.status").value("AWARDED"));
	}

	@Test
	void assignAwardShouldReturnNotFoundWhenAwardDoesNotExist() throws Exception {
		when(awardAssignmentService.assignAward("21", "TeamA"))
			.thenThrow(new AwardAssignmentException("AWARD_NOT_FOUND", "Award with id 21 not found"));

		mockMvc.perform(post("/awards/assign")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"awardId\":\"21\",\"teamId\":\"TeamA\"}"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("AWARD_NOT_FOUND"))
			.andExpect(jsonPath("$.path").value("/awards/assign"));
	}
}
