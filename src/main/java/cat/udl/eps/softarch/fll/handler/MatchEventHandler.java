package cat.udl.eps.softarch.fll.handler;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.service.MatchScheduleValidationService;

@Component
@RepositoryEventHandler(Match.class)
public class MatchEventHandler {

	private final MatchScheduleValidationService validationService;

	public MatchEventHandler(MatchScheduleValidationService validationService) {
		this.validationService = validationService;
	}

	@HandleBeforeCreate
	public void handleMatchBeforeCreate(Match match) {
		validationService.validateForCreateOrUpdate(match);
	}

	@HandleBeforeSave
	public void handleMatchBeforeSave(Match match) {
		validationService.validateForCreateOrUpdate(match);
	}
}