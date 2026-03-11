package cat.udl.eps.softarch.fll.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "venue")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Venue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@NotBlank
	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@NotBlank
	@Column(name = "city", nullable = false)
	private String city;

	public static Venue create(String name, String city) {
		DomainValidation.requireNonBlank(name, "name");
		DomainValidation.requireNonBlank(city, "city");

		Venue venue = new Venue();
		venue.name = name;
		venue.city = city;
		return venue;
	}
}
