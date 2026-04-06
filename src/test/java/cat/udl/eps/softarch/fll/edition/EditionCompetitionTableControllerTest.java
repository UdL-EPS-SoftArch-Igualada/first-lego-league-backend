package cat.udl.eps.softarch.fll.edition;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;

import cat.udl.eps.softarch.fll.controller.edition.EditionCompetitionTableController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import cat.udl.eps.softarch.fll.controller.edition.dto.EditionCompetitionTableResponse;
import cat.udl.eps.softarch.fll.controller.edition.dto.EditionTableMatchResponse;
import cat.udl.eps.softarch.fll.exception.EditionCompetitionTableNotFoundException;
import cat.udl.eps.softarch.fll.service.EditionCompetitionTableService;

class EditionCompetitionTableControllerTest {

	private EditionCompetitionTableService editionCompetitionTableService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		editionCompetitionTableService = mock(EditionCompetitionTableService.class);
		EditionCompetitionTableController controller = new EditionCompetitionTableController(editionCompetitionTableService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	void getCompetitionTablesReturnsTableOverview() throws Exception {
		List<EditionCompetitionTableResponse> response = List.of(
			new EditionCompetitionTableResponse(
				"Table-1",
				List.of(
					new EditionTableMatchResponse(10L, "11:00", "11:20"),
					new EditionTableMatchResponse(11L, "11:30", "11:50"))));

		when(editionCompetitionTableService.getTablesByEdition(1L)).thenReturn(response);

		mockMvc.perform(get("/editions/1/tables"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].identifier").value("Table-1"))
			.andExpect(jsonPath("$[0].matches[0].matchId").value(10))
			.andExpect(jsonPath("$[0].matches[1].endTime").value("11:50"));
	}

	@Test
	void getCompetitionTablesReturnsEmptyListWhenNoTablesExist() throws Exception {
		when(editionCompetitionTableService.getTablesByEdition(2L)).thenReturn(List.of());

		mockMvc.perform(get("/editions/2/tables"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void getCompetitionTablesReturnsNotFoundWhenEditionDoesNotExist() throws Exception {
		when(editionCompetitionTableService.getTablesByEdition(99L))
			.thenThrow(new EditionCompetitionTableNotFoundException());

		mockMvc.perform(get("/editions/99/tables"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("EDITION_NOT_FOUND"));
	}
}
