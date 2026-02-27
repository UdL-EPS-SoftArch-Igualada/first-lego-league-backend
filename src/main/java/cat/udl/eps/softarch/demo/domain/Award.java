package cat.udl.eps.softarch.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

/**
 * This entity represents an award given to a specific team within a specific edition.
 * Each award belongs to one edition and is granted to one team.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "edition_id"}))
public class Award extends UriEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
     * The name of the award.
     * Must be a non-blank string.
     */
	@NotBlank
	private String name;

	/**
     * The edition this award belongs to.
     * Must be a valid edition.
     */
	@NotNull
	@ManyToOne
	@JoinColumn(name = "edition_id")
	@JsonIdentityReference(alwaysAsId = true)
	private Edition edition;

	/**
     * The team that won this award.
     * Must be a valid team.
     */
	@NotNull
	@ManyToOne
	@JsonIdentityReference(alwaysAsId = true)
	private Team winner;

	@Override
	public Long getId() {
		return this.id;
	}
}