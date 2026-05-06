package cat.udl.eps.softarch.fll.service.floater;

import cat.udl.eps.softarch.fll.domain.team.Team;
import cat.udl.eps.softarch.fll.domain.volunteer.Floater;
import cat.udl.eps.softarch.fll.dto.AssignFloaterResponse;
import cat.udl.eps.softarch.fll.exception.TeamFloaterAssignmentException;
import cat.udl.eps.softarch.fll.repository.team.TeamRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FloaterAssignmentService {

	private final TeamRepository teamRepository;

	@PersistenceContext
	private EntityManager entityManager;

	public FloaterAssignmentService(TeamRepository teamRepository) {
		this.teamRepository = teamRepository;
	}

	@Transactional
	public AssignFloaterResponse assignFloater(String teamId, Long floaterId) {
		Team team = entityManager.find(Team.class, teamId, LockModeType.PESSIMISTIC_WRITE);
		if (team == null) {
			throw new TeamFloaterAssignmentException("TEAM_NOT_FOUND", "Team not found");
		}
		Floater floater = entityManager.find(Floater.class, floaterId, LockModeType.PESSIMISTIC_WRITE);
		if (floater == null) {
			throw new TeamFloaterAssignmentException("FLOATER_NOT_FOUND", "Floater not found");
		}

		try {
			team.addFloater(floater);
		} catch (IllegalStateException exception) {
			throw toAssignmentException(exception);
		}

		teamRepository.save(team);
		return new AssignFloaterResponse(teamId, floaterId);
	}

	private TeamFloaterAssignmentException toAssignmentException(IllegalStateException exception) {
		String errorCode = exception.getMessage();
		String message = switch (errorCode) {
			case "FLOATER_NOT_FOUND" -> "Floater not found";
			case "FLOATER_ALREADY_ASSIGNED" -> "Floater is already assigned to this team";
			case "MAX_FLOATERS_REACHED", "A team cannot have more than 2 floaters" ->
				"Team already has the maximum number of floaters";
			default -> "Invalid floater assignment";
		};
		String normalizedErrorCode = switch (errorCode) {
			case "A team cannot have more than 2 floaters" -> "MAX_FLOATERS_REACHED";
			default -> errorCode;
		};
		return new TeamFloaterAssignmentException(normalizedErrorCode, message);
	}
}
