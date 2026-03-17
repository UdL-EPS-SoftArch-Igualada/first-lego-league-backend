package cat.udl.eps.softarch.fll.steps.edition;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import cat.udl.eps.softarch.fll.steps.app.AuthenticationStepDefs;
import cat.udl.eps.softarch.fll.steps.app.StepDefs;
import org.springframework.http.MediaType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class SearchVenueStepDefs {

    private final StepDefs stepDefs;

    public SearchVenueStepDefs(StepDefs stepDefs) {
        this.stepDefs = stepDefs;
    }

    @Given("a venue exists with name {string} and city {string}")
    public void a_venue_exists_with_name_and_city(String name, String city) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("city", city);

        stepDefs.mockMvc.perform(post("/venues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(stepDefs.mapper.writeValueAsString(body))
                .characterEncoding(StandardCharsets.UTF_8)
                .with(AuthenticationStepDefs.authenticate()))
                .andExpect(status().isCreated());
    }

    @When("I search for a venue by partial name {string}")
    public void i_search_for_a_venue_by_partial_name(String searchName) throws Exception {
        stepDefs.result = stepDefs.mockMvc.perform(get("/venues/search/findByNameContaining")
                .param("name", searchName)
                .with(AuthenticationStepDefs.authenticate()));
    }

    @Then("the venue search API response status should be {int}")
    public void the_venue_search_api_response_status_should_be(int expectedStatus) throws Exception {
        stepDefs.result.andExpect(status().is(expectedStatus));
    }

    @Then("the search response should contain a venue with name {string}")
    public void the_search_response_should_contain_a_venue_with_name(String expectedName) throws Exception {
        stepDefs.result.andExpect(jsonPath("$._embedded.venues[0].name").value(expectedName));
    }

    @Then("the search response should be empty")
    public void the_search_response_should_be_empty() throws Exception {
        stepDefs.result.andExpect(jsonPath("$._embedded.venues", hasSize(0)));
    }
}