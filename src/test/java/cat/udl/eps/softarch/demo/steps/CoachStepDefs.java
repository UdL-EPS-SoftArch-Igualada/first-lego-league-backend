package cat.udl.eps.softarch.demo.steps;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import cat.udl.eps.softarch.demo.domain.Coach;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;

public class CoachStepDefs {
	private final StepDefs stepDefs;

	public CoachStepDefs(StepDefs stepDefs) {
		this.stepDefs = stepDefs;
	}

	@When("^I create a new coach with name \"([^\"]*)\", email \"([^\"]*)\" and phone \"([^\"]*)\"$")
	public void iCreateANewCoach(String name, String email, String phone) throws Throwable {
		Coach coach = new Coach();
		coach.setName(name);
		coach.setEmailAddress(email);
		coach.setPhoneNumber(phone);

		stepDefs.result = stepDefs.mockMvc.perform(
				post("/coaches")
						.contentType(MediaType.APPLICATION_JSON)
						.content(stepDefs.mapper.writeValueAsString(coach))
						.characterEncoding(StandardCharsets.UTF_8)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}

	@And("^It has been created a coach with name \"([^\"]*)\" and email \"([^\"]*)\"$")
	public void itHasBeenCreatedACoach(String name, String email) throws Throwable {
		String location = stepDefs.result.andReturn().getResponse().getHeader("Location");

		stepDefs.result = stepDefs.mockMvc.perform(
				get(location)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print())
				.andExpect(jsonPath("$.name", is(name)))
				.andExpect(jsonPath("$.emailAddress", is(email)));
	}
}
