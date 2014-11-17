import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

public class FireWorks {

    private static final Logger slf4jLogger = getLogger(FireWorks.class);

    public static void main(String arguments[]) {
        System.out.println("Hello fast Space!");
        slf4jLogger.debug("Hello");
    }
}
