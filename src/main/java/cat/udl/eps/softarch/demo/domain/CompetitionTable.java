package cat.udl.eps.softarch.demo.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.List;

@Entity
@Table(name = "competition_tables")
public class CompetitionTable extends UriEntity<String> {

	@Id
	private String id;

	@OneToMany(mappedBy = "competitionTable", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("table-matches")
	private List<Match> matches = new ArrayList<>();

	@OneToMany(mappedBy = "supervisesTable")
	@Size(max = 3, message = "A table can have a maximum of 3 referees")
	@JsonManagedReference("table-referees")
	private List<Referee> referees = new ArrayList<>();

	public CompetitionTable() {}

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
		this.matches.clear();
		if (matches != null) {
			matches.forEach(this::addMatch);
		}
	}

	public List<Referee> getReferees() {
		return referees;
	}

	public void setReferees(List<Referee> referees) {
		this.referees.clear();
		if (referees != null) {
			referees.forEach(this::addReferee);
		}
	}

	public void addMatch(Match match) {
		matches.add(match);
		match.setCompetitionTable(this);
	}

	public void removeMatch(Match match) {
		matches.remove(match);
		match.setCompetitionTable(null);
	}

	public void addReferee(Referee referee) {
		referees.add(referee);
		referee.setSupervisesTable(this);
	}

	public void removeReferee(Referee referee) {
		referees.remove(referee);
		referee.setSupervisesTable(null);
	}
}
