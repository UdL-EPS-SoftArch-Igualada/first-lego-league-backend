package cat.udl.eps.softarch.fll.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Edition extends UriEntity<Long> {

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
}
