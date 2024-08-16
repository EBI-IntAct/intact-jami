package uk.ac.ebi.intact.jami.dao.impl;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.intact.jami.context.DefaultSynchronizerContext;
import uk.ac.ebi.intact.jami.context.SynchronizerContext;
import uk.ac.ebi.intact.jami.dao.PreferenceDao;
import uk.ac.ebi.intact.jami.model.extension.IntactModelledParticipant;
import uk.ac.ebi.intact.jami.model.user.Preference;
import uk.ac.ebi.intact.jami.synchronizer.IntactDbSynchronizer;
import uk.ac.ebi.intact.jami.synchronizer.impl.PreferenceSynchronizer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.Collection;

/**
 * Implementation of preference dao
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/01/14</pre>
 */
public class PreferenceDaoImpl extends AbstractIntactBaseDao<Preference, Preference> implements PreferenceDao {

    public PreferenceDaoImpl(EntityManager entityManager, SynchronizerContext context) {
        super(Preference.class, entityManager, context);
    }

    @Retryable(
            include = PersistenceException.class,
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${retry.maxDelay}", multiplierExpression = "${retry.multiplier}"))
    public Collection<Preference> getByKey(String key, int first, int max) {
        Query query = getEntityManager().createQuery("select p from Preference p " +
                "where p.key = :keyValue order by p.ac");
        query.setParameter("keyValue", key);
        query.setFirstResult(first);
        query.setMaxResults(max);
        return query.getResultList();
    }

    @Override
    public IntactDbSynchronizer<Preference, Preference> getDbSynchronizer() {
        return getSynchronizerContext().getPreferenceSynchronizer();
    }
}