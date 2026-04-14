package cat.udl.eps.softarch.fll.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cat.udl.eps.softarch.fll.domain.Edition;
import cat.udl.eps.softarch.fll.domain.EditionOperation;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.exception.EditionLifecycleException;
import cat.udl.eps.softarch.fll.exception.EditionTeamRegistrationException;
import cat.udl.eps.softarch.fll.repository.edition.EditionRepository;
import cat.udl.eps.softarch.fll.repository.team.TeamRepository;

@Service
public class EditionTeamRegistrationService {

	private final EditionRepository editionRepository;
	private final TeamRepository teamRepository;
	private final EditionLifecycleService editionLifecycleService;

	public EditionTeamRegistrationService(EditionRepository editionRepository,
			TeamRepository teamRepository,
			EditionLifecycleService editionLifecycleService) {
		this.editionRepository = editionRepository;
		this.teamRepository = teamRepository;
		this.editionLifecycleService = editionLifecycleService;
	}

	@Transactional
	public Edition registerTeam(Long editionId, Long teamId) {
		Edition edition = editionRepository.findByIdForUpdate(editionId)
				.orElseThrow(() -> new EditionTeamRegistrationException(
						"EDITION_NOT_FOUND", "Edition with id " + editionId + " not found"));

		try {
			editionLifecycleService.assertOperationAllowed(edition, EditionOperation.TEAM_REGISTRATION);
		} catch (EditionLifecycleException exception) {
			throw new EditionTeamRegistrationException(exception.getError(), exception.getMessage(), exception);
		}

		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new EditionTeamRegistrationException(
						"TEAM_NOT_FOUND", "Team with id " + teamId + " not found"));

		if (edition.containsTeam(team)) {
			throw new EditionTeamRegistrationException(
					"TEAM_ALREADY_REGISTERED", "Team " + teamId + " is already registered in edition " + editionId);
		}

		if (edition.hasReachedMaxTeams()) {
			throw new EditionTeamRegistrationException(
					"MAX_TEAMS_REACHED",
					"Edition " + editionId + " has reached the maximum of " + Edition.MAX_TEAMS + " teams");
		}

		edition.getTeams().add(team);
		return editionRepository.save(edition);
	}
}
