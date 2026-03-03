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

	@OneToMany(mappedBy = "competitionTable", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("table-matches")
	@Setter(lombok.AccessLevel.NONE) // Evitamos el setter automático de Lombok
	private List<Match> matches = new ArrayList<>();

	@OneToMany(mappedBy = "supervisesTable")
	@Size(max = 3, message = "A table can have a maximum of 3 referees")
	@JsonManagedReference("table-referees")
	@Setter(lombok.AccessLevel.NONE) // Evitamos el setter automático de Lombok
	private List<Referee> referees = new ArrayList<>();

	public void setMatches(List<Match> matches) {
		this.matches.clear();
		if (matches != null) {
			matches.forEach(this::addMatch);
		}
	}

	public void addMatch(Match match) {
		if (match != null && !matches.contains(match)) {
			matches.add(match);
			match.setCompetitionTable(this);
		}
	}

	public void removeMatch(Match match) {
		if (matches.remove(match)) {
			match.setCompetitionTable(null);
		}
	}

	public void setReferees(List<Referee> referees) {
		this.referees.clear();
		if (referees != null) {
			referees.forEach(this::addReferee);
		}
	}

	public void addReferee(Referee referee) {
		if (referee == null || referees.contains(referee)) {
			return;
		}
		if (referees.size() >= 3) {
			throw new IllegalStateException("A table can have a maximum of 3 referees");
		}
		referees.add(referee);
		referee.setSupervisesTable(this);
	}

	public void removeReferee(Referee referee) {
		if (referees.remove(referee)) {
			referee.setSupervisesTable(null);
		}
	}
}
