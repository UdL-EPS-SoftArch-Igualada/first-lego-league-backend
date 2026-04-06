package cat.udl.eps.softarch.fll.edition;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cat.udl.eps.softarch.fll.controller.edition.EditionLifecycleController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import cat.udl.eps.softarch.fll.exception.DomainValidationExceptionHandler;
import cat.udl.eps.softarch.fll.domain.EditionState;
import cat.udl.eps.softarch.fll.exception.EditionLifecycleException;
import cat.udl.eps.softarch.fll.handler.EditionLifecycleExceptionHandler;
import cat.udl.eps.softarch.fll.service.EditionLifecycleService;

class EditionLifecycleControllerTest {

	private EditionLifecycleService editionLifecycleService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		editionLifecycleService = mock(EditionLifecycleService.class);
		EditionLifecycleController controller = new EditionLifecycleController(editionLifecycleService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.setControllerAdvice(new DomainValidationExceptionHandler(), new EditionLifecycleExceptionHandler())
			.build();
	}

	@Test
	void changeStateShouldReturnUpdatedResponse() throws Exception {
		when(editionLifecycleService.changeState(5L, EditionState.OPEN))
			.thenReturn(new EditionLifecycleService.TransitionResult(5L, EditionState.DRAFT, EditionState.OPEN));

		mockMvc.perform(patch("/editions/5/state")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"state\":\"OPEN\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.editionId").value(5))
			.andExpect(jsonPath("$.previousState").value("DRAFT"))
			.andExpect(jsonPath("$.newState").value("OPEN"))
			.andExpect(jsonPath("$.status").value("UPDATED"));
	}

	@Test
	void changeStateShouldReturnConflictForInvalidTransition() throws Exception {
		when(editionLifecycleService.changeState(5L, EditionState.OPEN))
			.thenThrow(new EditionLifecycleException(
				"INVALID_EDITION_STATE_TRANSITION",
				"Invalid transition from CLOSED to OPEN"));

		mockMvc.perform(patch("/editions/5/state")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"state\":\"OPEN\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error").value("INVALID_EDITION_STATE_TRANSITION"))
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.path").value("/editions/5/state"));
	}

	@Test
	void changeStateShouldReturnNotFoundWhenEditionMissing() throws Exception {
		when(editionLifecycleService.changeState(5L, EditionState.OPEN))
			.thenThrow(new EditionLifecycleException("EDITION_NOT_FOUND", "Edition with id 5 not found"));

		mockMvc.perform(patch("/editions/5/state")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"state\":\"OPEN\"}"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("EDITION_NOT_FOUND"))
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.path").value("/editions/5/state"));
	}

	@Test
	void changeStateShouldReturnBadRequestWhenStateIsMissing() throws Exception {
		mockMvc.perform(patch("/editions/5/state")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("INVALID_EDITION_STATE_REQUEST"))
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.path").value("/editions/5/state"));
	}

	@Test
	void changeStateShouldReturnBadRequestWhenStateIsInvalid() throws Exception {
		mockMvc.perform(patch("/editions/5/state")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"state\":\"INVALID\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("INVALID_EDITION_STATE_REQUEST"))
			.andExpect(jsonPath("$.message").value("Invalid state value. Allowed values: [DRAFT, OPEN, CLOSED]"))
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.path").value("/editions/5/state"));
	}

	@Test
	void changeStateShouldReturnStableMessageWhenJsonIsMalformed() throws Exception {
		mockMvc.perform(patch("/editions/5/state")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"state\":"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("INVALID_EDITION_STATE_REQUEST"))
			.andExpect(jsonPath("$.message").value("Invalid request body"));
	}
}
