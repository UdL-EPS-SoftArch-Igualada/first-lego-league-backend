package cat.udl.eps.softarch.demo.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
@Table(name = "team")
public class Team extends UriEntity<String> {

	@Override
	public String getId() {
		return name;
	}

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
	@ToString.Exclude
	private List<TeamMember> members = new ArrayList<>();

	public Team(String name) {
		this.name = name;
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
}
