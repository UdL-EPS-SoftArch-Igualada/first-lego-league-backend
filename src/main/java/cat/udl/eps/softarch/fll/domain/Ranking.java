package cat.udl.eps.softarch.fll.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "rankings", uniqueConstraints = @UniqueConstraint(columnNames = "team_name"))
@Data
@EqualsAndHashCode(callSuper = true)
public class Ranking extends UriEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "team_id", referencedColumnName = "name", nullable = false, unique = true)
	@JsonIdentityReference(alwaysAsId = true)
	private Team team;

	@NotNull
	@Min(value = 0, message = "Total score cannot be negative")
	private Integer totalScore;

	@NotNull
	@Min(value = 1, message = "Ranking position must be at least 1")
	private Integer position;

	@Override
	public Long getId() {
		return id;
	}
}
