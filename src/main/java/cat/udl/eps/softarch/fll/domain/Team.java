package cat.udl.eps.softarch.fll.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
@Table(name = "team")
public class Team extends UriEntity<String> {

	@Id
	@EqualsAndHashCode.Include
	@NotBlank(message = "Name is mandatory")
	@Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
	@Column(name = "name", length = 50)
	private String name;
	@NotBlank(message = "City is mandatory")
	@Size(max = 100, message = "City name too long")
	@Column(name = "city", length = 100)
	private String city;
	@NotNull(message = "Foundation year is mandatory")
	@Min(value = 1998, message = "Foundation year must be 1998 or later")
	private Integer foundationYear;
	@Size(max = 100, message = "Educational center name too long")
	private String educationalCenter;
	@NotBlank(message = "Category is mandatory")
	private String category;
	@PastOrPresent(message = "Inscription date cannot be in the future")
	@Column(nullable = false)
	private LocalDate inscriptionDate;
	@OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
	@Size(max = 10, message = "A team cannot have more than 10 members")
	@ToString.Exclude
	private List<TeamMember> members = new ArrayList<>();
	@ManyToMany
	@JoinTable(
		name = "team_edition",
		joinColumns = @JoinColumn(name = "team_name", referencedColumnName = "name"),
		inverseJoinColumns = @JoinColumn(name = "edition_id"))
	@JsonIdentityReference(alwaysAsId = true)
	@ToString.Exclude
	private Set<Edition> registeredEditions = new HashSet<>();
	@ManyToMany
	@JoinTable(
		name = "team_coach",
		joinColumns = @JoinColumn(name = "team_name"),
		inverseJoinColumns = @JoinColumn(name = "coach_id"))
	@ToString.Exclude
	private Set<Coach> trainedBy = new HashSet<>();
	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(
		name = "team_floaters",
		joinColumns = @JoinColumn(name = "team_name"),
		inverseJoinColumns = @JoinColumn(name = "floater_id"))
	@ToString.Exclude
	private Set<Floater> floaters = new HashSet<>();

	public static Team create(String name, String city, Integer foundationYear, String category) {
		DomainValidation.requireNonBlank(name, "name");
		DomainValidation.requireLengthBetween(name, 3, 50, "name");
		DomainValidation.requireNonBlank(city, "city");
		DomainValidation.requireLengthBetween(city, 1, 100, "city");
		DomainValidation.requireMin(foundationYear, 1998, "foundationYear");
		DomainValidation.requireNonBlank(category, "category");

		Team team = new Team();
		team.name = name;
		team.city = city;
		team.foundationYear = foundationYear;
		team.category = category;
		team.inscriptionDate = LocalDate.now();
		return team;
	}

	@Override
	public String getId() {
		return name;
	}

	@PrePersist
	public void prePersist() {
		if (this.inscriptionDate == null) {
			this.inscriptionDate = LocalDate.now();
		}
	}

	public void addMember(TeamMember member) {
		if (members.size() >= 10) {
			throw new IllegalStateException("A team cannot have more than 10 members");
		}
		members.add(member);
		member.setTeam(this);
	}

	public void addFloater(Floater floater) {
		if (floaters.contains(floater)) {
			return;
		}
		if (floaters.size() >= 2) {
			throw new IllegalStateException("A team cannot have more than 2 floaters");
		}
		floaters.add(floater);
		floater.getAssistedTeams().add(this);
	}

	public void removeFloater(Floater floater) {
		if (floater == null || !floaters.contains(floater)) {
			return;
		}
		floaters.remove(floater);
		floater.getAssistedTeams().remove(this);
	}

	public void registerEdition(Edition edition) {
		if (edition != null) {
			registeredEditions.add(edition);
		}
	}

	public void addCoach(Coach coach) {
		if (coach == null) {
			throw new IllegalStateException("COACH_NOT_FOUND");
		}
		if (trainedBy.contains(coach)) {
			throw new IllegalStateException("COACH_ALREADY_ASSIGNED");
		}

		if (trainedBy.size() >= 2) {
			throw new IllegalStateException("MAX_COACHES_PER_TEAM_REACHED");
		}

		if (coach.getTeams().size() >= 2) {
			throw new IllegalStateException("MAX_TEAMS_PER_COACH_REACHED");
		}

		trainedBy.add(coach);
		coach.getTeams().add(this);
	}
}
