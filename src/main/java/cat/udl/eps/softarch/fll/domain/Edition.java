package cat.udl.eps.softarch.fll.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.HashSet;
import java.util.Set;

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

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private EditionState state = EditionState.DRAFT;

	@ManyToMany
	@JoinTable(
		name = "edition_teams",
		joinColumns = @JoinColumn(name = "edition_id"),
		inverseJoinColumns = @JoinColumn(name = "team_name"))
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<Team> teams = new HashSet<>();

	protected Edition() {
	}

	public static Edition create(Integer year, String venueName, String description) {
		DomainValidation.requireNonNull(year, "year");
		DomainValidation.requireNonBlank(venueName, "venueName");
		DomainValidation.requireNonBlank(description, "description");

		Edition edition = new Edition();
		edition.year = year;
		edition.venueName = venueName;
		edition.description = description;
		return edition;
	}

	public boolean hasReachedMaxTeams() {
		return teams.size() >= MAX_TEAMS;
	}

	public boolean containsTeam(Team team) {
		return teams.contains(team);
	}
}
