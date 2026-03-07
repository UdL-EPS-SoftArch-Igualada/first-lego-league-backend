package cat.udl.eps.softarch.fll.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Coach extends UriEntity<Integer> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Integer id;

	@NotBlank
	private String name;

	@NotBlank
	@Email
	@Column(unique = true)
	private String emailAddress;

	private String phoneNumber;

	@ManyToMany(mappedBy = "trainedBy")
	@ToString.Exclude
	private Set<Team> teams = new HashSet<>();

	public static Coach create(String name, String emailAddress) {
		DomainValidation.requireNonBlank(name, "name");
		DomainValidation.requireValidEmail(emailAddress, "emailAddress");

		Coach coach = new Coach();
		coach.name = name;
		coach.emailAddress = emailAddress;
		return coach;
	}
}
