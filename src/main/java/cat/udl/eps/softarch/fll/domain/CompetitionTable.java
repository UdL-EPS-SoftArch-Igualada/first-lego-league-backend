package cat.udl.eps.softarch.fll.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "competition_tables")
public class CompetitionTable extends UriEntity<String> {

	@Id
	private String id;
	@OneToMany(mappedBy = "competitionTable", cascade = CascadeType.ALL)
	@JsonManagedReference("table-matches")
	private List<Match> matches = new ArrayList<>();
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

	public List<Match> getMatches() {
		return matches;
	}

	public void setMatches(List<Match> matches) {
		new ArrayList<>(this.matches).forEach(this::removeMatch);
		if (matches != null) {
			matches.forEach(this::addMatch);
		}
	}

	public List<Referee> getReferees() {
		return referees;
	}

	public void setReferees(List<Referee> referees) {
		new ArrayList<>(this.referees).forEach(this::removeReferee);
		if (referees != null) {
			referees.forEach(this::addReferee);
		}
	}

	public void addMatch(Match match) {
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
		matches.remove(match);
		match.setCompetitionTable(null);
	}

	public void addReferee(Referee referee) {
		CompetitionTable previousTable = referee.getSupervisesTable();
		if (previousTable != null && previousTable != this) {
			previousTable.removeReferee(referee);
		}
		if (referees.contains(referee)) {
			return;
		}
		if (referees.size() >= 3) {
			throw new IllegalStateException("A table can have a maximum of 3 referees");
		}
		referees.add(referee);
		referee.setSupervisesTable(this);
	}

	public void removeReferee(Referee referee) {
		referees.remove(referee);
		referee.setSupervisesTable(null);
	}
}
