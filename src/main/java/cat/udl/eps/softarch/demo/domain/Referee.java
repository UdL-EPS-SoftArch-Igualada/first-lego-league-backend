package cat.udl.eps.softarch.demo.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "referees")
public class Referee extends Volunteer {

	private boolean isExpert;

	@ManyToOne(fetch = FetchType.LAZY)
	@JsonBackReference
	private CompetitionTable supervisesTable;

	public Referee() {}

	public boolean isExpert() {
		return isExpert;
	}

	public void setExpert(boolean expert) {
		isExpert = expert;
	}

	public CompetitionTable getSupervisesTable() {
		return supervisesTable;
	}

	public void setSupervisesTable(CompetitionTable supervisesTable) {
		this.supervisesTable = supervisesTable;
	}
}
