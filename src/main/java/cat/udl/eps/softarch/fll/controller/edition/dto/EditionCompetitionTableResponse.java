package cat.udl.eps.softarch.fll.controller.edition.dto;

import java.util.List;

public record EditionCompetitionTableResponse(
	String identifier,
	List<EditionTableMatchResponse> matches) {
}
