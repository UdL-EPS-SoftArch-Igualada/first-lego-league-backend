package cat.udl.eps.softarch.demo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "project_room")
@Getter
@Setter
public class ProjectRoom {

	@Id
	@Column(name = "room_number")
	private String roomNumber;

	@OneToOne
	@JoinColumn(name = "managed_by_judge_id")
	private Judge managedByJudge;

}
