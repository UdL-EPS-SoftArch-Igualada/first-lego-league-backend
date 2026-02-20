package cat.udl.eps.softarch.demo.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;


@Entity
@Table(name = "referees")
public class Referee extends Volunteer {

	private boolean expert;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "supervises_table_id")
	@JsonBackReference("table-referees")
	private CompetitionTable supervisesTable;

	public Referee() {}

	public boolean isExpert() {
		return expert;
	}

	public void setExpert(boolean expert) {
		this.expert = expert;
	}

	public CompetitionTable getSupervisesTable() {
		return supervisesTable;
	}

	public void setSupervisesTable(CompetitionTable supervisesTable) {
		this.supervisesTable = supervisesTable;
	}
}
