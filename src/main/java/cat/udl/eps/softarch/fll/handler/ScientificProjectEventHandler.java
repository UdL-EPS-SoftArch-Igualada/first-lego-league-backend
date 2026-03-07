package cat.udl.eps.softarch.fll.handler;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import cat.udl.eps.softarch.fll.domain.ScientificProject;
import cat.udl.eps.softarch.fll.exception.DomainValidationException;
import cat.udl.eps.softarch.fll.repository.TeamRepository;

@Component
@RepositoryEventHandler(ScientificProject.class)
public class ScientificProjectEventHandler {

	private final TeamRepository teamRepository;

	public ScientificProjectEventHandler(TeamRepository teamRepository) {
		this.teamRepository = teamRepository;
	}

	@HandleBeforeCreate
	public void handleScientificProjectPreCreate(ScientificProject project) {
		validateTeam(project);
	}

	@HandleBeforeSave
	public void handleScientificProjectPreSave(ScientificProject project) {
		validateTeam(project);
	}

	private void validateTeam(ScientificProject project) {
		if (project.getTeam() == null || project.getTeam().getId() == null || project.getTeam().getId().isBlank()) {
			throw new DomainValidationException("TEAM_REQUIRED",
					"A scientific project must have an associated team");
		}

		if (!teamRepository.existsById(project.getTeam().getId())) {
			throw new DomainValidationException("TEAM_NOT_FOUND",
					"The referenced team does not exist");
		}
	}
}
