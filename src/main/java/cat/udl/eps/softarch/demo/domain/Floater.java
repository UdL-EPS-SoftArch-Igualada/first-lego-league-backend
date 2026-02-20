package cat.udl.eps.softarch.demo.domain;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "floaters")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "assistedTeams")
public class Floater extends Volunteer {

	@PreRemove
	private void removeFromAllTeams() {
		for (Team team : new HashSet<>(assistedTeams)) {
			team.getFloaters().remove(this);
		}
		assistedTeams.clear();
	}

	@NotBlank
	@Column(unique = true)
	private String studentCode;

	@ManyToMany(mappedBy = "floaters")
	@ToString.Exclude
	private Set<Team> assistedTeams = new HashSet<>();
}


