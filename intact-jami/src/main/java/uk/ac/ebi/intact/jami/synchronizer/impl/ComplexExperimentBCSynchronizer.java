package uk.ac.ebi.intact.jami.synchronizer.impl;

import org.apache.commons.collections.map.IdentityMap;
import psidev.psi.mi.jami.enricher.ExperimentEnricher;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.clone.ExperimentCloner;
import uk.ac.ebi.intact.jami.context.SynchronizerContext;
import uk.ac.ebi.intact.jami.merger.ExperimentMergerEnrichOnly;
import uk.ac.ebi.intact.jami.merger.IntactDbMerger;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.model.extension.IntactExperiment;
import uk.ac.ebi.intact.jami.model.extension.IntactOrganism;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;
import uk.ac.ebi.intact.jami.synchronizer.*;
import uk.ac.ebi.intact.jami.synchronizer.listener.impl.DbExperimentEnricherListener;
import uk.ac.ebi.intact.jami.utils.IntactUtils;
import uk.ac.ebi.intact.jami.utils.comparator.IntactComparator;
import uk.ac.ebi.intact.jami.utils.comparator.IntactComplexExperimentComparator;

import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Default synchronizer for experiments attached to complexes.
 * This synchronizer is only for backward compatibility with intact core and should be deleted when intact-core is not needed anymore
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/01/14</pre>
 */

