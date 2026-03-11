package cat.udl.eps.softarch.fll.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "judge")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Judge extends Volunteer {

	@Column(name = "is_expert")
	private boolean expert;

	@ManyToOne
	@JoinColumn(name = "member_of_room")
	private ProjectRoom memberOfRoom;

	public static Judge create(String name, String emailAddress, String phoneNumber) {
		Judge judge = new Judge();
		judge.initFields(name, emailAddress, phoneNumber);
		return judge;
	}

}
