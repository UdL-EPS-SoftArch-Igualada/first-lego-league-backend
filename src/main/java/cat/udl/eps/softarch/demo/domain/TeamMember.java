package cat.udl.eps.softarch.demo.domain;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "team_member")
public class TeamMember {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Name is mandatory")
	@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
	private String name;

	@NotNull(message = "Birth date is mandatory")
	@Past(message = "Birth date must be in the past")
	private LocalDate birthDate;

	@Size(max = 20, message = "Gender too long")
	private String gender;

	@Size(max = 10, message = "T-shirt size too long")
	private String tShirtSize;

	@NotBlank(message = "Role is mandatory")
	@Column(length = 50)
	private String role;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_name", nullable = false)
	@NotNull(message = "A member must belong to a team")
	private Team team;
}
