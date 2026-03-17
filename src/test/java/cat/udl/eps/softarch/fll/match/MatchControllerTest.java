package cat.udl.eps.softarch.fll.match;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cat.udl.eps.softarch.fll.controller.MatchController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.server.ResponseStatusException;
import cat.udl.eps.softarch.fll.controller.dto.MatchTableAssignmentResponse;
import cat.udl.eps.softarch.fll.exception.GlobalExceptionHandler;
import cat.udl.eps.softarch.fll.service.MatchTableAssignmentService;

class MatchControllerTest {

	private MatchTableAssignmentService matchTableAssignmentService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		matchTableAssignmentService = mock(MatchTableAssignmentService.class);
		MatchController controller = new MatchController(matchTableAssignmentService);
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();

		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.build();
	}

	@Test
	void assignTableReturnsOk() throws Exception {
		MatchTableAssignmentResponse response = new MatchTableAssignmentResponse(15L, "Table-1", "11:00", "11:20");
		when(matchTableAssignmentService.assignTable(15L, "Table-1")).thenReturn(response);

		mockMvc.perform(post("/matches/15/assign-table")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tableIdentifier\":\"Table-1\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.matchId").value(15))
				.andExpect(jsonPath("$.tableIdentifier").value("Table-1"))
				.andExpect(jsonPath("$.startTime").value("11:00"))
				.andExpect(jsonPath("$.endTime").value("11:20"));
	}

	@Test
	void assignTableReturnsNotFound() throws Exception {
		when(matchTableAssignmentService.assignTable(15L, "Table-1")).thenThrow(
				new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found: Table-1"));

		mockMvc.perform(post("/matches/15/assign-table")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tableIdentifier\":\"Table-1\"}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void assignTableReturnsConflict() throws Exception {
		when(matchTableAssignmentService.assignTable(15L, "Table-1")).thenThrow(
				new ResponseStatusException(HttpStatus.CONFLICT, "Table has overlapping scheduled match"));

		mockMvc.perform(post("/matches/15/assign-table")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tableIdentifier\":\"Table-1\"}"))
				.andExpect(status().isConflict());
	}

	@Test
	void assignTableReturnsUnprocessableContentWhenBodyInvalid() throws Exception {
		mockMvc.perform(post("/matches/15/assign-table")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tableIdentifier\":\"\"}"))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.message").isNotEmpty())
				.andExpect(jsonPath("$.timestamp").isNotEmpty())
				.andExpect(jsonPath("$.path").value("/matches/15/assign-table"));
}
}
