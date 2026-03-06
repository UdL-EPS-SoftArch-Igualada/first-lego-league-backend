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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Match extends UriEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	private LocalTime startTime;
	private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	private MatchState state = MatchState.SCHEDULED;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "referee_id")
	private Referee referee;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_a_id")
	@JsonIdentityReference(alwaysAsId = true)
	private Team teamA;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_b_id")
	@JsonIdentityReference(alwaysAsId = true)
	private Team teamB;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "round_id")
	@JsonBackReference("round-matches")
	private Round round;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "table_id")
	@JsonBackReference("table-matches")
	private CompetitionTable competitionTable;
}
