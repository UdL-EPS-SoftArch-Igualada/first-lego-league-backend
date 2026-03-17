package cat.udl.eps.softarch.fll.steps.volunteer;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import java.util.UUID;

import cat.udl.eps.softarch.fll.steps.app.AuthenticationStepDefs;
import cat.udl.eps.softarch.fll.steps.app.StepDefs;
import org.springframework.http.MediaType;
import cat.udl.eps.softarch.fll.domain.Edition;
import cat.udl.eps.softarch.fll.domain.volunteer.Floater;
import cat.udl.eps.softarch.fll.domain.volunteer.Judge;
import cat.udl.eps.softarch.fll.domain.volunteer.Referee;
import cat.udl.eps.softarch.fll.repository.EditionRepository;
import cat.udl.eps.softarch.fll.repository.FloaterRepository;
import cat.udl.eps.softarch.fll.repository.JudgeRepository;
import cat.udl.eps.softarch.fll.repository.RefereeRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class EditionVolunteerStepDefs {

	private final StepDefs stepDefs;
	private final EditionRepository editionRepository;
	private final RefereeRepository refereeRepository;
	private final JudgeRepository judgeRepository;
	private final FloaterRepository floaterRepository;

	private Long targetEditionId;
	private String otherRefereeName;
	private String otherJudgeName;
	private String otherFloaterName;

	public EditionVolunteerStepDefs(
		StepDefs stepDefs,
		EditionRepository editionRepository,
		RefereeRepository refereeRepository,
		JudgeRepository judgeRepository,
		FloaterRepository floaterRepository) {
		this.stepDefs = stepDefs;
		this.editionRepository = editionRepository;
		this.refereeRepository = refereeRepository;
		this.judgeRepository = judgeRepository;
		this.floaterRepository = floaterRepository;
	}

	@Given("an edition volunteer overview dataset exists")
	public void anEditionVolunteerOverviewDatasetExists() {
		final Edition targetEdition = createEdition("Target venue", "Target edition");
		final Edition otherEdition = createEdition("Other venue", "Other edition");
		targetEditionId = targetEdition.getId();

		String suffix = UUID.randomUUID().toString().substring(0, 8);

		Referee targetReferee = Referee.create("Target Referee " + suffix, "target.ref." + suffix + "@example.com", "111111111");
		targetReferee.setEdition(targetEdition);
		refereeRepository.save(targetReferee);

		Judge targetJudge = Judge.create("Target Judge " + suffix, "target.judge." + suffix + "@example.com", "222222222");
		targetJudge.setEdition(targetEdition);
		judgeRepository.save(targetJudge);

		Floater targetFloater = Floater.create("Target Floater " + suffix, "target.floater." + suffix + "@example.com",
				"333333333", "TARGET-" + suffix);
		targetFloater.setEdition(targetEdition);
		floaterRepository.save(targetFloater);

		otherRefereeName = "Other Referee " + suffix;
		Referee otherReferee = Referee.create(otherRefereeName, "other.ref." + suffix + "@example.com", "444444444");
		otherReferee.setEdition(otherEdition);
		refereeRepository.save(otherReferee);

		otherJudgeName = "Other Judge " + suffix;
		Judge otherJudge = Judge.create(otherJudgeName, "other.judge." + suffix + "@example.com", "555555555");
		otherJudge.setEdition(otherEdition);
		judgeRepository.save(otherJudge);

		otherFloaterName = "Other Floater " + suffix;
		Floater otherFloater = Floater.create(otherFloaterName, "other.floater." + suffix + "@example.com", "666666666", "OTHER-" + suffix);
		otherFloater.setEdition(otherEdition);
		floaterRepository.save(otherFloater);
	}

	@Given("an empty edition exists for volunteer overview")
	public void anEmptyEditionExistsForVolunteerOverview() {
		Edition edition = createEdition("Empty venue", "Empty edition");
		targetEditionId = edition.getId();
	}

	@When("I request volunteers grouped by type for the target edition")
	public void iRequestVolunteersGroupedByTypeForTheTargetEdition() throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(get("/editions/{editionId}/volunteers", targetEditionId)
				.accept(MediaType.APPLICATION_JSON)
				.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@When("I request volunteers grouped by type for edition id {long}")
	public void iRequestVolunteersGroupedByTypeForEditionId(long editionId) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(get("/editions/{editionId}/volunteers", editionId)
				.accept(MediaType.APPLICATION_JSON)
				.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@And("the volunteer overview contains {int} referee {int} judge and {int} floater")
	public void theVolunteerOverviewContainsRefereeJudgeAndFloater(int referees, int judges, int floaters) throws Exception {
		stepDefs.result
			.andExpect(jsonPath("$.referees.length()").value(referees))
			.andExpect(jsonPath("$.judges.length()").value(judges))
			.andExpect(jsonPath("$.floaters.length()").value(floaters));
	}

	@And("the volunteer overview does not include volunteers from other editions")
	public void theVolunteerOverviewDoesNotIncludeVolunteersFromOtherEditions() throws Exception {
		stepDefs.result
			.andExpect(jsonPath("$.referees[*].name", not(hasItem(otherRefereeName))))
			.andExpect(jsonPath("$.judges[*].name", not(hasItem(otherJudgeName))))
			.andExpect(jsonPath("$.floaters[*].name", not(hasItem(otherFloaterName))));
	}

	@And("the volunteer overview error is {string}")
	public void theVolunteerOverviewErrorIs(String error) throws Exception {
		stepDefs.result.andExpect(jsonPath("$.error").value(error));
	}

	private Edition createEdition(String venueName, String description) {
		Edition edition = Edition.create(2026, venueName + " " + UUID.randomUUID(), description);
		return editionRepository.save(edition);
	}
}
