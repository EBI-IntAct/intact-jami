package uk.ac.ebi.intact.jami.synchronizer.impl;

import org.apache.commons.collections.map.IdentityMap;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.clone.InteractionCloner;
import uk.ac.ebi.intact.jami.context.SynchronizerContext;
import uk.ac.ebi.intact.jami.merger.InteractionEvidenceMergerEnrichOnly;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.synchronizer.*;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Default synchronizer for interaction evidences
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/01/14</pre>
 */

public class InteractionEvidenceSynchronizer extends AbstractIntactDbSynchronizer<InteractionEvidence, IntactInteractionEvidence> {

    private Map<InteractionEvidence, IntactInteractionEvidence> persistedObjects;
    private Map<InteractionEvidence, IntactInteractionEvidence> convertedObjects;

    public InteractionEvidenceSynchronizer(SynchronizerContext context){
        super(context, IntactInteractionEvidence.class);
        // to keep track of persisted cvs
        this.persistedObjects = new IdentityMap();
        this.convertedObjects = new IdentityMap();
    }

    public IntactInteractionEvidence find(InteractionEvidence interaction) throws FinderException {
        if (this.persistedObjects.containsKey(interaction)){
            return this.persistedObjects.get(interaction);
        }
        else{
            return null;
        }
    }

