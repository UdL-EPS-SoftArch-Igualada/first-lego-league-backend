package cat.udl.eps.softarch.fll.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "floaters")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Floater extends Volunteer {

	@NotBlank(message = "Student code is mandatory")
	@Column(unique = true)
	private String studentCode;

	@ManyToMany(mappedBy = "floaters")
	@ToString.Exclude
	private Set<Team> assistedTeams = new HashSet<>();

	public static Floater create(String name, String emailAddress, String phoneNumber, String studentCode) {
		DomainValidation.requireNonBlank(studentCode, "studentCode");

		Floater floater = new Floater();
		floater.initFields(name, emailAddress, phoneNumber);
		floater.studentCode = studentCode;
		return floater;
	}
}

