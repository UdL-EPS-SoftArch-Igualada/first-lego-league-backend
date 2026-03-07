package cat.udl.eps.softarch.fll.controller.dto;

import java.util.List;

public record EditionVolunteersResponse(
		List<VolunteerSummaryResponse> referees,
		List<VolunteerSummaryResponse> judges,
		List<VolunteerSummaryResponse> floaters) {
}
