package cat.udl.eps.softarch.fll.steps;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.MediaType;
import cat.udl.eps.softarch.fll.domain.Referee;
import cat.udl.eps.softarch.fll.repository.RefereeRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;


public class ManageRefereeStepDefs {
	private final StepDefs stepDefs;
	private final RefereeRepository refereeRepository;

	public ManageRefereeStepDefs(StepDefs stepDefs, RefereeRepository refereeRepository) {
		this.stepDefs = stepDefs;
		this.refereeRepository = refereeRepository;
	}

	@Given("^There is no referee with email \"([^\"]*)\"$")
	public void thereIsNoRefereeWithEmail(String email) {
		refereeRepository.findByEmailAddress(email).ifPresent(refereeRepository::delete);
	}

	@Given("^There is a referee with name \"([^\"]*)\", email \"([^\"]*)\", phone \"([^\"]*)\" and expert \"([^\"]*)\"$")
	public void thereIsARefereeWithNameEmailPhoneAndExpert(String name, String email, String phone, String expert) {
		Referee referee = new Referee();
		referee.setName(name);
		referee.setEmailAddress(email);
		referee.setPhoneNumber(phone);
		referee.setExpert(Boolean.parseBoolean(expert));
		refereeRepository.save(referee);
	}

	@When("^I create a new referee with name \"([^\"]*)\", email \"([^\"]*)\", phone \"([^\"]*)\" and expert \"([^\"]*)\"$")
	public void iCreateANewRefereeWithNameEmailPhoneAndExpert(String name, String email, String phone, String expert)
			throws Throwable {
		Referee referee = new Referee();
		referee.setName(name);
		referee.setEmailAddress(email);
		referee.setPhoneNumber(phone);
		referee.setExpert(Boolean.parseBoolean(expert));

		stepDefs.result = stepDefs.mockMvc.perform(
				post("/referees")
						.contentType(MediaType.APPLICATION_JSON)
						.content(stepDefs.mapper.writeValueAsString(referee))
						.characterEncoding(StandardCharsets.UTF_8)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}

	@When("^I retrieve the referee with email \"([^\"]*)\"$")
	public void iRetrieveTheRefereeWithEmail(String email) throws Throwable {
		Referee referee = findRefereeByEmail(email);
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/referees/{id}", referee.getId())
						.accept(MediaType.APPLICATION_JSON)
						.characterEncoding(StandardCharsets.UTF_8)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}

	@When("^I update the referee with email \"([^\"]*)\" to expert \"([^\"]*)\"$")
	public void iUpdateTheRefereeWithEmailToExpert(String email, String expert) throws Throwable {
		Referee existingReferee = findRefereeByEmail(email);
		stepDefs.result = stepDefs.mockMvc.perform(
				patch("/referees/{id}", existingReferee.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(stepDefs.mapper.writeValueAsString(Map.of("expert", Boolean.parseBoolean(expert))))
						.characterEncoding(StandardCharsets.UTF_8)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}

	@When("^I delete the referee with email \"([^\"]*)\"$")
	public void iDeleteTheRefereeWithEmail(String email) throws Throwable {
		Referee referee = findRefereeByEmail(email);
		stepDefs.result = stepDefs.mockMvc.perform(
				delete("/referees/{id}", referee.getId())
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}

	@And("^A referee with name \"([^\"]*)\" and email \"([^\"]*)\" exists$")
	public void aRefereeWithNameAndEmailExists(String name, String email) {
		Referee referee = findRefereeByEmail(email);
		assertEquals(name, referee.getName());
		assertEquals(email, referee.getEmailAddress());
	}

	@And("^The response contains referee name \"([^\"]*)\" and email \"([^\"]*)\"$")
	public void theResponseContainsRefereeNameAndEmail(String name, String email) throws Throwable {
		stepDefs.result
				.andExpect(jsonPath("$.name", is(name)))
				.andExpect(jsonPath("$.emailAddress", is(email)));
	}

	@And("^A referee with name \"([^\"]*)\", email \"([^\"]*)\", phone \"([^\"]*)\" and expert \"([^\"]*)\" exists$")
	public void aRefereeWithNameEmailPhoneAndExpertExists(String name, String email, String phone, String expert) {
		Referee referee = findRefereeByEmail(email);
		assertEquals(name, referee.getName());
		assertEquals(email, referee.getEmailAddress());
		assertEquals(phone, referee.getPhoneNumber());
		assertEquals(Boolean.parseBoolean(expert), referee.isExpert());
	}

	@And("^The response contains referee name \"([^\"]*)\", email \"([^\"]*)\", phone \"([^\"]*)\" and expert \"([^\"]*)\"$")
	public void theResponseContainsRefereeNameEmailPhoneAndExpert(String name, String email, String phone, String expert) throws Throwable {
		stepDefs.result
				.andExpect(jsonPath("$.name", is(name)))
				.andExpect(jsonPath("$.emailAddress", is(email)))
				.andExpect(jsonPath("$.phoneNumber", is(phone)))
				.andExpect(jsonPath("$.expert", is(Boolean.parseBoolean(expert))));
	}

	@And("^The referee with email \"([^\"]*)\" is expert$")
	public void theRefereeWithEmailIsExpert(String email) {
		Referee referee = findRefereeByEmail(email);
		assertTrue(referee.isExpert());
	}

	@And("^No referee with email \"([^\"]*)\" exists$")
	public void noRefereeWithEmailExists(String email) {
		assertFalse(refereeRepository.findByEmailAddress(email).isPresent());
	}

	private Referee findRefereeByEmail(String email) {
		return refereeRepository.findByEmailAddress(email)
				.orElseThrow(() -> new NoSuchElementException("Referee not found with email: " + email));
	}
}
