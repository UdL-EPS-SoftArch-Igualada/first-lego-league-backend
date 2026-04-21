package cat.udl.eps.softarch.fll.service.award;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cat.udl.eps.softarch.fll.domain.Award;
import cat.udl.eps.softarch.fll.domain.Edition;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.dto.AssignAwardResponse;
import cat.udl.eps.softarch.fll.exception.AwardAssignmentException;
import cat.udl.eps.softarch.fll.repository.ranking.AwardRepository;
import cat.udl.eps.softarch.fll.repository.team.TeamRepository;

@Service
public class AwardAssignmentService {

	private final AwardRepository awardRepository;
	private final TeamRepository teamRepository;

	public AwardAssignmentService(AwardRepository awardRepository, TeamRepository teamRepository) {
		this.awardRepository = awardRepository;
		this.teamRepository = teamRepository;
	}

	@Transactional
	public AssignAwardResponse assignAward(String awardId, String teamId) {
		if (awardId == null || awardId.isBlank() || teamId == null || teamId.isBlank()) {
			throw new AwardAssignmentException(
				"INVALID_AWARD_ASSIGNMENT_REQUEST",
				"Award id and team id must be provided");
		}

		Long parsedAwardId = parseAwardId(awardId);
		Award award = awardRepository.findByIdForUpdate(parsedAwardId)
			.orElseThrow(() -> new AwardAssignmentException(
				"AWARD_NOT_FOUND",
				"Award with id " + awardId + " not found"));

		Team team = teamRepository.findByNameForUpdate(teamId)
			.orElseThrow(() -> new AwardAssignmentException(
				"TEAM_NOT_FOUND",
				"Team with id " + teamId + " not found"));

		if (award.getWinner() != null) {
			throw new AwardAssignmentException(
				"AWARD_ALREADY_ASSIGNED",
				"Award " + awardId + " has already been assigned to a team");
		}

		validateEditionMembership(award, team, awardId, teamId);

		award.setWinner(team);
		awardRepository.saveAndFlush(award);

		return new AssignAwardResponse(awardId, teamId, "AWARDED");
	}

	private Long parseAwardId(String awardId) {
		try {
			return Long.valueOf(awardId);
		} catch (NumberFormatException exception) {
			throw new AwardAssignmentException(
				"INVALID_AWARD_ID",
				"Award id must be numeric");
		}
	}

	private void validateEditionMembership(Award award, Team team, String awardId, String teamId) {
		Edition awardEdition = award.getEdition();
		Edition teamEdition = team.getEdition();

		if (awardEdition == null || teamEdition == null || !awardEdition.getId().equals(teamEdition.getId())) {
			throw new AwardAssignmentException(
				"EDITION_MISMATCH",
				"Team " + teamId + " is not registered in the edition for award " + awardId);
		}
	}
}
