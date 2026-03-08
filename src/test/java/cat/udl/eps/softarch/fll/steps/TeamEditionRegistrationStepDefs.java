package cat.udl.eps.softarch.fll.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import cat.udl.eps.softarch.fll.domain.Edition;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.EditionRepository;
import cat.udl.eps.softarch.fll.repository.TeamRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class TeamEditionRegistrationStepDefs {

	private final StepDefs stepDefs;
	private final ManageEditionStepDefs manageEditionStepDefs;
	private final EditionRepository editionRepository;
	private final TeamRepository teamRepository;

	private List<Integer> concurrentResponseCodes;

	public TeamEditionRegistrationStepDefs(StepDefs stepDefs,
			ManageEditionStepDefs manageEditionStepDefs,
			EditionRepository editionRepository,
			TeamRepository teamRepository) {
		this.stepDefs = stepDefs;
		this.manageEditionStepDefs = manageEditionStepDefs;
		this.editionRepository = editionRepository;
		this.teamRepository = teamRepository;
	}

	@Given("There is a team named {string} from {string} with category {string}")
	public void thereIsATeam(String name, String city, String category) {
		if (!teamRepository.existsById(name)) {
			Team team = new Team(name);
			team.setCity(city);
			team.setFoundationYear(2000);
			team.setCategory(category);
			teamRepository.save(team);
		}
	}

	@Given("Team {string} is already registered in the current edition")
	@Transactional
	public void teamIsAlreadyRegistered(String teamName) {
		Edition edition = editionRepository.findById(currentEditionId()).orElseThrow();
		Team team = teamRepository.findById(teamName).orElseThrow();
		edition.getTeams().add(team);
		editionRepository.save(edition);
	}

	@Given("The current edition already has {int} teams registered")
	@Transactional
	public void editionHasTeamsRegistered(int count) {
		Edition edition = editionRepository.findById(currentEditionId()).orElseThrow();
		IntStream.range(0, count).forEach(i -> {
			String teamName = "FillerTeam_" + edition.getId() + "_" + i;
			Team team = teamRepository.findById(teamName).orElseGet(() -> {
				Team created = new Team(teamName);
				created.setCity("Igualada");
				created.setFoundationYear(2000);
				created.setCategory("Challenge");
				return teamRepository.save(created);
			});
			edition.getTeams().add(team);
		});
		editionRepository.save(edition);
	}

	@When("I register team {string} to the current edition")
	public void iRegisterTeamToCurrentEdition(String teamName) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				post("/editions/" + currentEditionId() + "/teams/" + teamName)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}

	@When("I register team {string} to edition with id {long}")
	public void iRegisterTeamToEditionById(String teamName, Long editionId) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				post("/editions/" + editionId + "/teams/" + teamName)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}

	@When("I register teams {string} and {string} concurrently to the current edition")
	public void iRegisterTeamsConcurrently(String teamA, String teamB) throws Exception {
		Long editionId = currentEditionId();
		MockMvc mockMvc = stepDefs.mockMvc;
		CyclicBarrier barrier = new CyclicBarrier(2);

		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			Future<Integer> futureA = executor.submit(() -> performRegistration(mockMvc, editionId, teamA, barrier));
			Future<Integer> futureB = executor.submit(() -> performRegistration(mockMvc, editionId, teamB, barrier));
			concurrentResponseCodes = Arrays.asList(futureA.get(), futureB.get());
		} finally {
			executor.shutdown();
		}
	}

	@Then("One registration succeeds with code {int} and the other fails with code {int}")
	public void oneSucceedsAndOtherFails(int successCode, int failCode) {
		assertThat("Expected exactly 2 responses", concurrentResponseCodes.size(), is(2));
		List<Integer> expected = Arrays.asList(successCode, failCode).stream().sorted().toList();
		List<Integer> actual = concurrentResponseCodes.stream().sorted().toList();
		assertThat("Expected one success and one failure", actual, is(expected));
	}

	@And("The response has status {string}")
	public void theResponseHasStatus(String status) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.status", is(status)));
	}

	@And("The response has error {string}")
	public void theResponseHasError(String error) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.error", is(error)));
	}

	@And("The response has a non-empty message")
	public void theResponseHasNonEmptyMessage() throws Exception {
		stepDefs.result.andExpect(jsonPath("$.message").isNotEmpty());
	}

	private Long currentEditionId() {
		String uri = manageEditionStepDefs.editionUri;
		return Long.parseLong(uri.substring(uri.lastIndexOf('/') + 1));
	}

	private int performRegistration(MockMvc mockMvc, Long editionId, String teamName,
			CyclicBarrier barrier) throws Exception {
		barrier.await();
		MvcResult result = mockMvc.perform(
				post("/editions/" + editionId + "/teams/" + teamName)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andReturn();
		return result.getResponse().getStatus();
	}
}