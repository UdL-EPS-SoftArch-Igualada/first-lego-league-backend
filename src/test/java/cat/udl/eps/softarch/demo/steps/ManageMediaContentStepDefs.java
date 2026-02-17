package cat.udl.eps.softarch.demo.steps;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import cat.udl.eps.softarch.demo.domain.MediaContent;
import cat.udl.eps.softarch.demo.repository.MediaContentRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class ManageMediaContentStepDefs {
	private final StepDefs stepDefs;
	private final MediaContentRepository mediaContentRepository;
	private String mediaContentUri;

	public ManageMediaContentStepDefs(StepDefs stepDefs, MediaContentRepository mediaContentRepository) {
		this.stepDefs = stepDefs;
		this.mediaContentRepository = mediaContentRepository;
	}

	@When("I create a new media content with url {string} and type {string}")
	public void iCreateANewMediaContent(String url, String type) throws Exception {
		MediaContent mediaContent = new MediaContent();
		mediaContent.setUrl(url);
		mediaContent.setType(type);

		stepDefs.result = stepDefs.mockMvc.perform(
				post("/mediaContents")
						.contentType(MediaType.APPLICATION_JSON)
						.content(stepDefs.mapper.writeValueAsString(mediaContent))
						.characterEncoding(StandardCharsets.UTF_8)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());

		mediaContentUri = stepDefs.result.andReturn().getResponse().getHeader("Location");
	}

	@And("The created media content has type {string}")
	public void theCreatedMediaContentHasType(String type) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get(mediaContentUri)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print())
				.andExpect(jsonPath("$.type", is(type)));
	}

	@Given("There is a media content with url {string} and type {string}")
	public void thereIsAMediaContent(String url, String type) {
		MediaContent mediaContent = new MediaContent();
		mediaContent.setUrl(url);
		mediaContent.setType(type);
		mediaContentRepository.save(mediaContent);
		mediaContentUri = "/mediaContents/" + url;
	}

	@When("I retrieve the media content with url {string}")
	public void iRetrieveTheMediaContent(String url) throws Exception {
		mediaContentUri = "/mediaContents/" + url;
		stepDefs.result = stepDefs.mockMvc.perform(
				get(mediaContentUri)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}

	@When("I update the media content with url {string} type to {string}")
	public void iUpdateTheMediaContentType(String url, String type) throws Exception {
		mediaContentUri = "/mediaContents/" + url;
		stepDefs.result = stepDefs.mockMvc.perform(
				patch(mediaContentUri)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"type\": \"" + type + "\"}")
						.characterEncoding(StandardCharsets.UTF_8)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}

	@And("The retrieved media content has type {string}")
	public void theRetrievedMediaContentHasType(String type) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				get(mediaContentUri)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print())
				.andExpect(jsonPath("$.type", is(type)));
	}

	@When("I delete the media content with url {string}")
	public void iDeleteTheMediaContent(String url) throws Exception {
		stepDefs.result = stepDefs.mockMvc.perform(
				delete("/mediaContents/" + url)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print());
	}

	@And("The media content with url {string} has been deleted")
	public void theMediaContentHasBeenDeleted(String url) throws Exception {
		stepDefs.mockMvc.perform(
				get("/mediaContents/" + url)
						.accept(MediaType.APPLICATION_JSON)
						.with(AuthenticationStepDefs.authenticate()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}
}
