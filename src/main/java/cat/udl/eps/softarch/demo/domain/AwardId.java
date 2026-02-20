package cat.udl.eps.softarch.demo.domain;


import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * This entity links the name of the award with the edition it belongs to.
 * It is a composite key for the Award entity.
 */
@Data 
@NoArgsConstructor
@AllArgsConstructor
public class AwardId implements Serializable {
    private String name;
    private Long edition;
}