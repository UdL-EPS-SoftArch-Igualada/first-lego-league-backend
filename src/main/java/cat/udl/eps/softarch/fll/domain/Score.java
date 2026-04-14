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
@Data
@EqualsAndHashCode(callSuper = true)
@Table(
		name = "scores",
		uniqueConstraints = @UniqueConstraint(columnNames = {"round_id", "team_name"}))
public class Score extends UriEntity<Long> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Points is mandatory")
	@Min(value = 0, message = "Points cannot be negative")
	private Integer points;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "round_id")
	@JsonIdentityReference(alwaysAsId = true)
	private Round round;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "team_name")
	@JsonIdentityReference(alwaysAsId = true)
	private Team team;

	@Override
	public Long getId() {
		return id;
	}
}