public class ComplexExperimentBCSynchronizer extends AbstractIntactDbSynchronizer<Experiment, IntactExperiment>
        implements IntactExperimentSynchronizer{

    private Map<Experiment, IntactExperiment> persistedObjects;
    private Map<Experiment, IntactExperiment> convertedObjects;

    private IntactComparator<Experiment> experimentComparator;

    private Set<String> persistedNames;

    private DbExperimentEnricherListener enricherListener;

    public ComplexExperimentBCSynchronizer(SynchronizerContext context){
        super(context, IntactExperiment.class);
        // to keep track of persisted cvs
        this.experimentComparator = new IntactComplexExperimentComparator();

        this.persistedObjects = new TreeMap<Experiment, IntactExperiment>(this.experimentComparator);
        this.convertedObjects = new IdentityMap();
        persistedNames = new HashSet<String>();
        enricherListener = new DbExperimentEnricherListener(context, this);
    }

    public IntactExperiment find(Experiment experiment) throws FinderException {
        if (experiment == null){
            return null;
        }
        else if (this.persistedObjects.containsKey(experiment)){
            return this.persistedObjects.get(experiment);
        }
        else{
            IntactPublication fetchedPublication = null;
            if (experiment.getPublication() != null){
                fetchedPublication = getContext().getPublicationSynchronizer().find(experiment.getPublication());
                // the publication does not exist so the experiment does not exist
                if (fetchedPublication == null || fetchedPublication.getAc() == null){
                    return null;
                }
            }
            IntactOrganism fetchedOrganism = null;
            if (experiment.getHostOrganism() != null){
                fetchedOrganism = getContext().getOrganismSynchronizer().find(experiment.getHostOrganism());
                // the organism does not exist so the experiment does not exist
                if (fetchedOrganism == null || fetchedOrganism.getAc() == null){
                    return null;
                }
            }
            IntactCvTerm fetchedDetectionMethod = null;
            if (experiment.getInteractionDetectionMethod() != null){
                fetchedDetectionMethod = getContext().getInteractionDetectionMethodSynchronizer().find(experiment.getInteractionDetectionMethod());
                // the detection method does not exist so the experiment does not exist
                if (fetchedDetectionMethod == null || fetchedDetectionMethod.getAc() == null){
                    return null;
                }
            }

            Query query = getEntityManager().createQuery("select e from IntactExperiment e " +
                    (fetchedOrganism != null ? "join e.hostOrganism as h " : "" ) +
                    (fetchedDetectionMethod != null ? "join e.interactionDetectionMethod as det " : "" ) +
                    (fetchedPublication != null ? "join e.publication as p " : "" ) +
                    "where "+
                    (fetchedOrganism != null ? "h.ac = :orgAc " : "e.hostOrganism is null " ) +
                    (fetchedDetectionMethod != null ? "and det.ac = :detAc " : "and e.interactionDetectionMethod is null " ) +
                    (fetchedPublication != null ? "and p.ac = :pubAc " : "and e.publication is null " ));
            if (fetchedOrganism != null){
                query.setParameter("orgAc", fetchedOrganism.getAc());
            }
            if (fetchedDetectionMethod != null){
                query.setParameter("detAc", fetchedDetectionMethod.getAc());
            }
            if (fetchedPublication != null){
                query.setParameter("pubAc", fetchedPublication.getAc());
            }

            Collection<IntactExperiment> results = query.getResultList();
            if (results.size() == 1){
                return results.iterator().next();
            }
            else if (results.size() > 1){
                throw new FinderException("The experiment "+experiment.toString() + " can match "+results.size()+" experiments in the database and we cannot determine which one is valid: "+results);
            }
            return null;
        }
    }

    @Override
    public Collection<IntactExperiment> findAll(Experiment experiment) {
        if (experiment == null){
            return Collections.EMPTY_LIST;
        }
        else if (this.persistedObjects.containsKey(experiment)){
            return Collections.singleton(this.persistedObjects.get(experiment));
        }
        else{
            Collection<String> fetchedPublications = Collections.EMPTY_LIST;
            if (experiment.getPublication() != null){
                fetchedPublications = getContext().getPublicationSynchronizer().findAllMatchingAcs(experiment.getPublication());
                // the publication does not exist so the experiment does not exist
                if (fetchedPublications.isEmpty()){
                    return Collections.EMPTY_LIST;
                }
            }
            Collection<String> fetchedOrganisms = Collections.EMPTY_LIST;
            if (experiment.getHostOrganism() != null){
                fetchedOrganisms = getContext().getOrganismSynchronizer().findAllMatchingAcs(experiment.getHostOrganism());
                // the organism does not exist so the experiment does not exist
                if (fetchedOrganisms.isEmpty()){
                    return Collections.EMPTY_LIST;
                }
            }
            Collection<String> fetchedDetectionMethods = Collections.EMPTY_LIST;
            if (experiment.getInteractionDetectionMethod() != null){
                fetchedDetectionMethods = getContext().getInteractionDetectionMethodSynchronizer().findAllMatchingAcs(experiment.getInteractionDetectionMethod());
                // the detection method does not exist so the experiment does not exist
                if (fetchedDetectionMethods.isEmpty()){
                    return Collections.EMPTY_LIST;
                }
            }

            Query query = getEntityManager().createQuery("select e from IntactExperiment e " +
                    (!fetchedOrganisms.isEmpty() ? "join e.hostOrganism as h " : "" ) +
                    (!fetchedDetectionMethods.isEmpty() ? "join e.interactionDetectionMethod as det " : "" ) +
                    (!fetchedPublications.isEmpty() ? "join e.publication as p " : "" ) +
                    "where "+
                    (!fetchedOrganisms.isEmpty() ? "h.ac in (:orgAc) " : "e.hostOrganism is null " ) +
                    (!fetchedDetectionMethods.isEmpty() ? "and det.ac in (:detAc) " : "and e.interactionDetectionMethod is null " ) +
                    (!fetchedPublications.isEmpty() ? "and p.ac in (:pubAc) " : "and e.publication is null " ));
            if (fetchedOrganisms != null){
                query.setParameter("orgAc", fetchedOrganisms);
            }
            if (fetchedDetectionMethods != null){
                query.setParameter("detAc", fetchedDetectionMethods);
            }
            if (fetchedPublications != null){
                query.setParameter("pubAc", fetchedPublications);
            }

            return query.getResultList();
        }
    }

    @Override
    public Collection<String> findAllMatchingAcs(Experiment experiment) {
        if (experiment == null){
            return Collections.EMPTY_LIST;
        }
        IntactExperiment exp = this.persistedObjects.get(experiment);

        if (exp != null && exp.getAc() != null ){
            return Collections.singleton(exp.getAc());
        }
        else{
            Collection<String> fetchedPublications = Collections.EMPTY_LIST;
            if (experiment.getPublication() != null){
                fetchedPublications = getContext().getPublicationSynchronizer().findAllMatchingAcs(experiment.getPublication());
                // the publication does not exist so the experiment does not exist
                if (fetchedPublications.isEmpty()){
                    return Collections.EMPTY_LIST;
                }
            }
            Collection<String> fetchedOrganisms = Collections.EMPTY_LIST;
            if (experiment.getHostOrganism() != null){
                fetchedOrganisms = getContext().getOrganismSynchronizer().findAllMatchingAcs(experiment.getHostOrganism());
                // the organism does not exist so the experiment does not exist
                if (fetchedOrganisms.isEmpty()){
                    return Collections.EMPTY_LIST;
                }
            }
            Collection<String> fetchedDetectionMethods = Collections.EMPTY_LIST;
            if (experiment.getInteractionDetectionMethod() != null){
                fetchedDetectionMethods = getContext().getInteractionDetectionMethodSynchronizer().findAllMatchingAcs(experiment.getInteractionDetectionMethod());
                // the detection method does not exist so the experiment does not exist
                if (fetchedDetectionMethods.isEmpty()){
                    return Collections.EMPTY_LIST;
                }
            }

            Query query = getEntityManager().createQuery("select distinct e.ac from IntactExperiment e " +
                    (!fetchedOrganisms.isEmpty() ? "join e.hostOrganism as h " : "" ) +
                    (!fetchedDetectionMethods.isEmpty() ? "join e.interactionDetectionMethod as det " : "" ) +
                    (!fetchedPublications.isEmpty() ? "join e.publication as p " : "" ) +
                    "where "+
                    (!fetchedOrganisms.isEmpty() ? "h.ac in (:orgAc) " : "e.hostOrganism is null " ) +
                    (!fetchedDetectionMethods.isEmpty() ? "and det.ac in (:detAc) " : "and e.interactionDetectionMethod is null " ) +
                    (!fetchedPublications.isEmpty() ? "and p.ac in (:pubAc) " : "and e.publication is null " ));
            if (fetchedOrganisms != null){
                query.setParameter("orgAc", fetchedOrganisms);
            }
            if (fetchedDetectionMethods != null){
                query.setParameter("detAc", fetchedDetectionMethods);
            }
            if (fetchedPublications != null){
                query.setParameter("pubAc", fetchedPublications);
            }

            return query.getResultList();
        }
    }

    public void synchronizeProperties(IntactExperiment intactExperiment) throws FinderException, PersisterException, SynchronizerException {
        // then check shortlabel/synchronize
        prepareAndSynchronizeShortLabel(intactExperiment);
        // then check interaction detection method
        prepareInteractionDetectionMethod(intactExperiment, true);
        // then check participant identification method
        prepareParticipantIdentificationMethod(intactExperiment, true);
        // then check organism
        prepareHostOrganism(intactExperiment, true);
        // then check annotations
        prepareAnnotations(intactExperiment, true);
        // then check xrefs
        prepareXrefs(intactExperiment, true);
    }

    public void clearCache() {
        this.persistedObjects.clear();
        this.convertedObjects.clear();
        this.persistedNames.clear();
        this.enricherListener.getExperimentUpdates().clear();
    }
    @Override
    protected Object extractIdentifier(IntactExperiment object) {
        return object.getAc();
    }

    @Override
    protected IntactExperiment instantiateNewPersistentInstance(Experiment object, Class<? extends IntactExperiment> intactClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        IntactExperiment exp = new IntactExperiment(object.getPublication());
        ExperimentCloner.copyAndOverrideExperimentPropertiesAndInteractionEvidences(object, exp);
        return exp;
    }

    @Override
    protected void storeInCache(Experiment originalObject, IntactExperiment persistentObject, IntactExperiment existingInstance) {
        if (existingInstance != null){
            this.persistedObjects.put(originalObject, existingInstance);
        }
        else{
            this.persistedObjects.put(originalObject, persistentObject);
        }
    }

    @Override
    protected IntactExperiment fetchObjectFromCache(Experiment object) {
        return this.persistedObjects.get(object);
    }

    @Override
    protected boolean isObjectStoredInCache(Experiment object) {
        return this.persistedObjects.containsKey(object);
    }

    @Override
    protected boolean containsObjectInstance(Experiment object) {
        return this.convertedObjects.containsKey(object);
    }

    @Override
    protected void removeObjectInstanceFromIdentityCache(Experiment object) {
        this.convertedObjects.remove(object);
    }

    @Override
    protected IntactExperiment fetchMatchingObjectFromIdentityCache(Experiment object) {
        return this.convertedObjects.get(object);
    }

    @Override
    protected void convertPersistableProperties(IntactExperiment intactExperiment) throws SynchronizerException, PersisterException, FinderException {
        // then check interaction detection method
        prepareInteractionDetectionMethod(intactExperiment, false);
        // then check participant identification method
        prepareParticipantIdentificationMethod(intactExperiment, false);
        // then check organism
        prepareHostOrganism(intactExperiment, false);
        // then check annotations
        prepareAnnotations(intactExperiment, false);
        // then check xrefs
        prepareXrefs(intactExperiment, false);
    }

    @Override
    protected void storeObjectInIdentityCache(Experiment originalObject, IntactExperiment persistableObject) {
        this.convertedObjects.put(originalObject, persistableObject);
    }

    @Override
    protected boolean isObjectPartiallyInitialised(Experiment originalObject) {
        return !this.experimentComparator.canCompare(originalObject);
    }

    protected void prepareHostOrganism(IntactExperiment intactExperiment, boolean enableSynchronization) throws PersisterException, FinderException, SynchronizerException {
        Organism host = intactExperiment.getHostOrganism();
        if (host != null){
            intactExperiment.setHostOrganism(enableSynchronization ?
                    getContext().getOrganismSynchronizer().synchronize(host, true) :
                    getContext().getOrganismSynchronizer().convertToPersistentObject(host));
        }
    }

    protected void prepareInteractionDetectionMethod(IntactExperiment intactExperiment, boolean enableSynchronization) throws PersisterException, FinderException, SynchronizerException {
        CvTerm detectionMethod = intactExperiment.getInteractionDetectionMethod();
        if (detectionMethod != null){
            intactExperiment.setInteractionDetectionMethod(enableSynchronization ?
                    getContext().getInteractionDetectionMethodSynchronizer().synchronize(detectionMethod, true) :
                    getContext().getInteractionDetectionMethodSynchronizer().convertToPersistentObject(detectionMethod));
        }
    }

    protected void prepareXrefs(IntactExperiment intactExperiment, boolean enableSynchronization) throws FinderException, PersisterException, SynchronizerException {
        if (intactExperiment.areXrefsInitialized()){
            List<Xref> xrefsToPersist = new ArrayList<Xref>(intactExperiment.getXrefs());
            intactExperiment.getXrefs().clear();
            int index = 0;
            try{
                for (Xref xref : xrefsToPersist){
                    // do not persist or merge xrefs because of cascades
                    Xref expRef = enableSynchronization ?
                            getContext().getExperimentXrefSynchronizer().synchronize(xref, false) :
                            getContext().getExperimentXrefSynchronizer().convertToPersistentObject(xref);
                    // we have a different instance because needed to be synchronized
                    if (expRef != null && !intactExperiment.getXrefs().contains(expRef)){
                        intactExperiment.getXrefs().add(expRef);
                    }
                    index++;
                }
            }
            finally {
                // always add previous properties in case of exception
                if (index < xrefsToPersist.size() - 1){
                    for (int i = index; i < xrefsToPersist.size(); i++){
                        intactExperiment.getXrefs().add(xrefsToPersist.get(i));
                    }
                }
            }
        }
    }

    protected void prepareAnnotations(IntactExperiment intactExperiment, boolean enableSynchronization) throws FinderException, PersisterException, SynchronizerException {
        if (intactExperiment.areAnnotationsInitialized()){
            List<Annotation> annotationsToPersist = new ArrayList<Annotation>(intactExperiment.getAnnotations());
            intactExperiment.getAnnotations().clear();
            int index = 0;
            try{
                for (Annotation annotation : annotationsToPersist){
                    // do not persist or merge annotations because of cascades
                    Annotation expAnnotation = enableSynchronization ?
                            getContext().getExperimentAnnotationSynchronizer().synchronize(annotation, false) :
                            getContext().getExperimentAnnotationSynchronizer().convertToPersistentObject(annotation);
                    // we have a different instance because needed to be synchronized
                    if (expAnnotation != null && !intactExperiment.getAnnotations().contains(expAnnotation)){
                        intactExperiment.getAnnotations().add(expAnnotation);
                    }
                    index++;
                }
            }
            finally {
                // always add previous properties in case of exception
                if (index < annotationsToPersist.size() - 1){
                    for (int i = index; i < annotationsToPersist.size(); i++){
                        intactExperiment.getAnnotations().add(annotationsToPersist.get(i));
                    }
                }
            }
        }
    }

    protected void prepareParticipantIdentificationMethod(IntactExperiment intactExperiment, boolean enableSynchronization) throws FinderException, PersisterException, SynchronizerException {
        CvTerm detectionMethod = intactExperiment.getParticipantIdentificationMethod();
        if (detectionMethod != null){
            intactExperiment.setParticipantIdentificationMethod(enableSynchronization ?
                    getContext().getParticipantDetectionMethodSynchronizer().synchronize(detectionMethod, true) :
                    getContext().getParticipantDetectionMethodSynchronizer().convertToPersistentObject(detectionMethod));
        }
    }

    public void prepareAndSynchronizeShortLabel(IntactExperiment intactExperiment) throws SynchronizerException {
        // first initialise shortlabel if not done
        if (intactExperiment.getShortLabel() == null){
            intactExperiment.setShortLabel(intactExperiment.getHostOrganism() != null ?
                    intactExperiment.getHostOrganism().getCommonName() :
                    IntactUtils.generateAutomaticExperimentShortlabelFor(intactExperiment, IntactUtils.MAX_SHORT_LABEL_LEN));
        }

        String oldLabel = intactExperiment.getShortLabel();

        // then synchronize with database
        IntactUtils.synchronizeExperimentComplexShortLabel(intactExperiment, getEntityManager(), this.persistedNames);

        // only add name as persisted name if new object persisted or update in shortlabel
        if (intactExperiment.getAc() == null){
            this.persistedNames.add(intactExperiment.getShortLabel());
        }
        else if (!oldLabel.equals(intactExperiment.getShortLabel())){
            this.persistedNames.add(intactExperiment.getShortLabel());
        }
    }

    @Override
    protected void initialiseDefaultMerger() {
        ExperimentMergerEnrichOnly merger = new ExperimentMergerEnrichOnly();
        merger.setExperimentEnricherListener(this.enricherListener);
        super.setIntactMerger(merger);
    }

    @Override
    public void setIntactMerger(IntactDbMerger<Experiment, IntactExperiment> intactMerger) {
        if (intactMerger instanceof ExperimentEnricher){
            ((ExperimentEnricher)intactMerger).setExperimentEnricherListener(this.enricherListener);
        }
        super.setIntactMerger(intactMerger);
    }

    @Override
    public void deleteRelatedProperties(IntactExperiment intactParticipant){
        for (InteractionEvidence f : intactParticipant.getInteractionEvidences()){
            getContext().getInteractionSynchronizer().delete(f);
        }
        intactParticipant.getInteractionEvidences().clear();
    }

    @Override
    protected void resetObjectIdentifier(IntactExperiment intactObject) {
        intactObject.setAc(null);
    }
}
