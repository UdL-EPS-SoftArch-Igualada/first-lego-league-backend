package cat.udl.eps.softarch.fll.controller.dto;

public record VolunteerSummaryResponse(
		Long id,
		String name,
		String emailAddress,
		String phoneNumber) {
}
