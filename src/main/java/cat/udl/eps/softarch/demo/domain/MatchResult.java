package cat.udl.eps.softarch.demo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @Min(value = 0, message = "Score cannot be negative")
    private Integer score;

	/**
     * The match where this result was obtained.
     * Serialized as a URI reference to avoid infinite recursion.
     */
    @NotNull
    @ManyToOne
	@JsonIdentityReference(alwaysAsId = true)
    private Match match;

	/**
     * The team that achieved this result.
     * Serialized as a URI reference to avoid infinite recursion.
     */
    @NotNull
    @ManyToOne
	@JsonIdentityReference(alwaysAsId = true)
    private Team team;
}