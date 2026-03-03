package cat.udl.eps.softarch.fll.domain;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rounds")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Round extends UriEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@Column(unique = true)
	private int number;

	@OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("round-matches")
	@Setter(lombok.AccessLevel.NONE)
	private List<Match> matches = new ArrayList<>();

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
}
