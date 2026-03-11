package cat.udl.eps.softarch.fll.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "referees")
public class Referee extends Volunteer {

	private boolean expert;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "supervises_table_id")
	@JsonBackReference("table-referees")
	private CompetitionTable supervisesTable;

	public static Referee create(String name, String emailAddress, String phoneNumber) {
		Referee referee = new Referee();
		referee.initFields(name, emailAddress, phoneNumber);
		return referee;
	}

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
