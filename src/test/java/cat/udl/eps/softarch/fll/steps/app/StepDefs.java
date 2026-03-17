package cat.udl.eps.softarch.fll.steps.app;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cat.udl.eps.softarch.fll.MainApplication;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.spring.CucumberContextConfiguration;

@ContextConfiguration(classes = {MainApplication.class}, loader = SpringBootContextLoader.class)
@DirtiesContext
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ActiveProfiles("test")
@CucumberContextConfiguration
public class StepDefs {

	protected final WebApplicationContext wac;
	public MockMvc mockMvc;
	public ResultActions result;
	public ObjectMapper mapper = new ObjectMapper();

	public StepDefs(WebApplicationContext wac) {
		this.wac = wac;
	}

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(this.wac)
				.apply(SecurityMockMvcConfigurers.springSecurity())
				.build();
		this.mapper.registerModule(new JavaTimeModule());
	}

	@Then("^The response code is (\\d+)$")
	public void theResponseCodeIs(int code) throws Throwable {
		result.andExpect(status().is(code));
	}

	@And("^The error message is \"([^\"]*)\"$")
	public void theErrorMessageIs(String message) throws Throwable {
		if (result.andReturn().getResponse().getContentAsString().isEmpty()) {
			result.andExpect(status().reason(is(message)));
		} else {
			result.andExpect(jsonPath("$..message", hasItem(containsString(message))));
		}
	}

	@And("^The error code is \"([^\"]*)\"$")
	public void theErrorCodeIs(String errorCode) throws Throwable {
		result.andExpect(jsonPath("$.error", is(errorCode)));
	}
}
