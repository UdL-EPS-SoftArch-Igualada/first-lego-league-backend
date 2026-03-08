package cat.udl.eps.softarch.fll.domain;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Edition extends UriEntity<Long> {

	public static final int MAX_TEAMS = 18;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(name = "edition_year")
	private Integer year;

	@NotBlank
	private String venueName;

	@NotBlank
	private String description;

	@ManyToMany
	@JoinTable(
			name = "edition_teams",
			joinColumns = @JoinColumn(name = "edition_id"),
			inverseJoinColumns = @JoinColumn(name = "team_name"))
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<Team> teams = new HashSet<>();

	public boolean hasReachedMaxTeams() {
		return teams.size() >= MAX_TEAMS;
	}

	public boolean containsTeam(Team team) {
		return teams.contains(team);
	}
}
