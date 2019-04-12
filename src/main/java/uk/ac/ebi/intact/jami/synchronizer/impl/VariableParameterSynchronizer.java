package uk.ac.ebi.intact.jami.synchronizer.impl;

import org.apache.commons.collections.map.IdentityMap;
import psidev.psi.mi.jami.model.VariableParameter;
import psidev.psi.mi.jami.model.VariableParameterValue;
import psidev.psi.mi.jami.utils.clone.VariableParameterCloner;
import psidev.psi.mi.jami.utils.comparator.experiment.UnambiguousVariableParameterComparator;
import psidev.psi.mi.jami.utils.comparator.experiment.VariableParameterComparator;
import uk.ac.ebi.intact.jami.context.SynchronizerContext;
import uk.ac.ebi.intact.jami.merger.IntactDbMergerIgnoringLocalObject;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.model.extension.IntactExperiment;
import uk.ac.ebi.intact.jami.model.extension.IntactVariableParameter;
import uk.ac.ebi.intact.jami.synchronizer.AbstractIntactDbSynchronizer;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Synchronizer of variable parameter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/14</pre>
 */

public class VariableParameterSynchronizer extends AbstractIntactDbSynchronizer<VariableParameter, IntactVariableParameter> {

    private Map<VariableParameter, IntactVariableParameter>  persistedObjects;
    private Map<VariableParameter, IntactVariableParameter>  convertedObjects;

    private VariableParameterComparator variableParameterComparator;


    public VariableParameterSynchronizer(SynchronizerContext context) {
        super(context, IntactVariableParameter.class);
        this.variableParameterComparator = new UnambiguousVariableParameterComparator();

        this.persistedObjects = new TreeMap<VariableParameter, IntactVariableParameter>(variableParameterComparator);
        this.convertedObjects = new IdentityMap();
    }

    @Override
    protected Object extractIdentifier(IntactVariableParameter object) {
        return object.getId();
    }

    @Override
    protected IntactVariableParameter instantiateNewPersistentInstance(VariableParameter object, Class<? extends IntactVariableParameter> intactClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        IntactVariableParameter param = new IntactVariableParameter(object.getDescription(), object.getExperiment(), object.getUnit());
        VariableParameterCloner.copyAndOverrideVariableParameterProperties(object, param);
        return param;
    }

    @Override
    protected void storeInCache(VariableParameter originalObject, IntactVariableParameter persistentObject, IntactVariableParameter existingInstance) {
        if (existingInstance != null){
            this.persistedObjects.put(originalObject, existingInstance);
        }
        else{
            this.persistedObjects.put(originalObject, persistentObject);
        }    }

    @Override
    protected IntactVariableParameter fetchObjectFromCache(VariableParameter object) {
        return this.persistedObjects.get(object);
    }

    @Override
    protected boolean isObjectStoredInCache(VariableParameter object) {
        return this.persistedObjects.containsKey(object);
    }

    @Override
    protected boolean containsObjectInstance(VariableParameter object) {
        return this.convertedObjects.containsKey(object);
    }

    @Override
    protected void removeObjectInstanceFromIdentityCache(VariableParameter object) {
        this.convertedObjects.remove(object);
    }

    @Override
    protected IntactVariableParameter fetchMatchingObjectFromIdentityCache(VariableParameter object) {
        return this.convertedObjects.get(object);
    }

    @Override
    protected void convertPersistableProperties(IntactVariableParameter object) throws SynchronizerException, PersisterException, FinderException {
        // synchronize unit
        synchronizeUnit(object, false);
        // synchronize values
        synchronizeParameterValues(object, false);
    }

    @Override
    protected void storeObjectInIdentityCache(VariableParameter originalObject, IntactVariableParameter persistableObject) {
        this.convertedObjects.put(originalObject, persistableObject);
    }

    @Override
    protected boolean isObjectPartiallyInitialised(VariableParameter originalObject) {
        return false;
    }

