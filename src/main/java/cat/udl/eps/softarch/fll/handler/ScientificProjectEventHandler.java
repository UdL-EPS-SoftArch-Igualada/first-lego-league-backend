package cat.udl.eps.softarch.fll.handler;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import cat.udl.eps.softarch.fll.domain.ScientificProject;
import cat.udl.eps.softarch.fll.exception.DomainValidationException;
import cat.udl.eps.softarch.fll.repository.edition.EditionRepository;
import cat.udl.eps.softarch.fll.repository.team.TeamRepository;

@Component
@RepositoryEventHandler
public class ScientificProjectEventHandler {

	private final TeamRepository teamRepository;
	private final EditionRepository editionRepository;

	public ScientificProjectEventHandler(TeamRepository teamRepository, EditionRepository editionRepository) {
		this.teamRepository = teamRepository;
		this.editionRepository = editionRepository;
	}

	@HandleBeforeCreate
	public void handleScientificProjectPreCreate(ScientificProject project) {
		validateProject(project);
	}

	@HandleBeforeSave
	public void handleScientificProjectPreSave(ScientificProject project) {
		validateProject(project);
	}

	private void validateProject(ScientificProject project) {
		validateTeam(project);
		validateEdition(project);
		validateTeamEditionCompatibility(project);
	}

	private void validateTeam(ScientificProject project) {
		if (project.getTeam() == null || project.getTeam().getId() == null) {
			throw new DomainValidationException("TEAM_REQUIRED",
				"A scientific project must have an associated team");
		}

	}

	private void validateEdition(ScientificProject project) {
		if (project.getEdition() == null || project.getEdition().getId() == null) {
			throw new DomainValidationException("EDITION_REQUIRED",
				"A scientific project must belong to an edition");
		}

		if (!editionRepository.existsById(project.getEdition().getId())) {
			throw new DomainValidationException("EDITION_NOT_FOUND",
				"The referenced edition does not exist");
		}
	}

	private void validateTeamEditionCompatibility(ScientificProject project) {
		if (project.getTeam() == null || project.getTeam().getId() == null
			|| project.getEdition() == null || project.getEdition().getId() == null) {
			return;
		}

		if (!teamRepository.existsByIdAndRegisteredEditionsId(
			project.getTeam().getName(),
			project.getEdition().getId())) {

			throw new DomainValidationException("EDITION_TEAM_MISMATCH",
				"The referenced team is not registered in the referenced edition");
		}
	}
}