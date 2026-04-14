package cat.udl.eps.softarch.fll.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id", callSuper = false)
public class ScientificProject extends UriEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Integer score;

	private String comments;
	@NotNull
	@ManyToOne
	@JoinColumn(name = "team_name", nullable = false)
	@JsonIdentityReference(alwaysAsId = true)
	private Team team;
	@NotNull
	@ManyToOne
	@JoinColumn(name = "edition_id", nullable = false)
	@JsonIdentityReference(alwaysAsId = true)
	private Edition edition;

	public static ScientificProject create(Integer score) {
		DomainValidation.requireNonNegative(score, "score");

		ScientificProject project = new ScientificProject();
		project.score = score;
		return project;
	}
}