    @Override
    public Collection<IntactInteractionEvidence> findAll(InteractionEvidence interaction) {
        if (this.persistedObjects.containsKey(interaction)){
            return Collections.singleton(this.persistedObjects.get(interaction));
        }
        else{
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public Collection<String> findAllMatchingAcs(InteractionEvidence interaction) {
        if (this.persistedObjects.containsKey(interaction)){
            IntactInteractionEvidence evCached = this.persistedObjects.get(interaction);
            if (evCached != null && evCached.getAc() != null){
               return Collections.singleton(evCached.getAc());
            }
            return Collections.EMPTY_LIST;
        }
        else{
            return Collections.EMPTY_LIST;
        }
    }

    public void synchronizeProperties(IntactInteractionEvidence intactInteraction) throws FinderException, PersisterException, SynchronizerException {
        // then check shortlabel/synchronize
        prepareAndSynchronizeShortLabel(intactInteraction);
        // then check interaction detection method
        prepareInteractionType(intactInteraction, true);
        // then check participant identification method
        prepareConfidences(intactInteraction, true);
        // then check organism
        prepareParameters(intactInteraction, true);
        // then check annotations
        prepareAnnotations(intactInteraction, true);
        // then check xrefs
        prepareXrefs(intactInteraction, true);
        // then check interactions
        prepareParticipants(intactInteraction, true);
        // then check variable parameters
        prepareVariableParametersValues(intactInteraction, true);
    }

    public void clearCache() {
        this.persistedObjects.clear();
        this.convertedObjects.clear();
    }

    @Override
    protected Object extractIdentifier(IntactInteractionEvidence object) {
        return object.getAc();
    }

    @Override
    protected IntactInteractionEvidence instantiateNewPersistentInstance(InteractionEvidence object, Class<? extends IntactInteractionEvidence> intactClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        IntactInteractionEvidence inter = new IntactInteractionEvidence();
        InteractionCloner.copyAndOverrideInteractionEvidenceProperties(object, inter, false, false);
        return inter;
    }

    @Override
    protected void storeInCache(InteractionEvidence originalObject, IntactInteractionEvidence persistentObject, IntactInteractionEvidence existingInstance) {
        if (existingInstance != null){
            this.persistedObjects.put(originalObject, existingInstance);
        }
        else{
            this.persistedObjects.put(originalObject, persistentObject);
        }
    }

    @Override
    protected IntactInteractionEvidence fetchObjectFromCache(InteractionEvidence object) {
        return this.persistedObjects.get(object);
    }

    @Override
    protected boolean isObjectStoredInCache(InteractionEvidence object) {
        return this.persistedObjects.containsKey(object);
    }

    @Override
    protected boolean containsObjectInstance(InteractionEvidence object) {
        return this.convertedObjects.containsKey(object);
    }

    @Override
    protected void removeObjectInstanceFromIdentityCache(InteractionEvidence object) {
        this.convertedObjects.remove(object);
    }

    @Override
    protected IntactInteractionEvidence fetchMatchingObjectFromIdentityCache(InteractionEvidence object) {
        return this.convertedObjects.get(object);
    }

    @Override
    protected void convertPersistableProperties(IntactInteractionEvidence intactInteraction) throws SynchronizerException, PersisterException, FinderException {
        // then check interaction detection method
        prepareInteractionType(intactInteraction, false);
        // then check participant identification method
        prepareConfidences(intactInteraction, false);
        // then check organism
        prepareParameters(intactInteraction, false);
        // then check annotations
        prepareAnnotations(intactInteraction, false);
        // then check xrefs
        prepareXrefs(intactInteraction, false);
        // then check interactions
        prepareParticipants(intactInteraction, false);
        // then check variable parameters
        prepareVariableParametersValues(intactInteraction, false);
    }

    @Override
    protected void storeObjectInIdentityCache(InteractionEvidence originalObject, IntactInteractionEvidence persistableObject) {
         this.convertedObjects.put(originalObject, persistableObject);
    }

    @Override
    protected boolean isObjectDirty(InteractionEvidence originalObject) {
        return false;
    }

    protected void prepareVariableParametersValues(IntactInteractionEvidence intactInteraction, boolean enableSynchronization) throws PersisterException, FinderException, SynchronizerException {

        if (intactInteraction.areVariableParameterValuesInitialized()){
            Collection<VariableParameterValueSet> parametersToPersist = new ArrayList<VariableParameterValueSet>(intactInteraction.getVariableParameterValues());
            for (VariableParameterValueSet param : parametersToPersist){
                // do not persist or merge parameters because of cascades
                VariableParameterValueSet expParam = enableSynchronization ?
                        getContext().getVariableParameterValueSetSynchronizer().synchronize(param, false) :
                        getContext().getVariableParameterValueSetSynchronizer().convertToPersistentObject(param);
                // we have a different instance because needed to be synchronized
                if (expParam != param){
                    intactInteraction.getVariableParameterValues().remove(param);
                    intactInteraction.getVariableParameterValues().add(expParam);
                }
            }
        }
    }

    protected void prepareParticipants(IntactInteractionEvidence intactInteraction, boolean enableSynchronization) throws PersisterException, FinderException, SynchronizerException {
        if (intactInteraction.areParticipantsInitialized()){
            Collection<ParticipantEvidence> participantsToPersist = new ArrayList<ParticipantEvidence>(intactInteraction.getParticipants());
            for (ParticipantEvidence participant : participantsToPersist){
                // reinit parent
                participant.setInteraction(intactInteraction);
                // do not persist or merge participants because of cascades
                ParticipantEvidence expPart = enableSynchronization ?
                        getContext().getParticipantEvidenceSynchronizer().synchronize(participant, false) :
                        getContext().getParticipantEvidenceSynchronizer().convertToPersistentObject(participant);
                // we have a different instance because needed to be synchronized
                if (expPart != participant){
                    intactInteraction.getParticipants().remove(participant);
                    intactInteraction.addParticipant(expPart);
                }
            }
        }
    }

    protected void prepareParameters(IntactInteractionEvidence intactInteraction, boolean enableSynchronization) throws PersisterException, FinderException, SynchronizerException {
        if (intactInteraction.areParametersInitialized()){
            Collection<Parameter> parametersToPersist = new ArrayList<Parameter>(intactInteraction.getParameters());
            for (Parameter param : parametersToPersist){
                // do not persist or merge parameters because of cascades
                Parameter expPar = enableSynchronization ?
                        getContext().getInteractionParameterSynchronizer().synchronize(param, false) :
                        getContext().getInteractionParameterSynchronizer().convertToPersistentObject(param);
                // we have a different instance because needed to be synchronized
                if (expPar != param){
                    intactInteraction.getParameters().remove(param);
                    intactInteraction.getParameters().add(expPar);
                }
            }
        }
    }

    protected void prepareInteractionType(IntactInteractionEvidence intactInteraction, boolean enableSynchronization) throws PersisterException, FinderException, SynchronizerException {
        CvTerm type = intactInteraction.getInteractionType();
        if (type != null){
            intactInteraction.setInteractionType(enableSynchronization ?
                    getContext().getInteractionTypeSynchronizer().synchronize(type, true) :
                    getContext().getInteractionTypeSynchronizer().convertToPersistentObject(type));
        }
    }

    protected void prepareXrefs(IntactInteractionEvidence intactInteraction, boolean enableSynchronization) throws FinderException, PersisterException, SynchronizerException {
        if (intactInteraction.areXrefsInitialized()){
            List<Xref> xrefsToPersist = new ArrayList<Xref>(intactInteraction.getXrefs());
            for (Xref xref : xrefsToPersist){
                // do not persist or merge xrefs because of cascades
                Xref expRef = enableSynchronization ?
                        getContext().getInteractionXrefSynchronizer().synchronize(xref, false) :
                        getContext().getInteractionXrefSynchronizer().convertToPersistentObject(xref);
                // we have a different instance because needed to be synchronized
                if (expRef != xref){
                    intactInteraction.getXrefs().remove(xref);
                    intactInteraction.getXrefs().add(expRef);
                }
            }
        }
    }

    protected void prepareAnnotations(IntactInteractionEvidence intactInteraction, boolean enableSynchronization) throws FinderException, PersisterException, SynchronizerException {
        if (intactInteraction.areAnnotationsInitialized()){
            List<Annotation> annotationsToPersist = new ArrayList<Annotation>(intactInteraction.getDbAnnotations());
            for (Annotation annotation : annotationsToPersist){
                // do not persist or merge annotations because of cascades
                Annotation expAnnotation = enableSynchronization ?
                        getContext().getInteractionAnnotationSynchronizer().synchronize(annotation, false) :
                        getContext().getInteractionAnnotationSynchronizer().convertToPersistentObject(annotation);
                // we have a different instance because needed to be synchronized
                if (expAnnotation != annotation){
                    intactInteraction.getDbAnnotations().remove(annotation);
                    intactInteraction.getDbAnnotations().add(expAnnotation);
                }
            }
        }
    }

    protected void prepareConfidences(IntactInteractionEvidence intactInteraction, boolean enableSynchronization) throws FinderException, PersisterException, SynchronizerException {
        if (intactInteraction.areConfidencesInitialized()){
            List<Confidence> confsToPersist = new ArrayList<Confidence>(intactInteraction.getConfidences());
            for (Confidence confidence : confsToPersist){
                // do not persist or merge confidences because of cascades
                Confidence expConf = enableSynchronization ?
                        getContext().getInteractionConfidenceSynchronizer().synchronize(confidence, false) :
                        getContext().getInteractionConfidenceSynchronizer().convertToPersistentObject(confidence);
                // we have a different instance because needed to be synchronized
                if (expConf != confidence){
                    intactInteraction.getConfidences().remove(confidence);
                    intactInteraction.getConfidences().add(expConf);
                }
            }
        }
    }

    protected void prepareAndSynchronizeShortLabel(IntactInteractionEvidence intactInteraction) throws SynchronizerException {
        // first initialise shortlabel if not done
        if (intactInteraction.getShortName() == null){
            intactInteraction.setShortName(IntactUtils.generateAutomaticInteractionEvidenceShortlabelFor(intactInteraction, IntactUtils.MAX_SHORT_LABEL_LEN));
        }

        // then synchronize with database
        String name;
        List<String> existingInteractions;
        do{
            name = intactInteraction.getShortName().trim().toLowerCase();
            existingInteractions = Collections.EMPTY_LIST;

            // check if short name already exist, if yes, synchronize with existing label
            Query query = getEntityManager().createQuery("select i.shortName from IntactInteractionEvidence i " +
                    "where (i.shortName = :name or i.shortName like :nameWithSuffix) "
                    + (intactInteraction.getAc() != null ? "and i.ac <> :interAc" : ""));
            query.setParameter("name", name);
            query.setParameter("nameWithSuffix", name+"-%");
            if (intactInteraction.getAc() != null){
                query.setParameter("interAc", intactInteraction.getAc());
            }
            existingInteractions = query.getResultList();
            if (!existingInteractions.isEmpty()){
                String nameInSync = IntactUtils.synchronizeShortlabel(name, existingInteractions, IntactUtils.MAX_SHORT_LABEL_LEN, true);
                if (!nameInSync.equals(name)){
                    intactInteraction.setShortName(nameInSync);
                }
                else{
                    break;
                }
            }
            else{
                intactInteraction.setShortName(name);
            }
        }
        while(!existingInteractions.isEmpty());
    }

    @Override
    protected void initialiseDefaultMerger() {
        super.setIntactMerger(new InteractionEvidenceMergerEnrichOnly());
    }

    @Override
    public void deleteRelatedProperties(IntactInteractionEvidence intactParticipant){
        for (Object f : intactParticipant.getParticipants()){
            getContext().getParticipantEvidenceSynchronizer().delete((ParticipantEvidence)f);
        }
        intactParticipant.getParticipants().clear();
    }
}
