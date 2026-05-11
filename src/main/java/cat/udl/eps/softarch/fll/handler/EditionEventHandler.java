package cat.udl.eps.softarch.fll.handler;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import cat.udl.eps.softarch.fll.domain.edition.Edition;
import cat.udl.eps.softarch.fll.domain.edition.Venue;
import cat.udl.eps.softarch.fll.repository.edition.VenueRepository;

@Component
@RepositoryEventHandler
public class EditionEventHandler {

	private final VenueRepository venueRepository;

	public EditionEventHandler(VenueRepository venueRepository) {
		this.venueRepository = venueRepository;
	}

	@HandleBeforeCreate
	public void handleEditionBeforeCreate(Edition edition) {
		if (edition.getVenue() != null) {
			return;
		}
		String venueName = edition.getInputVenueName();
		if (venueName == null || venueName.isBlank()) {
			return;
		}
		String venueCity = edition.getInputVenueCity() != null ? edition.getInputVenueCity() : venueName;
		Venue venue = venueRepository.findByName(venueName)
			.orElseGet(() -> venueRepository.save(Venue.create(venueName, venueCity)));
		edition.setVenue(venue);
	}
}
