package cat.udl.eps.softarch.fll.handler;

import cat.udl.eps.softarch.fll.domain.TeamMember;
import cat.udl.eps.softarch.fll.exception.DomainValidationException;
import cat.udl.eps.softarch.fll.repository.team.TeamMemberRepository;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class TeamMemberEventHandler {

	private final TeamMemberRepository teamMemberRepository;

	public TeamMemberEventHandler(TeamMemberRepository teamMemberRepository) {
		this.teamMemberRepository = teamMemberRepository;
	}

	@HandleBeforeCreate
	public void handleTeamMemberBeforeCreate(TeamMember member) {
		if (member.getTeam() == null) {
			return;
		}
		if (teamMemberRepository.countByTeam(member.getTeam()) >= 10) {
			throw new DomainValidationException(
				"TEAM_CAPACITY_EXCEEDED",
				"A team cannot have more than 10 members"
			);
		}
	}
}