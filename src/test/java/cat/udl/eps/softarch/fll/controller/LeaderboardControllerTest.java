package cat.udl.eps.softarch.fll.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.server.ResponseStatusException;
import cat.udl.eps.softarch.fll.controller.dto.LeaderboardItemResponse;
import cat.udl.eps.softarch.fll.controller.dto.LeaderboardPageResponse;
import cat.udl.eps.softarch.fll.service.LeaderboardService;

class LeaderboardControllerTest {

	private LeaderboardService leaderboardService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		leaderboardService = mock(LeaderboardService.class);
		LeaderboardController controller = new LeaderboardController(leaderboardService);
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setValidator(validator)
				.build();
	}

	@Test
	void getEditionLeaderboardReturnsOkWithPayload() throws Exception {
		LeaderboardPageResponse response = new LeaderboardPageResponse(
				2025L,
				0,
				10,
				2,
				List.of(
						new LeaderboardItemResponse(1, "TeamA", "TeamA", 560L, 3L),
						new LeaderboardItemResponse(2, "TeamB", "TeamB", 500L, 3L)));

		when(leaderboardService.getEditionLeaderboard(2025L, 0, 10)).thenReturn(response);

		mockMvc.perform(get("/leaderboards/editions/2025")
				.param("page", "0")
				.param("size", "10")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.editionId").value(2025))
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].position").value(1))
				.andExpect(jsonPath("$.items[0].teamName").value("TeamA"));
	}

	@Test
	void getEditionLeaderboardReturnsNotFoundForMissingEdition() throws Exception {
		when(leaderboardService.getEditionLeaderboard(999999L, 0, 10))
				.thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Edition not found: 999999"));

		mockMvc.perform(get("/leaderboards/editions/999999")
				.param("page", "0")
				.param("size", "10"))
				.andExpect(status().isNotFound());
	}

	@Test
	void getEditionLeaderboardRespectsPaginationParameters() throws Exception {
		LeaderboardPageResponse response = new LeaderboardPageResponse(
				2025L,
				1,
				1,
				3,
				List.of(new LeaderboardItemResponse(2, "TeamB", "TeamB", 500L, 3L)));

		when(leaderboardService.getEditionLeaderboard(2025L, 1, 1)).thenReturn(response);

		mockMvc.perform(get("/leaderboards/editions/2025")
				.param("page", "1")
				.param("size", "1")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").value(1))
				.andExpect(jsonPath("$.size").value(1))
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].position").value(2))
				.andExpect(jsonPath("$.items[0].teamName").value("TeamB"));
	}

}
