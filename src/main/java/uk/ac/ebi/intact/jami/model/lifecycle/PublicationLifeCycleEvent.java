package uk.ac.ebi.intact.jami.model.lifecycle;

import uk.ac.ebi.intact.jami.model.user.User;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

/**
 * Publication lifecycle event
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 */
@Entity
@Table(
        name = "ia_lifecycle_event",
        indexes = {
                @Index(name = "idx_event_who", columnList = "user_ac"),
                @Index(name = "idx_event_event", columnList = "event_ac")
        })
public class PublicationLifeCycleEvent extends AbstractLifeCycleEvent {

    protected PublicationLifeCycleEvent() {
    }

    public PublicationLifeCycleEvent(LifeCycleEventType event, User who, Date when, String note) {
        super(event, who, when, note);
    }

    /**
     * Date is set to current time.
     * @param event
     * @param who
     * @param note
     */
    public PublicationLifeCycleEvent(LifeCycleEventType event, User who, String note) {
        super( event, who, new Date(), note );
    }
}
