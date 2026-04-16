package cat.udl.eps.softarch.fll.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "edition_id")
	private Edition edition;

	@OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	@Setter(lombok.AccessLevel.NONE)
	private List<Match> matches = new ArrayList<>();

	public void setMatches(List<Match> matches) {
		if (matches == this.matches) {
			return;
		}

		List<Match> incoming = (matches == null) ? List.of() : new ArrayList<>(matches);
		new ArrayList<>(this.matches).forEach(this::removeMatch);
		incoming.forEach(this::addMatch);
	}

	public void addMatch(Match match) {
		if (match == null) {
			return;
		}

		if (this.matches.stream().anyMatch(m -> m == match)) {
			return;
		}

		Round previousRound = match.getRound();
		if (previousRound != null && previousRound != this) {
			previousRound.getMatches().removeIf(m -> m == match);
		}

		this.matches.add(match);
		match.setRound(this);
	}

	public void removeMatch(Match match) {
		if (match == null) {
			return;
		}

		if (matches.removeIf(m -> m == match)) {
			match.setRound(null);
		}
	}
}
