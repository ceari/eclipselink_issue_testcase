import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class Child implements Serializable {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    int id;

}
