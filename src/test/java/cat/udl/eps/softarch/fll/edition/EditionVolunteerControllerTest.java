package cat.udl.eps.softarch.fll.edition;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import cat.udl.eps.softarch.fll.controller.edition.EditionVolunteerController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import cat.udl.eps.softarch.fll.controller.edition.dto.EditionVolunteersResponse;
import cat.udl.eps.softarch.fll.controller.volunteer.dto.VolunteerSummaryResponse;
import cat.udl.eps.softarch.fll.exception.EditionVolunteerException;
import cat.udl.eps.softarch.fll.handler.EditionVolunteerExceptionHandler;
import cat.udl.eps.softarch.fll.service.EditionVolunteerService;

class EditionVolunteerControllerTest {

	private EditionVolunteerService editionVolunteerService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		editionVolunteerService = mock(EditionVolunteerService.class);
		EditionVolunteerController controller = new EditionVolunteerController(editionVolunteerService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.setControllerAdvice(new EditionVolunteerExceptionHandler())
			.build();
	}

	@Test
	void getVolunteersGroupedByTypeReturnsGroupedResponse() throws Exception {
		EditionVolunteersResponse response = new EditionVolunteersResponse(
			List.of(new VolunteerSummaryResponse(1L, "Ref One", "ref@example.com", "111111111")),
			List.of(new VolunteerSummaryResponse(2L, "Judge One", "judge@example.com", "222222222")),
			List.of(new VolunteerSummaryResponse(3L, "Floater One", "floater@example.com", "333333333")));

		when(editionVolunteerService.getVolunteersGroupedByType(1L)).thenReturn(response);

		mockMvc.perform(get("/editions/1/volunteers"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.referees[0].id").value(1))
			.andExpect(jsonPath("$.judges[0].name").value("Judge One"))
			.andExpect(jsonPath("$.floaters[0].emailAddress").value("floater@example.com"));
	}

	@Test
	void getVolunteersGroupedByTypeReturnsNotFoundWhenEditionDoesNotExist() throws Exception {
		when(editionVolunteerService.getVolunteersGroupedByType(99L))
			.thenThrow(new EditionVolunteerException("EDITION_NOT_FOUND", "Edition with id 99 not found"));

		mockMvc.perform(get("/editions/99/volunteers"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("EDITION_NOT_FOUND"))
			.andExpect(jsonPath("$.message").value("Edition with id 99 not found"))
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.path").value("/editions/99/volunteers"));
	}
}
