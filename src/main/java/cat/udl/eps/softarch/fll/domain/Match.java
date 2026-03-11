package cat.udl.eps.softarch.fll.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.time.LocalTime;

@Entity
@Table(name = "matches")
@Getter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Match extends UriEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@Getter
	private LocalTime startTime;

	@Getter
	private LocalTime endTime;

	@Getter
	@JsonBackReference("round-matches")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "round_id")
	private Round round;

	@Getter
	@JsonBackReference("table-matches")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "table_id")
	private CompetitionTable competitionTable;

	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_a_id")
	@JsonIdentityReference(alwaysAsId = true)
	private Team teamA;

	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_b_id")
	@JsonIdentityReference(alwaysAsId = true)
	private Team teamB;

	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "referee_id")
	private Referee referee;

	@Getter
	@Enumerated(EnumType.STRING)
	private MatchState state = MatchState.SCHEDULED;

	public Match() {
		// Doesn't need to restrict values
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public void setRound(Round round) {
		this.round = round;
	}

	public void setCompetitionTable(CompetitionTable competitionTable) {
		this.competitionTable = competitionTable;
	}

	public void setTeamA(Team teamA) {
		this.teamA = teamA;
	}

	public void setTeamB(Team teamB) {
		this.teamB = teamB;
	}

	public void setReferee(Referee referee) {
		this.referee = referee;
	}

	public void setState(MatchState state) {
		this.state = state;
	}
}
