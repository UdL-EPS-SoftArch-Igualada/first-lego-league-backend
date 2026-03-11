package cat.udl.eps.softarch.fll.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "team_member")
public class TeamMember extends UriEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;
	@NotBlank(message = "Member name is mandatory")
	@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
	private String name;
	@NotNull(message = "Birth date cannot be null")
	@Past(message = "Birth date must be in the past")
	private LocalDate birthDate;
	@Size(max = 20, message = "Gender too long")
	private String gender;
	@Size(max = 10, message = "T-shirt size too long")
	private String tShirtSize;
	@NotBlank(message = "Role is mandatory")
	@Size(max = 50, message = "Role name too long")
	@Column(length = 50)
	private String role;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "team_name", nullable = false)
	@NotNull(message = "A member must belong to a team")
	@JsonIdentityReference(alwaysAsId = true)
	private Team team;

	public static TeamMember create(String name, String role, LocalDate birthDate, Team team) {
		DomainValidation.requireNonBlank(name, "name");
		DomainValidation.requireLengthBetween(name, 2, 100, "name");
		DomainValidation.requireNonBlank(role, "role");
		DomainValidation.requireMaxLength(role, 50, "role");
		DomainValidation.requirePast(birthDate, "birthDate");
		DomainValidation.requireNonNull(team, "team");

		TeamMember member = new TeamMember();
		member.name = name;
		member.role = role;
		member.birthDate = birthDate;
		team.addMember(member);
		return member;
	}

	@Override
	public Long getId() {
		return id;
	}
}
