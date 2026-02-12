package cat.udl.eps.softarch.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "referees")
@PrimaryKeyJoinColumn(name = "volunteer_id")
public class Referee extends Volunteer {

	private Boolean isExpert;

	@ManyToOne
	@JoinColumn(name = "table_id")
	private CompetitionTable supervisesTable;

	public Referee() {}

	public Boolean getIsExpert() {
		return isExpert;
	}

	public void setIsExpert(Boolean expert) {
		isExpert = expert;
	}

	public CompetitionTable getSupervisesTable() {
		return supervisesTable;
	}

	public void setSupervisesTable(CompetitionTable supervisesTable) {
		this.supervisesTable = supervisesTable;
	}
}
