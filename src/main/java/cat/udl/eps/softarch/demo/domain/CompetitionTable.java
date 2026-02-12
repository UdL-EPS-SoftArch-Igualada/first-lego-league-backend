package cat.udl.eps.softarch.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "competition_tables")
public class CompetitionTable {

	@Id
	private String identifier;

	@OneToMany(mappedBy = "competitionTable")
	private List<Match> matches = new ArrayList<>();

	@OneToMany(mappedBy = "supervisesTable")
	private List<Referee> referees = new ArrayList<>();

	public CompetitionTable() {}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public List<Match> getMatches() {
		return matches;
	}

	public void setMatches(List<Match> matches) {
		this.matches = matches;
	}

	public List<Referee> getReferees() {
		return referees;
	}

	public void setReferees(List<Referee> referees) {
		this.referees = referees;
	}
}
