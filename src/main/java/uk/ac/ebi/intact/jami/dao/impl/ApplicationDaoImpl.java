package uk.ac.ebi.intact.jami.dao.impl;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import uk.ac.ebi.intact.jami.context.SynchronizerContext;
import uk.ac.ebi.intact.jami.dao.ApplicationDao;
import uk.ac.ebi.intact.jami.model.meta.Application;
import uk.ac.ebi.intact.jami.synchronizer.IntactDbSynchronizer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.List;

/**
 *
 */
public class ApplicationDaoImpl extends AbstractIntactBaseDao<Application, Application> implements ApplicationDao {

    public ApplicationDaoImpl(EntityManager entityManager, SynchronizerContext context) {
        super(Application.class, entityManager, context);
    }

    @Retryable(
            include = PersistenceException.class,
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${retry.maxDelay}", multiplierExpression = "${retry.multiplier}"))
    public Application getByAc(String ac) {
        return getEntityManager().find(getEntityClass(), ac);
    }

    @Override
    @Retryable(
            include = PersistenceException.class,
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${retry.maxDelay}", multiplierExpression = "${retry.multiplier}"))
    public Application getByKey(String key) {
        final Query query = getEntityManager().createQuery( "select app from Application as app where app.key = :key" );
        query.setParameter( "key", key );
        List<Application> apps = query.getResultList();
        if ( apps.isEmpty() ) {
            return null;
        }
        return apps.get( 0 );
    }


    @Override
    public IntactDbSynchronizer getDbSynchronizer() {
        return getSynchronizerContext().getApplicationSynchronizer();
    }
}
