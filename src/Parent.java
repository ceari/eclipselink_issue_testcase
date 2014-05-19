import org.eclipse.persistence.annotations.ChangeTracking;
import org.eclipse.persistence.annotations.ChangeTrackingType;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Entity
@ChangeTracking(ChangeTrackingType.DEFERRED)
public class Parent implements Serializable {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    int id;

    @OneToOne(fetch = FetchType.LAZY, targetEntity = Child.class, cascade={CascadeType.ALL})
    @JoinColumn(name="child_id")
    public Child child;

    public Child getChild() {
        return this.child;
    }
}
