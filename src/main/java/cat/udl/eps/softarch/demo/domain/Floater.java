package cat.udl.eps.softarch.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "floaters")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "assistedTeams")
public class Floater extends Volunteer {

    @NotBlank
    private String studentCode;

    @ManyToMany(mappedBy = "floaters")
    @ToString.Exclude
    private Set<Team> assistedTeams = new HashSet<>();
}


