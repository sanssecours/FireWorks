import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.slf4j.Logger;

import java.util.ArrayList;

import static org.slf4j.LoggerFactory.getLogger;

public class FireWorks {

    private static final Logger slf4jLogger = getLogger(FireWorks.class);

    public static void main(String arguments[]) {
        ContainerReference wood = null;
        MzsCore mozartSpace = DefaultMzsCore.newInstance();
        Capi capi = new Capi(mozartSpace);
        ArrayList<String> result;

        try {
            wood = capi.createContainer();
            capi.write(wood, new Entry("Test"));
            result = capi.read(wood);
            System.err.println("Read: " + result.toString());
            capi.destroyContainer(wood, null);
        } catch(MzsCoreException e) {
            e.printStackTrace();
        }
        mozartSpace.shutdown(true);
    }
}
