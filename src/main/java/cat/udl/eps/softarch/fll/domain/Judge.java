package cat.udl.eps.softarch.fll.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "judge")
@Getter
@Setter
public class Judge extends Volunteer {

	@Column(name = "is_expert")
	private boolean expert;

	@ManyToOne
	@JoinColumn(name = "member_of_room")
	private ProjectRoom memberOfRoom;

}
