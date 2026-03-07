package cat.udl.eps.softarch.fll.domain;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "rounds")
public class Round extends UriEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private int number;

	@OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("round-matches")
	private List<Match> matches = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "edition_id")
	@JsonIdentityReference(alwaysAsId = true)
	private Edition edition;

	public Round() {}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
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

	public void addMatch(Match match) {
		matches.add(match);
		match.setRound(this);
	}

	public void removeMatch(Match match) {
		matches.remove(match);
		match.setRound(null);
	}

	public Edition getEdition() {
		return edition;
	}

	public void setEdition(Edition edition) {
		this.edition = edition;
	}
}
