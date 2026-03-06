package cat.udl.eps.softarch.fll.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "referees")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Referee extends Volunteer {

	private boolean expert;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "supervises_table_id")
	@JsonBackReference("table-referees")
	private CompetitionTable supervisesTable;
}
