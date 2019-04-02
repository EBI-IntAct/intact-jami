package uk.ac.ebi.intact.jami.synchronizer.impl;

import org.apache.commons.collections.map.IdentityMap;
import psidev.psi.mi.jami.model.VariableParameterValue;
import psidev.psi.mi.jami.model.VariableParameterValueSet;
import psidev.psi.mi.jami.utils.comparator.experiment.VariableParameterValueComparator;
import uk.ac.ebi.intact.jami.context.SynchronizerContext;
import uk.ac.ebi.intact.jami.merger.IntactDbMergerIgnoringLocalObject;
import uk.ac.ebi.intact.jami.model.extension.IntactVariableParameter;
import uk.ac.ebi.intact.jami.model.extension.IntactVariableParameterValue;
import uk.ac.ebi.intact.jami.synchronizer.AbstractIntactDbSynchronizer;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Synchronizer for variable parameter value
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/14</pre>
 */

public class VariableParameterValueSynchronizer extends AbstractIntactDbSynchronizer<VariableParameterValue, IntactVariableParameterValue> {

    private Map<VariableParameterValue, IntactVariableParameterValue> persistedObjects;
    private Map<VariableParameterValue, IntactVariableParameterValue> convertedObjects;

    private VariableParameterValueComparator variableParameterValueComparator;


    public VariableParameterValueSynchronizer(SynchronizerContext context) {
        super(context, IntactVariableParameterValue.class);

        this.variableParameterValueComparator = new VariableParameterValueComparator();

        this.persistedObjects = new TreeMap<VariableParameterValue, IntactVariableParameterValue>(variableParameterValueComparator);
        this.convertedObjects = new IdentityMap();
    }

    @Override
    protected Object extractIdentifier(IntactVariableParameterValue object) {
        return object.getId();
    }

    @Override
    protected IntactVariableParameterValue instantiateNewPersistentInstance(VariableParameterValue object, Class<? extends IntactVariableParameterValue> intactClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return new IntactVariableParameterValue(object.getValue(), object.getVariableParameter(), object.getOrder());
    }

    @Override
    protected void storeInCache(VariableParameterValue originalObject, IntactVariableParameterValue persistentObject, IntactVariableParameterValue existingInstance) {
        if (existingInstance != null){
            this.persistedObjects.put(originalObject, existingInstance);
        }
        else{
            this.persistedObjects.put(originalObject, persistentObject);
        }
    }

    @Override
    protected IntactVariableParameterValue fetchObjectFromCache(VariableParameterValue object) {
        return this.persistedObjects.get(object);
    }

    @Override
    protected boolean isObjectStoredInCache(VariableParameterValue object) {
        return this.persistedObjects.containsKey(object);
    }

    @Override
    protected boolean containsObjectInstance(VariableParameterValue object) {
        return this.convertedObjects.containsKey(object);
    }

    @Override
    protected void removeObjectInstanceFromIdentityCache(VariableParameterValue object) {
        this.convertedObjects.remove(object);
    }

    @Override
    protected IntactVariableParameterValue fetchMatchingObjectFromIdentityCache(VariableParameterValue object) {
        return this.convertedObjects.get(object);
    }

    @Override
    protected void convertPersistableProperties(IntactVariableParameterValue object) throws SynchronizerException, PersisterException, FinderException {
        synchronizeVariableParameter(object, false);
    }

    @Override
    protected void storeObjectInIdentityCache(VariableParameterValue originalObject, IntactVariableParameterValue persistableObject) {
         this.convertedObjects.put(originalObject, persistableObject);
    }

    @Override
    protected boolean isObjectPartiallyInitialised(VariableParameterValue originalObject) {
        return false;
    }

    public IntactVariableParameterValue find(VariableParameterValue object) throws FinderException {
        if (this.persistedObjects.containsKey(object)){
            return this.persistedObjects.get(object);
        }
        else {
            IntactVariableParameter fetchedVariableParameter = null;
            if (object.getVariableParameter() != null) {
                fetchedVariableParameter = getContext().getVariableParameterSynchronizer().find(object.getVariableParameter());
                // the variable parameter does not exist so the object does not exist
                if (fetchedVariableParameter == null || fetchedVariableParameter.getId() == null) {
                    return null;
                }
            }

            Query query = getEntityManager().createQuery(
                    "select vpv from IntactVariableParameterValue vpv " +
                            (fetchedVariableParameter != null ? "join vpv.variableParameter as vp " : "") +
                            "where "+
                            (object.getValue() != null ? "vpv.value = :vpvValue " : "") +
                            (object.getOrder() != null ? "and vpv.persistentOrder = :vpvOrder " : "") +
                            (fetchedVariableParameter != null ? "and vp.id = :vpId " : "and vpv.variableParameter is null ")
            );

            if (object.getValue() != null) {
                query.setParameter("vpvValue", object.getValue());
            }
            if (object.getOrder() != null) {
                query.setParameter("vpvOrder", object.getOrder());
            }
            if (fetchedVariableParameter != null) {
                query.setParameter("vpId", fetchedVariableParameter.getId());
            }

            Collection<IntactVariableParameterValue> results = query.getResultList();
            if (!results.isEmpty()) {
                //No filtering for the moment
                if (results.size() == 1) {
                    return results.iterator().next();
                } else { //if (results.size() > 1)
                    throw new FinderException("The variable parameter value " + object.toString() + " can match " + results.size() +
                            " variable parameter values in the database and we cannot determine which one is valid: " + results);
                }
            }
            return null;
        }
    }

