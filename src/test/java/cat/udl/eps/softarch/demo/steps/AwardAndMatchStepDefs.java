package cat.udl.eps.softarch.demo.steps;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.http.MediaType;
import io.cucumber.java.en.When;

public class AwardAndMatchStepDefs {
    
    private final StepDefs stepDefs;

    public AwardAndMatchStepDefs(StepDefs stepDefs) {
        this.stepDefs = stepDefs;
    }

    @When("^I request the match results list$")
    public void iRequestTheMatchResultsList() throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/matchResults")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(AuthenticationStepDefs.authenticate()));
    }

    @When("^I request the awards list$")
    public void iRequestTheAwardsList() throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/awards")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(AuthenticationStepDefs.authenticate()));
    }
}