    public IntactVariableParameter find(VariableParameter object) throws FinderException {
        if (object == null){
            return null;
        }
        else if (this.persistedObjects.containsKey(object)){
            return this.persistedObjects.get(object);
        }
        else {
            IntactCvTerm fetchedUnit = null;
            if (object.getUnit() != null) {
                fetchedUnit = getContext().getUnitSynchronizer().find(object.getUnit());
                // the unit does not exist so the object does not exist
                if (fetchedUnit == null || fetchedUnit.getAc() == null) {
                    return null;
                }
            }
            IntactExperiment fetchedExperiment = null;
            if (object.getExperiment() != null){
                fetchedExperiment = getContext().getExperimentSynchronizer().find(object.getExperiment());
                // the experiment does not exist so the object does not exist
                if (fetchedExperiment == null || fetchedExperiment.getAc() == null){
                    return null;
                }
            }

            Query query = getEntityManager().createQuery(
                    "select vp from IntactVariableParameter vp " +
                            (fetchedUnit != null ? "join vp.unit as u " : "") +
                            (fetchedExperiment != null ? "join vp.experiment as exp " : "") +
                            "where "+
                            (object.getDescription() != null ? "vp.description = :vpDescription " : "") +
                            (fetchedUnit != null ? "and u.ac = :unitAc " : "and vp.unit is null ") +
                            (fetchedExperiment != null ? "and exp.ac = :experimentAc " : "and vp.experiment is null ")
            );

            if (object.getDescription() != null) {
                query.setParameter("vpDescription", object.getDescription());
            }
            if (fetchedUnit != null) {
                query.setParameter("unitAc", fetchedUnit.getAc());
            }
            if (fetchedExperiment != null) {
                query.setParameter("experimentAc", fetchedExperiment.getAc());
            }

            Collection<IntactVariableParameter> results = query.getResultList();
            if (!results.isEmpty()) {
                Collection<IntactVariableParameter> filteredResults = new ArrayList<IntactVariableParameter>(results.size());
                for (IntactVariableParameter variableParameter : results) {
                    if (variableParameterComparator.getVariableParameterValueCollectionComparator().compare(object.getVariableValues(), variableParameter.getVariableValues()) == 0) {
                        filteredResults.add(variableParameter);
                    }
                }

                if (filteredResults.size() == 1) {
                    return filteredResults.iterator().next();
                } else if (filteredResults.size() > 1) {
                    throw new FinderException("The variable parameter " + object.toString() + " can match " +
                            filteredResults.size() + " variable parameter in the database and we cannot determine which one is valid: " + filteredResults);
                }
            }
            return null;
        }
    }

