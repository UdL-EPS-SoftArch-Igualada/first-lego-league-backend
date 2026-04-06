package cat.udl.eps.softarch.fll.controller.volunteer.dto;

public record VolunteerSummaryResponse(
	Long id,
	String name,
	String emailAddress,
	String phoneNumber) {
}
