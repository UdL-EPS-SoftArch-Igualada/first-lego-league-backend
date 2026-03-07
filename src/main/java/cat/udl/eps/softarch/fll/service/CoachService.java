package cat.udl.eps.softarch.fll.service;

import cat.udl.eps.softarch.fll.domain.Coach;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.dto.AssignCoachResponse;
import cat.udl.eps.softarch.fll.repository.CoachRepository;
import cat.udl.eps.softarch.fll.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.LockModeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class CoachService {

	private final TeamRepository teamRepository;
	private final CoachRepository coachRepository;

	@PersistenceContext
	private EntityManager entityManager;

	public AssignCoachResponse assignCoach(String teamId, Integer coachId) {

		Team team = entityManager.find(Team.class, teamId, LockModeType.PESSIMISTIC_WRITE);
		if (team == null) {
			throw new NoSuchElementException("TEAM_NOT_FOUND");
		}
		Coach coach = entityManager.find(Coach.class, coachId, LockModeType.PESSIMISTIC_WRITE);
		if (coach == null) {
			throw new NoSuchElementException("COACH_NOT_FOUND");
		}



		team.addCoach(coach);
		teamRepository.save(team);

		return new AssignCoachResponse(teamId, coachId, "ASSIGNED");
	}
}