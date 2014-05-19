import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.sessions.remote.rmi.RMIConnection;
import org.eclipse.persistence.sessions.remote.rmi.RMIRemoteSessionControllerDispatcher;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertTrue;

/**
 * Testcase for EclipseLink issue
 *
 * getter of a lazy one-to-one relationship (statically woven) throws an exception after serialization:
 *
 * "An attempt was made to traverse a relationship using indirection that had a null
 * Session.  This often occurs when an entity with an uninstantiated LAZY relationship is serialized and that
 * lazy relationship is traversed after serialization.  To avoid this issue, instantiate the LAZY relationship
 * prior to serialization."
 *
 * But the relationship was never uninstantiated and "lazy" ...
 *
 * Probably caused by some issue in the internal ValueHolder that is used for the relationship in a nested unit of work
 * in combination with remote sessions.
 */
@RunWith(JUnit4.class)
public class Testcase {
    @org.junit.Test
    public void test() throws IOException, ClassNotFoundException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test");
        EntityManager em = emf.createEntityManager();

        // Set up a remote session architecture
        RMIRemoteSessionControllerDispatcher controller = new RMIRemoteSessionControllerDispatcher(JpaHelper.getEntityManager(em).getServerSession());
        RMIConnection rmiConnection = new RMIConnection(controller);
        Session session = rmiConnection.createRemoteSession();
        UnitOfWork uow = session.acquireUnitOfWork();

        // Create object in top-level unit of work
        Parent a = (Parent) uow.registerObject(new Parent());

        UnitOfWork nestedUoW = uow.acquireUnitOfWork();
        // Clone object into nested unit of work
        Parent aClone = (Parent) nestedUoW.registerObject(a);

        // Instantiate child relationship
        aClone.child = (Child) nestedUoW.registerNewObject(new Child());

        // Serialize object
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(aClone);
        oos.close();

        // Pretend the serialized object is transferred to some server and de-serialized there:
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        Parent desA = (Parent) ois.readObject();

        assertTrue(desA.getChild() != null); // Exception thrown in getter
    }
}
