package cat.udl.eps.softarch.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This entity links a team to a match and records the score obtained.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class MatchResult extends UriEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * The score obtained by the team in this match.
	 * Must be a non-negative integer.
	 */
	@NotNull(message = "Score is mandatory")
	@Min(value = 0, message = "Score cannot be negative")
	private Integer score;

	@Override
	public Long getId() {
		return this.id;
	}

	@NotNull
	@ManyToOne
	@JsonIdentityReference(alwaysAsId = true)
	private Match match;

	@NotNull
	@ManyToOne
	@JsonIdentityReference(alwaysAsId = true)
	private Team team;
}