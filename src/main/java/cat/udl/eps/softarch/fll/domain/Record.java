package cat.udl.eps.softarch.fll.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.ZonedDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Record extends UriEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	private String name;

	private String description;

	@DateTimeFormat
	private ZonedDateTime created;

	@DateTimeFormat
	private ZonedDateTime modified;

	@ManyToOne
	@JsonIdentityReference(alwaysAsId = true)
	private User owner;

	public static Record create(String name) {
		DomainValidation.requireNonBlank(name, "name");

		Record newRecord = new Record();
		newRecord.name = name;
		return newRecord;
	}
}
