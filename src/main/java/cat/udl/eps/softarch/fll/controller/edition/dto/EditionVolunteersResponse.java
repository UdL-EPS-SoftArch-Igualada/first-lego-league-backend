package cat.udl.eps.softarch.fll.controller.edition.dto;

import cat.udl.eps.softarch.fll.controller.volunteer.dto.VolunteerSummaryResponse;
import java.util.List;

public record EditionVolunteersResponse(
	List<VolunteerSummaryResponse> referees,
	List<VolunteerSummaryResponse> judges,
	List<VolunteerSummaryResponse> floaters) {
}
