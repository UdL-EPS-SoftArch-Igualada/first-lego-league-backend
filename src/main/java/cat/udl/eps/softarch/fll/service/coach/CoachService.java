package cat.udl.eps.softarch.fll.service.coach;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cat.udl.eps.softarch.fll.domain.Coach;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.dto.AssignCoachResponse;
import cat.udl.eps.softarch.fll.exception.TeamCoachAssignmentException;
import cat.udl.eps.softarch.fll.repository.team.TeamRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CoachService {

	private final TeamRepository teamRepository;

	@PersistenceContext
	private EntityManager entityManager;

	public AssignCoachResponse assignCoach(String teamId, Integer coachId) {

		Long teamLongId;
		try {
			teamLongId = Long.parseLong(teamId);
		} catch (NumberFormatException e) {
			throw new TeamCoachAssignmentException("TEAM_NOT_FOUND", "Invalid team ID format: " + teamId);
		}

		Team team = entityManager.find(Team.class, teamLongId, LockModeType.PESSIMISTIC_WRITE);
		if (team == null) {
			throw new TeamCoachAssignmentException("TEAM_NOT_FOUND", "Team not found");
		}

		Coach coach = entityManager.find(Coach.class, coachId, LockModeType.PESSIMISTIC_WRITE);
		if (coach == null) {
			throw new TeamCoachAssignmentException("COACH_NOT_FOUND", "Coach not found");
		}

		try {
			team.addCoach(coach);
		} catch (IllegalStateException exception) {
			throw toAssignmentException(exception);
		}
		teamRepository.save(team);

		return new AssignCoachResponse(teamId, coachId, "ASSIGNED");
	}

	private TeamCoachAssignmentException toAssignmentException(IllegalStateException exception) {
		String errorCode = exception.getMessage();
		String message = switch (errorCode) {
			case "COACH_NOT_FOUND" -> "Coach not found";
			case "COACH_ALREADY_ASSIGNED" -> "Coach is already assigned to this team";
			case "MAX_COACHES_PER_TEAM_REACHED" -> "Team already has the maximum number of coaches";
			case "MAX_TEAMS_PER_COACH_REACHED" -> "Coach already trains the maximum number of teams";
			default -> "Invalid coach assignment";
		};
		return new TeamCoachAssignmentException(errorCode, message);
	}
}