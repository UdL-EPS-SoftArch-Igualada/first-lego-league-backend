package cat.udl.eps.softarch.fll.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "volunteers")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class Volunteer extends UriEntity<Long> {

	@Setter
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@NotBlank(message = "Name is mandatory")
	private String name;

	@NotBlank(message = "Email address is mandatory")
	@Email
	@Column(name = "email_address", unique = true)
	private String emailAddress;

	@NotBlank(message = "Phone number is mandatory")
	private String phoneNumber;
	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "edition_id")
	private Edition edition;

	protected Volunteer() {
	}

	public void setPhoneNumber(String phoneNumber) {
		DomainValidation.requireNonBlank(phoneNumber, "phoneNumber");
		this.phoneNumber = phoneNumber;
	}

	protected void validateFields(String name, String emailAddress, String phoneNumber) {
		DomainValidation.requireNonBlank(name, "name");
		DomainValidation.requireValidEmail(emailAddress, "emailAddress");
		DomainValidation.requireNonBlank(phoneNumber, "phoneNumber");
	}

	protected void initFields(String name, String emailAddress, String phoneNumber) {
		validateFields(name, emailAddress, phoneNumber);

		this.name = name;
		this.emailAddress = emailAddress;
		this.phoneNumber = phoneNumber;
	}
}

