package uk.ac.ebi.intact.jami.dao.impl;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import uk.ac.ebi.intact.jami.context.SynchronizerContext;
import uk.ac.ebi.intact.jami.dao.IntactBaseDao;
import uk.ac.ebi.intact.jami.model.audit.Auditable;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.IntactDbSynchronizer;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/01/14</pre>
 */
public abstract class AbstractIntactBaseDao<I,T extends Auditable> implements IntactBaseDao<T> {

    private Class<T> entityClass;
    private EntityManager entityManager;
    private SynchronizerContext synchronizerContext;

    public AbstractIntactBaseDao( Class<T> entityClass, EntityManager entityManager, SynchronizerContext context) {
        if (entityClass == null){
            throw new IllegalArgumentException("Entity class is mandatory");
        }
        this.entityClass = entityClass;
        if (entityManager == null){
            throw new IllegalArgumentException("The entityManager cannot be null");
        }
        this.entityManager = entityManager;
        if (context == null){
            throw new IllegalArgumentException("The Intact database synchronizer context cannot be null");
        }
        this.synchronizerContext = context;
    }

    public void flush() {
        getEntityManager().flush();
    }

    @Retryable(
            include = PersistenceException.class,
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${retry.maxDelay}", multiplierExpression = "${retry.multiplier}"))
    public List<T> getByQuery(String query, Map<String, Object> queryParameters, int first, int max) {

        Query queryObject = getEntityManager().createQuery(query);
        if (queryParameters != null && !queryParameters.isEmpty()){
            for (Map.Entry<String,Object> param : queryParameters.entrySet()){
                queryObject.setParameter(param.getKey(), param.getValue());
            }
        }
        queryObject.setFirstResult(first);
        queryObject.setMaxResults(max);
        return queryObject.getResultList();
    }

    @Retryable(
            include = PersistenceException.class,
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${retry.maxDelay}", multiplierExpression = "${retry.multiplier}"))
    public long countByQuery(String query, Map<String, Object> queryParameters) {

        Query queryObject = getEntityManager().createQuery(query);
        if (queryParameters != null && !queryParameters.isEmpty()){
            for (Map.Entry<String,Object> param : queryParameters.entrySet()){
                queryObject.setParameter(param.getKey(), param.getValue());
            }
        }
        return getLongQueryResult(queryObject);
    }

    @Retryable(
            include = PersistenceException.class,
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${retry.maxDelay}", multiplierExpression = "${retry.multiplier}"))
    public List<T> getAll() {
        return this.entityManager.createQuery("select o from "+getEntityClass().getName()+" o")
                .getResultList();
    }

    @Retryable(
            include = PersistenceException.class,
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${retry.maxDelay}", multiplierExpression = "${retry.multiplier}"))
    public List<T> getAll(String sortProperty, int firstResult, int maxResults) {
        String strQuery = "select o from "+getEntityClass().getName()+" o order by "+sortProperty;
        Query query = this.entityManager.createQuery(strQuery);
        return query
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .getResultList();
    }

    @Retryable(
            include = PersistenceException.class,
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${retry.maxDelay}", multiplierExpression = "${retry.multiplier}"))
    public List<T> getAllSorted(int firstResult, int maxResults, String sortProperty, boolean ascendant) {
        String strQuery = "select o from "+getEntityClass().getName()+" o order by "+sortProperty+" "+((ascendant)? "asc" : "desc");
        Query query = this.entityManager.createQuery(strQuery);
        return query
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .getResultList();
    }

    @Retryable(
            include = PersistenceException.class,
            maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${retry.maxDelay}", multiplierExpression = "${retry.multiplier}"))
    public long countAll() {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        criteria.select(builder.count(criteria.from(getEntityClass())));
        return getLongQueryResult(this.entityManager.createQuery(criteria));
    }

    public T update(T objToUpdate) throws FinderException,PersisterException,SynchronizerException{
        T objInSync = synchronizeAndUpdateObjectProperties(objToUpdate);
        getDbSynchronizer().flush();
        return objInSync;
    }

    public void persist(T objToPersist) throws FinderException,PersisterException,SynchronizerException{
        synchronizeAndUpdateObjectProperties(objToPersist);
        getDbSynchronizer().flush();
    }

    public void persistAll(Collection<T> objsToPersist) throws FinderException,PersisterException,SynchronizerException{
        for (T obj : objsToPersist){
            persist(obj);
        }
    }

    public void delete(T objToDelete) {
        getDbSynchronizer().delete(objToDelete);
        getDbSynchronizer().flush();
    }

    public void deleteAll(Collection<T> objsToDelete) {
        for (T obj : objsToDelete){
            delete(obj);
        }
    }

    public int deleteAll() {
        return getEntityManager().createQuery("delete from "+getEntityClass().getSimpleName()).executeUpdate();
    }

    public void refresh(T obj) {
         getEntityManager().refresh(obj);
    }

    public void detach(T objToEvict) {
        getEntityManager().detach(objToEvict);
    }

    public T merge(T objToReplicate) throws FinderException,PersisterException,SynchronizerException{
        return synchronizeAndUpdateObjectProperties(objToReplicate);
    }

    public boolean isTransient(T object) {
        return !getEntityManager().contains(object);
    }

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<T> getEntityClass() {
        if (entityClass == null){
            throw new IllegalArgumentException("Entity class is mandatory");
        }
        return this.entityClass;
    }

    public abstract IntactDbSynchronizer getDbSynchronizer();

    protected void synchronizeObjectProperties(T objToUpdate) throws PersisterException, FinderException, SynchronizerException {
        getDbSynchronizer().synchronizeProperties(objToUpdate);
    }

    protected T synchronizeAndUpdateObjectProperties(T objToUpdate) throws PersisterException, FinderException, SynchronizerException {
        return (T)getDbSynchronizer().synchronize(objToUpdate, true);
    }

    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

    protected SynchronizerContext getSynchronizerContext() {
        return synchronizerContext;
    }

    protected Long getLongQueryResult(Query query) {
        try {
            Long result = (Long) query.getSingleResult();
            if (result != null) {
                return result;
            }
        } catch (NoResultException e) {
            // Nothing to do, the query did not find any result
        }
        return 0L;
    }

    protected Integer getIntQueryResult(Query query) {
        try {
            Integer result = (Integer) query.getSingleResult();
            if (result != null) {
                return result;
            }
        } catch (NoResultException e) {
            // Nothing to do, the query did not find any result
        }
        return 0;
    }
}
