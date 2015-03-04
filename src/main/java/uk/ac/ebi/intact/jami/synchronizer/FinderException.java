package uk.ac.ebi.intact.jami.synchronizer;

/**
 * Exception thrown by IntactDbSynchronizer when an ambiguous object is found in the db
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/01/14</pre>
 */

public class FinderException extends Exception {

    public FinderException() {
        super();
    }

    public FinderException(String message) {
        super(message);
    }

    public FinderException(String message, Throwable cause) {
        super(message, cause);
    }

    public FinderException(Throwable cause) {
        super(cause);
    }
}
