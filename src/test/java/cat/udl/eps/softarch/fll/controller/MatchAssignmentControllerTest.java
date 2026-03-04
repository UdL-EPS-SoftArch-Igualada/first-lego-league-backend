package cat.udl.eps.softarch.fll.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import cat.udl.eps.softarch.fll.controller.dto.BatchMatchAssignmentItemRequest;
import cat.udl.eps.softarch.fll.controller.dto.BatchMatchAssignmentItemResponse;
import cat.udl.eps.softarch.fll.controller.dto.BatchMatchAssignmentResponse;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.Referee;
import cat.udl.eps.softarch.fll.exception.MatchAssignmentErrorCode;
import cat.udl.eps.softarch.fll.exception.MatchAssignmentException;
import cat.udl.eps.softarch.fll.exception.MatchAssignmentExceptionHandler;
import cat.udl.eps.softarch.fll.service.MatchAssignmentService;

class MatchAssignmentControllerTest {

	private MatchAssignmentService matchAssignmentService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		matchAssignmentService = mock(MatchAssignmentService.class);
		MatchAssignmentController controller = new MatchAssignmentController(matchAssignmentService);
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();

		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new MatchAssignmentExceptionHandler())
				.setValidator(validator)
				.build();
	}

	@Test
	void assignRefereeReturnsAssignedResponse() throws Exception {
		Match match = new Match();
		match.setId(1L);
		Referee referee = new Referee();
		referee.setId(2L);
		match.setReferee(referee);

		when(matchAssignmentService.assignReferee("1", "2")).thenReturn(match);

		mockMvc.perform(post("/matchAssignments/assign")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"matchId\":\"1\",\"refereeId\":\"2\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.matchId").value("1"))
				.andExpect(jsonPath("$.refereeId").value("2"))
				.andExpect(jsonPath("$.status").value("ASSIGNED"));
	}

	@Test
	void assignRefereeReturnsErrorResponse() throws Exception {
		when(matchAssignmentService.assignReferee("1", "2")).thenThrow(
				new MatchAssignmentException(
						MatchAssignmentErrorCode.AVAILABILITY_CONFLICT,
						"Referee is already assigned to another overlapping match"));

		mockMvc.perform(post("/matchAssignments/assign")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"matchId\":\"1\",\"refereeId\":\"2\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("AVAILABILITY_CONFLICT"))
				.andExpect(jsonPath("$.message")
						.value("Referee is already assigned to another overlapping match"));
	}

	@Test
	void assignBatchReturnsAssignedResponse() throws Exception {
		BatchMatchAssignmentResponse response = new BatchMatchAssignmentResponse(
				"3",
				"ASSIGNED",
				2,
				List.of(
						new BatchMatchAssignmentItemResponse("10", "20", "ASSIGNED"),
						new BatchMatchAssignmentItemResponse("11", "21", "ASSIGNED")));

		when(matchAssignmentService.assignBatch(
				"3",
				List.of(
						new BatchMatchAssignmentItemRequest("10", "20"),
						new BatchMatchAssignmentItemRequest("11", "21")))).thenReturn(response);

		mockMvc.perform(post("/matchAssignments/batch")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "roundId": "3",
						  "assignments": [
						    {"matchId":"10","refereeId":"20"},
						    {"matchId":"11","refereeId":"21"}
						  ]
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.roundId").value("3"))
				.andExpect(jsonPath("$.status").value("ASSIGNED"))
				.andExpect(jsonPath("$.processed").value(2))
				.andExpect(jsonPath("$.assignments[0].matchId").value("10"))
				.andExpect(jsonPath("$.assignments[1].refereeId").value("21"));
	}

	@Test
	void assignBatchReturnsDetailedErrorResponse() throws Exception {
		when(matchAssignmentService.assignBatch(
				"3",
				List.of(
						new BatchMatchAssignmentItemRequest("10", "20"),
						new BatchMatchAssignmentItemRequest("11", "20")))).thenThrow(
								new MatchAssignmentException(
										MatchAssignmentErrorCode.AVAILABILITY_CONFLICT,
										"Referee is assigned to overlapping matches in the same batch",
										1,
										"11",
										"20"));

		mockMvc.perform(post("/matchAssignments/batch")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "roundId": "3",
						  "assignments": [
						    {"matchId":"10","refereeId":"20"},
						    {"matchId":"11","refereeId":"20"}
						  ]
						}
						"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("BATCH_ASSIGNMENT_FAILED"))
				.andExpect(jsonPath("$.details.index").value(1))
				.andExpect(jsonPath("$.details.matchId").value("11"))
				.andExpect(jsonPath("$.details.refereeId").value("20"))
				.andExpect(jsonPath("$.details.cause").value("AVAILABILITY_CONFLICT"));
	}
}
