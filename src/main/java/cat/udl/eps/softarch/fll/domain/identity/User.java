package cat.udl.eps.softarch.fll.domain.identity;

import java.util.Collection;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import cat.udl.eps.softarch.fll.domain.DomainValidation;
import cat.udl.eps.softarch.fll.domain.UriEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "users") // Avoid collision with system table "user"
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends UriEntity<String> implements UserDetails {

	public static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	@Id
	private String id;
	@NotBlank
	@Email
	@Column(unique = true)
	private String email;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@NotBlank
	@Length(min = 8, max = 256)
	private String password;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private boolean passwordReset;

	@NotBlank
	@Column(nullable = false)
	private String roles = "ROLE_ADMIN";

	public static User create(String id, String email, String password) {
		DomainValidation.requireNonBlank(id, "id");
		DomainValidation.requireValidEmail(email, "email");
		DomainValidation.requireNonBlank(password, "password");
		DomainValidation.requireLengthBetween(password, 8, 256, "password");

		User user = new User();
		user.id = id;
		user.email = email;
		user.password = passwordEncoder.encode(password);
		return user;
	}

	public void encodePassword() {
		this.password = passwordEncoder.encode(this.password);
	}

	public boolean isPasswordPlainText() {
		return password != null && !password.startsWith("$2");
	}

	@Override
	public String getUsername() {
		return id;
	}

	public void setUsername(String username) {
		this.id = username;
	}

	@Override
	@JsonValue(value = false)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return AuthorityUtils.commaSeparatedStringToAuthorityList(roles);
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
