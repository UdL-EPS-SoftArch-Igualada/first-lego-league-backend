package cat.udl.eps.softarch.fll.domain;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "competition_tables")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class CompetitionTable extends UriEntity<String> {

	@Id
	@EqualsAndHashCode.Include
	private String id;

	@OneToMany(mappedBy = "competitionTable", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JsonManagedReference("table-matches")
	@Setter(lombok.AccessLevel.NONE)
	private List<Match> matches = new ArrayList<>();

	@OneToMany(mappedBy = "supervisesTable")
	@Size(max = 3, message = "A table can have a maximum of 3 referees")
	@JsonManagedReference("table-referees")
	@Setter(lombok.AccessLevel.NONE)
	private List<Referee> referees = new ArrayList<>();

	public void setMatches(List<Match> matches) {
		new ArrayList<>(this.matches).forEach(this::removeMatch);
		if (matches != null) {
			matches.forEach(this::addMatch);
		}
	}

	public void addMatch(Match match) {
		if (match == null) {
			return;
		}

		if (this.matches.stream().anyMatch(m -> m == match)) {
			return;
		}

		CompetitionTable previousTable = match.getCompetitionTable();
		if (previousTable != null && previousTable != this) {
			previousTable.getMatches().removeIf(m -> m == match);
		}

		this.matches.add(match);
		match.setCompetitionTable(this);
	}

	public void removeMatch(Match match) {
		if (match == null) {
			return;
		}

		if (this.matches.removeIf(m -> m == match)) {
			match.setCompetitionTable(null);
		}
	}

	public void setReferees(List<Referee> referees) {
		if (referees == this.referees) {
			return;
		}
		List<Referee> incoming = (referees == null) ? List.of() : new ArrayList<>(referees);
		new ArrayList<>(this.referees).forEach(this::removeReferee);
		incoming.forEach(this::addReferee);
	}

	public void addReferee(Referee referee) {
		if (referee == null) {
			return;
		}

		if (this.referees.stream().anyMatch(r -> r == referee)) {
			return;
		}

		if (this.referees.size() >= 3) {
			throw new IllegalStateException("A table can have a maximum of 3 referees");
		}

		CompetitionTable previousTable = referee.getSupervisesTable();
		if (previousTable != null && previousTable != this) {
			previousTable.getReferees().removeIf(r -> r == referee);
		}

		this.referees.add(referee);
		referee.setSupervisesTable(this);
	}

	public void removeReferee(Referee referee) {
		if (referee == null) {
			return;
		}

		if (this.referees.removeIf(r -> r == referee)) {
			referee.setSupervisesTable(null);
		}
	}
}
