package cat.udl.eps.softarch.fll.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "competition_tables")
public class CompetitionTable extends UriEntity<String> {

	@Id
	@EqualsAndHashCode.Include
	private String id;
	@Getter
	@OneToMany(mappedBy = "competitionTable", cascade = CascadeType.ALL)
	@JsonManagedReference("table-matches")
	private List<Match> matches = new ArrayList<>();
	@Getter
	@OneToMany(mappedBy = "supervisesTable")
	@Size(max = 3, message = "A table can have a maximum of 3 referees")
	@JsonManagedReference("table-referees")
	private List<Referee> referees = new ArrayList<>();

	public static CompetitionTable create(String id) {
		DomainValidation.requireNonBlank(id, "id");
		CompetitionTable table = new CompetitionTable();
		table.id = id;
		return table;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setMatches(List<Match> matches) {
		new ArrayList<>(this.matches).forEach(this::removeMatch);
		if (matches != null) {
			matches.forEach(this::addMatch);
		}
	}

	public void setReferees(List<Referee> referees) {
		new ArrayList<>(this.referees).forEach(this::removeReferee);
		if (referees != null) {
			referees.forEach(this::addReferee);
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
			previousTable.removeMatch(match);
		}
		if (!matches.contains(match)) {
			matches.add(match);
		}
		match.setCompetitionTable(this);
	}

	public void removeMatch(Match match) {
		if (match == null) {
			return;
		}

		matches.remove(match);
		match.setCompetitionTable(null);
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
			previousTable.removeReferee(referee);
		}
		if (referees.contains(referee)) {
			return;
		}
		referees.add(referee);
		referee.setSupervisesTable(this);
	}

	public void removeReferee(Referee referee) {
		if (referee == null) {
			return;
		}

		referees.remove(referee);
		referee.setSupervisesTable(null);
	}
}