    @Override
    public Collection<IntactVariableParameter> findAll(VariableParameter object) {
        if (object == null) {
            return Collections.EMPTY_LIST;
        } else if (this.persistedObjects.containsKey(object)) {
            return Collections.singleton(this.persistedObjects.get(object));
        } else {
            Collection<String> fetchedUnits = Collections.EMPTY_LIST;
            if (object.getUnit() != null) {
                fetchedUnits = getContext().getUnitSynchronizer().findAllMatchingAcs(object.getUnit());
                // the unit does not exist so the object does not exist
                if (fetchedUnits.isEmpty()) {
                    return Collections.EMPTY_LIST;
                }
            }
            Collection<String> fetchedExperiments = Collections.EMPTY_LIST;
            if (object.getExperiment() != null) {
                fetchedExperiments = getContext().getExperimentSynchronizer().findAllMatchingAcs(object.getExperiment());
                // the experiment does not exist so the object does not exist
                if (fetchedExperiments.isEmpty()) {
                    return Collections.EMPTY_LIST;
                }
            }

            Query query = getEntityManager().createQuery(
                    "select vp from IntactVariableParameter vp " +
                            (fetchedUnits != null ? "join vp.unit as u " : "") +
                            (fetchedExperiments != null ? "join vp.experiment as exp " : "") +
                            "where "+
                            (object.getDescription() != null ? "vp.description = :vpDescription " : "") +
                            (fetchedUnits != null ? "and u.ac in (:unitAc) " : "and vp.unit is null ") +
                            (fetchedExperiments != null ? "and exp.ac in (:experimentAc) " : "and vp.experiment is null ")
            );

            if (object.getDescription() != null) {
                query.setParameter("vpDescription", object.getDescription());
            }
            if (fetchedUnits != null) {
                query.setParameter("unitAc", fetchedUnits);
            }
            if (fetchedExperiments != null) {
                query.setParameter("experimentAc", fetchedExperiments);
            }

            Collection<IntactVariableParameter> results = query.getResultList();
            if (!results.isEmpty()) {
                Collection<IntactVariableParameter> filteredResults = new ArrayList<IntactVariableParameter>(results.size());
                for (IntactVariableParameter variableParameter : results) {
                    if (variableParameterComparator.getVariableParameterValueCollectionComparator().compare(object.getVariableValues(), variableParameter.getVariableValues()) == 0) {
                        filteredResults.add(variableParameter);
                    }
                }

                return filteredResults;

            }
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public Collection<String> findAllMatchingAcs(VariableParameter object) {
        if (this.persistedObjects.containsKey(object)){
            IntactVariableParameter fetched = this.persistedObjects.get(object);
            if (fetched.getId() != null){
                return Collections.singleton(Long.toString(fetched.getId()));
            }
            return Collections.EMPTY_LIST;
        }
        // only retrieve an object in the cache, otherwise return null
        else {
        
          Collection<String> fetchedUnits = Collections.EMPTY_LIST;
            if (object.getUnit() != null) {
                fetchedUnits = getContext().getUnitSynchronizer().findAllMatchingAcs(object.getUnit());
                // the unit does not exist so the object does not exist
                if (fetchedUnits.isEmpty()) {
                    return Collections.EMPTY_LIST;
                }
            }
            Collection<String> fetchedExperiments = Collections.EMPTY_LIST;
            if (object.getExperiment() != null) {
                fetchedExperiments = getContext().getExperimentSynchronizer().findAllMatchingAcs(object.getExperiment());
                // the experiment does not exist so the object does not exist
                if (fetchedExperiments.isEmpty()) {
                    return Collections.EMPTY_LIST;
                }
            }

            Query query = getEntityManager().createQuery(
                    "select distinct vp from IntactVariableParameter vp " +
                            (fetchedUnits != null ? "join vp.unit as u " : "") +
                            (fetchedExperiments != null ? "join vp.experiment as exp " : "") +
                            "where "+
                            (object.getDescription() != null ? "vp.description = :vpDescription " : "") +
                            (fetchedUnits != null ? "and u.ac in (:unitAc) " : "and vp.unit is null ") +
                            (fetchedExperiments != null ? "and exp.ac in (:experimentAc) " : "and vp.experiment is null ")
            );

            if (object.getDescription() != null) {
                query.setParameter("vpDescription", object.getDescription());
            }
            if (fetchedUnits != null) {
                query.setParameter("unitAc", fetchedUnits);
            }
            if (fetchedExperiments != null) {
                query.setParameter("experimentAc", fetchedExperiments);
            }

            Collection<IntactVariableParameter> results = query.getResultList();
            if (!results.isEmpty()) {
                Collection<String> filteredResults = new HashSet<>(results.size());
                for (IntactVariableParameter variableParameter : results) {
                    if (variableParameterComparator.getVariableParameterValueCollectionComparator().compare(object.getVariableValues(), variableParameter.getVariableValues()) == 0) {
                        filteredResults.add(Long.toString(variableParameter.getId()));
                    }
                }

                return filteredResults;

            }
            return Collections.EMPTY_LIST;
        }
    }

    public void synchronizeProperties(IntactVariableParameter object) throws FinderException, PersisterException, SynchronizerException {
        // synchronize
        synchronizeDescription(object);
        // synchronize unit
        synchronizeUnit(object, true);
        // synchronize values
        synchronizeParameterValues(object, true);
    }

    protected void synchronizeParameterValues(IntactVariableParameter object, boolean enableSynchronization) throws FinderException, PersisterException, SynchronizerException {
        if (object.areVariableParameterValuesInitialized()){
            List<VariableParameterValue> valuesToPersist = new ArrayList<VariableParameterValue>(object.getVariableValues());
            for (VariableParameterValue value : valuesToPersist){
                VariableParameterValue valueCheck = enableSynchronization ?
                        getContext().getVariableParameterValueSynchronizer().synchronize(value, false) :
                        getContext().getVariableParameterValueSynchronizer().convertToPersistentObject(value);
                // we have a different instance because needed to be synchronized
                if (valueCheck != value){
                    object.getVariableValues().remove(value);
                    if (valueCheck != null && !object.getVariableValues().contains(valueCheck)){
                        object.getVariableValues().add(valueCheck);
                    }
                }
            }
        }
    }

    protected void synchronizeUnit(IntactVariableParameter object, boolean enableSynchronization) throws FinderException, PersisterException, SynchronizerException {
        if (object.getUnit() != null){
            object.setUnit(enableSynchronization ?
                    getContext().getUnitSynchronizer().synchronize(object.getUnit(), true) :
                    getContext().getUnitSynchronizer().convertToPersistentObject(object.getUnit()));
        }
    }

    protected void synchronizeDescription(IntactVariableParameter object) {
        if (object.getDescription() != null && IntactUtils.MAX_DESCRIPTION_LEN < object.getDescription().length()){
            object.setDescription(object.getDescription().substring(0, IntactUtils.MAX_DESCRIPTION_LEN));
        }
    }

    public void clearCache() {
        this.persistedObjects.clear();
    }

    @Override
    protected void resetObjectIdentifier(IntactVariableParameter intactObject) {
        intactObject.setId(null);
    }

    @Override
    protected void initialiseDefaultMerger() {
        super.setIntactMerger(new IntactDbMergerIgnoringLocalObject<>(this));
    }
}