    @Override
    public Collection<IntactVariableParameterValue> findAll(VariableParameterValue object) {
        //TODO as in experiment
        if (this.persistedObjects.containsKey(object)){
            return Collections.singleton(this.persistedObjects.get(object));
        }
        // only retrieve an object in the cache, otherwise return null
        else {
            Collection<String> fetchedVariableParameters = Collections.EMPTY_LIST;
            if (object.getVariableParameter() != null) {
                fetchedVariableParameters = getContext().getVariableParameterSynchronizer().findAllMatchingAcs(object.getVariableParameter());
                // the variable parameter does not exist so the object does not exist
                if (fetchedVariableParameters.isEmpty()) {
                    return Collections.EMPTY_LIST;
                }
            }

            Query query = getEntityManager().createQuery(
                    "select vpv from IntactVariableParameterValue vpv " +
                            (fetchedVariableParameters != null ? "join vpv.variableParameter as vp " : "") +
                            "where "+
                            (object.getValue() != null ? "vpv.value = :vpvValue " : "") +
                            (object.getOrder() != null ? "and vpv.persistentOrder = :vpvOrder " : "") +
                            (fetchedVariableParameters != null ? "and vp.id in (:vpId) " : "and vpv.variableParameter is null ")
            );

            if (object.getValue() != null) {
                query.setParameter("vpvValue", object.getValue());
            }
            if (object.getOrder() != null) {
                query.setParameter("vpvOrder", object.getOrder());
            }
            if (fetchedVariableParameters != null) {
                query.setParameter("vpId", fetchedVariableParameters);
            }

            Collection<IntactVariableParameterValue> results = query.getResultList();
            return results;
        }
    }

    @Override
    public Collection<String> findAllMatchingAcs(VariableParameterValue object) {
        //TODO as in experiment
        if (this.persistedObjects.containsKey(object)){
            IntactVariableParameterValue fetched = this.persistedObjects.get(object);
            if (fetched.getId() != null){
                return Collections.singleton(Long.toString(fetched.getId()));
            }
            return Collections.EMPTY_LIST;
        }
        // only retrieve an object in the cache, otherwise return null
        else {
            Collection<String> fetchedVariableParameters = Collections.EMPTY_LIST;
            if (object.getVariableParameter() != null) {
                fetchedVariableParameters = getContext().getVariableParameterSynchronizer().findAllMatchingAcs(object.getVariableParameter());
                // the variable parameter does not exist so the object does not exist
                if (fetchedVariableParameters.isEmpty()) {
                    return Collections.EMPTY_LIST;
                }
            }

            Query query = getEntityManager().createQuery(
                    "select distinct vpv from IntactVariableParameterValue vpv " +
                            (fetchedVariableParameters != null ? "join vpv.variableParameter as vp " : "") +
                            "where "+
                            (object.getValue() != null ? "vpv.value = :vpvValue " : "") +
                            (object.getOrder() != null ? "and vpv.persistentOrder = :vpvOrder " : "") +
                            (fetchedVariableParameters != null ? "and vp.id in (:vpId) " : "and vpv.variableParameter is null ")
            );

            if (object.getValue() != null) {
                query.setParameter("vpvValue", object.getValue());
            }
            if (object.getOrder() != null) {
                query.setParameter("vpvOrder", object.getOrder());
            }
            if (fetchedVariableParameters != null) {
                query.setParameter("vpId", fetchedVariableParameters);
            }

            Collection<IntactVariableParameterValue> results = query.getResultList();
            if (!results.isEmpty()) {
                Collection<String> filteredResults = new HashSet<>(results.size());
                for (IntactVariableParameterValue result : results) {
                    filteredResults.add(Long.toString(result.getId()));
                }
                return filteredResults;
            }

            return Collections.EMPTY_LIST;
        }
    }

    public void synchronizeProperties(IntactVariableParameterValue object) throws FinderException, PersisterException, SynchronizerException {
        // only synchornize the value
        if (object.getValue() != null && IntactUtils.MAX_DESCRIPTION_LEN < object.getValue().length()){
            object.setValue(object.getValue().substring(0, IntactUtils.MAX_DESCRIPTION_LEN));
        }

        synchronizeVariableParameter(object, true);

    }

    private void synchronizeVariableParameter(IntactVariableParameterValue object, boolean enableSynchronization) throws FinderException, PersisterException, SynchronizerException {
        object.setVariableParameter( enableSynchronization ?
            getContext().getVariableParameterSynchronizer().synchronize(object.getVariableParameter(), true) :
            getContext().getVariableParameterSynchronizer().convertToPersistentObject(object.getVariableParameter()));
    }

    public void clearCache() {
        this.persistedObjects.clear();
    }

    @Override
    public void deleteRelatedProperties(IntactVariableParameterValue value){
        if (!value.getInteractionParameterValues().isEmpty()){
            Collection<VariableParameterValueSet> existingSet = new ArrayList<VariableParameterValueSet>(value.getInteractionParameterValues());
            // delete interaction sets if it only contained the value that will be removed (avoid to keep empty sets)
            for (VariableParameterValueSet set : existingSet){
                if (set.size() == 1 && set.contains(value)){
                    value.getInteractionParameterValues().remove(set);
                    getEntityManager().remove(set);
                }
            }
        }
    }

    @Override
    protected void resetObjectIdentifier(IntactVariableParameterValue intactObject) {
        intactObject.setId(null);
    }

    @Override
    protected void initialiseDefaultMerger() {
        super.setIntactMerger(new IntactDbMergerIgnoringLocalObject<VariableParameterValue, IntactVariableParameterValue>(this));
    }
}
