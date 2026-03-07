package cat.udl.eps.softarch.fll.steps;

import cat.udl.eps.softarch.fll.domain.Venue;
import cat.udl.eps.softarch.fll.repository.VenueRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class ManageVenueStepDefs {
	private final StepDefs stepDefs;
	private final VenueRepository venueRepository;

	public ManageVenueStepDefs(StepDefs stepDefs, VenueRepository venueRepository) {
		this.stepDefs = stepDefs;
		this.venueRepository = venueRepository;
	}

	@Given("^There is no venue with name \"([^\"]*)\"$")
	public void thereIsNoVenueWithName(String name) {
		venueRepository.findByName(name).ifPresent(venueRepository::delete);
	}

	@Given("^There is a venue with name \"([^\"]*)\" and city \"([^\"]*)\"$")
	public void thereIsAVenueWithNameAndCity(String name, String city) {
		Venue venue = Venue.create(name, city);
		venueRepository.save(venue);
	}

	@When("^I create a new venue with name \"([^\"]*)\" and city \"([^\"]*)\"$")
	public void iCreateANewVenueWithNameAndCity(String name, String city) throws Throwable {
		Venue venue = Venue.create(name, city);

		stepDefs.result = stepDefs.mockMvc.perform(
				post("/venues")
					.contentType(MediaType.APPLICATION_JSON)
					.content(stepDefs.mapper.writeValueAsString(venue))
					.characterEncoding(StandardCharsets.UTF_8)
					.accept(MediaType.APPLICATION_JSON)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@When("^I retrieve the venue with name \"([^\"]*)\"$")
	public void iRetrieveTheVenueWithName(String name) throws Throwable {
		Venue venue = findVenueByName(name);
		stepDefs.result = stepDefs.mockMvc.perform(
				get("/venues/{id}", venue.getId())
					.accept(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@When("^I update the venue with name \"([^\"]*)\" to city \"([^\"]*)\"$")
	public void iUpdateTheVenueWithNameToCity(String name, String newCity) throws Throwable {
		Venue existingVenue = findVenueByName(name);
		Venue venue = Venue.create(name, newCity);

		stepDefs.result = stepDefs.mockMvc.perform(
				put("/venues/{id}", existingVenue.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(stepDefs.mapper.writeValueAsString(venue))
					.characterEncoding(StandardCharsets.UTF_8)
					.accept(MediaType.APPLICATION_JSON)
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@When("^I delete the venue with name \"([^\"]*)\"$")
	public void iDeleteTheVenueWithName(String name) throws Throwable {
		Venue venue = findVenueByName(name);
		stepDefs.result = stepDefs.mockMvc.perform(
				delete("/venues/{id}", venue.getId())
					.with(AuthenticationStepDefs.authenticate()))
			.andDo(print());
	}

	@And("^A venue with name \"([^\"]*)\" and city \"([^\"]*)\" exists$")
	public void aVenueWithNameAndCityExists(String name, String city) {
		Venue venue = findVenueByName(name);
		assertEquals(city, venue.getCity());
	}

	@And("^The response contains venue name \"([^\"]*)\" and city \"([^\"]*)\"$")
	public void theResponseContainsVenueNameAndCity(String name, String city) throws Throwable {
		stepDefs.result
			.andExpect(jsonPath("$.name", is(name)))
			.andExpect(jsonPath("$.city", is(city)));
	}

	@And("^The venue with name \"([^\"]*)\" has city \"([^\"]*)\"$")
	public void theVenueWithNameHasCity(String name, String city) {
		aVenueWithNameAndCityExists(name, city);
	}

	@And("^No venue with name \"([^\"]*)\" exists$")
	public void noVenueWithNameExists(String name) {
		assertFalse(venueRepository.findByName(name).isPresent());
	}

	private Venue findVenueByName(String name) {
		return venueRepository.findByName(name)
			.orElseThrow(() -> new NoSuchElementException("Venue not found with name: " + name));
	}
}
