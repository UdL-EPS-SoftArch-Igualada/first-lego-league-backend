package cat.udl.eps.softarch.demo.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;

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
}
