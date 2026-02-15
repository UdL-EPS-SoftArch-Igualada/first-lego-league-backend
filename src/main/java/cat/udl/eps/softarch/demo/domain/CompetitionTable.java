package cat.udl.eps.softarch.demo.domain;

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

	@OneToMany(mappedBy = "competitionTable")
	private List<Match> matches = new ArrayList<>();

	@OneToMany(mappedBy = "supervisesTable")
	@Size(min = 2, max = 3, message = "A table must have between 2 and 3 referees")
	@JsonManagedReference
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
		this.matches = matches;
	}

	public List<Referee> getReferees() {
		return referees;
	}

	public void setReferees(List<Referee> referees) {
		this.referees = referees;
	}
}
