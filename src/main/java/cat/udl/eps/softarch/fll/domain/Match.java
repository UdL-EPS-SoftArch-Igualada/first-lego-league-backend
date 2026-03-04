package cat.udl.eps.softarch.fll.domain;

import java.time.LocalTime;
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

@Entity
@Table(name = "matches")
public class Match extends UriEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalTime startTime;

	private LocalTime endTime;

	@JsonBackReference("round-matches")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "round_id")
	private Round round;

	@JsonBackReference("table-matches")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "table_id")
	private CompetitionTable competitionTable;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_a_id")
	@JsonIdentityReference(alwaysAsId = true)
	private Team teamA;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_b_id")
	@JsonIdentityReference(alwaysAsId = true)
	private Team teamB;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "referee_id")
	private Referee referee;

	@Enumerated(EnumType.STRING)
	private MatchState state = MatchState.SCHEDULED;

	public Match() {}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public Round getRound() {
		return round;
	}

	public void setRound(Round round) {
		this.round = round;
	}

	public CompetitionTable getCompetitionTable() {
		return competitionTable;
	}

	public void setCompetitionTable(CompetitionTable competitionTable) {
		this.competitionTable = competitionTable;
	}

	public Team getTeamA() {
		return teamA;
	}

	public void setTeamA(Team teamA) {
		this.teamA = teamA;
	}

	public Team getTeamB() {
		return teamB;
	}

	public void setTeamB(Team teamB) {
		this.teamB = teamB;
	}

	public Referee getReferee() {
		return referee;
	}

	public void setReferee(Referee referee) {
		this.referee = referee;
	}

	public MatchState getState() {
		return state;
	}

	public void setState(MatchState state) {
		this.state = state;
	}
}
