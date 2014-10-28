package uk.ac.ebi.intact.jami.context;

import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.jami.model.lifecycle.AbstractLifeCycleEvent;
import uk.ac.ebi.intact.jami.model.lifecycle.ComplexLifeCycleEvent;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleEvent;
import uk.ac.ebi.intact.jami.model.lifecycle.PublicationLifeCycleEvent;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.model.user.Preference;
import uk.ac.ebi.intact.jami.model.user.Role;
import uk.ac.ebi.intact.jami.model.user.User;
import uk.ac.ebi.intact.jami.synchronizer.*;

import javax.persistence.EntityManager;

/**
 * Context for synchronizers.
 *
 * It provides all default synchronizers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>19/02/14</pre>
 */

public interface SynchronizerContext {

    /**
     * The entity manager associated with this context. It cannot be null
     * @return
     */
    public EntityManager getEntityManager();

    /**
     * Clear cache of all synchronizers
     */
    public void clearCache();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getGeneralCvSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getCvSynchronizer(String objclass);

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getDatabaseSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getQualifierSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getTopicSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getAliasTypeSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getUnitSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getFeatureTypeSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getExperimentalRoleSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getBiologicalRoleSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getInteractionDetectionMethodSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getInteractionTypeSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getParticipantDetectionMethodSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getExperimentalPreparationSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getInteractorTypeSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getRangeStatusSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getConfidenceTypeSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getParameterTypeSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getCellTypeSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getTissueSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getFeatureDetectionMethodSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getLifecycleStatusSynchronizer();

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getLifecycleEventSynchronizer();

    public <A extends AbstractIntactAlias> AliasSynchronizer<A> getAliasSynchronizer(Class<A> aliasclass);

    public <A extends AbstractIntactAnnotation> AnnotationSynchronizer<A> getAnnotationSynchronizer(Class<A> annotationclass);

    public <A extends AbstractIntactXref> XrefSynchronizer<A> getXrefSynchronizer(Class<A> xrefclass);

    public <A extends AbstractIntactConfidence> ConfidenceSynchronizer<Confidence, A> getConfidenceSynchronizer(Class<A> confidenceclass);

    public <A extends AbstractIntactParameter> ParameterSynchronizer<Parameter, A> getParameterSynchronizer(Class<A> parameterclass);

    public <A extends AbstractLifeCycleEvent> LifecycleEventSynchronizer<A> getLifecycleSynchronizer(Class<A> eventclass);

    public AliasSynchronizer<CvTermAlias> getCvAliasSynchronizer();

    public XrefSynchronizer<CvTermXref> getCvXrefSynchronizer();

    public AnnotationSynchronizer<CvTermAnnotation> getCvAnnotationSynchronizer();

    public IntactDbSynchronizer<Source, IntactSource> getSourceSynchronizer();

    public AliasSynchronizer<SourceAlias> getSourceAliasSynchronizer();

    public XrefSynchronizer<SourceXref> getSourceXrefSynchronizer();

    public AnnotationSynchronizer<SourceAnnotation> getSourceAnnotationSynchronizer();

    public AliasSynchronizer<OrganismAlias> getOrganismAliasSynchronizer();

    public AliasSynchronizer<FeatureEvidenceAlias> getFeatureEvidenceAliasSynchronizer();

    public AliasSynchronizer<ModelledFeatureAlias> getModelledFeatureAliasSynchronizer();

    public AliasSynchronizer<ParticipantEvidenceAlias> getParticipantEvidenceAliasSynchronizer();

    public AliasSynchronizer<ModelledParticipantAlias> getModelledParticipantAliasSynchronizer();

    public AliasSynchronizer<InteractorAlias> getInteractorAliasSynchronizer();

    public XrefSynchronizer<PublicationXref> getPublicationXrefSynchronizer();

    public XrefSynchronizer<ExperimentXref> getExperimentXrefSynchronizer();

    public XrefSynchronizer<InteractionXref> getInteractionXrefSynchronizer();

    public XrefSynchronizer<InteractorXref> getInteractorXrefSynchronizer();

    public XrefSynchronizer<InteractorXref> getComplexXrefSynchronizer();

    public XrefSynchronizer<FeatureEvidenceXref> getFeatureEvidenceXrefSynchronizer();

    public XrefSynchronizer<ModelledFeatureXref> getModelledFeatureXrefSynchronizer();

    public XrefSynchronizer<ParticipantEvidenceXref> getParticipantEvidenceXrefSynchronizer();

    public XrefSynchronizer<ModelledParticipantXref> getModelledParticipantXrefSynchronizer();

    public XrefSynchronizer<ExperimentalResultingSequenceXref> getExperimentalResultingSequenceXrefSynchronizer();

    public XrefSynchronizer<ModelledResultingSequenceXref> getModelledResultingSequenceXrefSynchronizer();

    public AnnotationSynchronizer<PublicationAnnotation> getPublicationAnnotationSynchronizer();

    public AnnotationSynchronizer<ExperimentAnnotation> getExperimentAnnotationSynchronizer();

    public AnnotationSynchronizer<InteractionAnnotation> getInteractionAnnotationSynchronizer();

    public AnnotationSynchronizer<InteractorAnnotation> getInteractorAnnotationSynchronizer();

    public AnnotationSynchronizer<FeatureEvidenceAnnotation> getFeatureEvidenceAnnotationSynchronizer();

    public AnnotationSynchronizer<ModelledFeatureAnnotation> getModelledFeatureAnnotationSynchronizer();

    public AnnotationSynchronizer<ParticipantEvidenceAnnotation> getParticipantEvidenceAnnotationSynchronizer();

    public AnnotationSynchronizer<ModelledParticipantAnnotation> getModelledParticipantAnnotationSynchronizer();

    public AnnotationSynchronizer<CooperativeEffectAnnotation> getCooperativeEffectAnnotationSynchronizer();

    public CooperativeEffectSynchronizer<Preassembly, IntactPreassembly> getPreAssemblySynchronizer();

    public CooperativeEffectSynchronizer<Allostery, AbstractIntactAllostery> getAllosterySynchronizer();

    public CooperativeEffectSynchronizer<CooperativeEffect, AbstractIntactCooperativeEffect> getCooperativeEffectSynchronizer();

    public IntactDbSynchronizer<CooperativityEvidence, IntactCooperativityEvidence> getCooperativityEvidenceSynchronizer();

    public InteractorSynchronizer<Complex, IntactComplex> getComplexSynchronizer();

    public InteractorSynchronizer<Interactor, IntactInteractor> getInteractorSynchronizer();

    public InteractorSynchronizer<Interactor, IntactInteractor> getInteractorBaseSynchronizer();

    public InteractorSynchronizer<Polymer, IntactPolymer> getPolymerSynchronizer();

    public InteractorSynchronizer<Protein, IntactProtein> getProteinSynchronizer();

    public InteractorSynchronizer<NucleicAcid, IntactNucleicAcid> getNucleicAcidSynchronizer();

    public InteractorSynchronizer<Molecule, IntactMolecule> getMoleculeSynchronizer();

    public InteractorSynchronizer<Gene, IntactGene> getGeneSynchronizer();

    public InteractorSynchronizer<InteractorPool, IntactInteractorPool> getInteractorPoolSynchronizer();

    public InteractorSynchronizer<BioactiveEntity, IntactBioactiveEntity> getBioactiveEntitySynchronizer();

    public <T extends AbstractIntactCausalRelationship> IntactDbSynchronizer<CausalRelationship, T> getCausalRelationshipSynchronizer(Class<T> intactClass);

    public IntactDbSynchronizer<CausalRelationship, ModelledCausalRelationship> getModelledCausalRelationshipSynchronizer();

    public IntactDbSynchronizer<CausalRelationship, ExperimentalCausalRelationship> getExperimentalCausalRelationshipSynchronizer();

    public ConfidenceSynchronizer<ModelledConfidence, ComplexConfidence> getComplexConfidenceSynchronizer();

    public ConfidenceSynchronizer<Confidence, InteractionEvidenceConfidence> getInteractionConfidenceSynchronizer();

    public ConfidenceSynchronizer<Confidence, ParticipantEvidenceConfidence> getParticipantEvidenceConfidenceSynchronizer();

    public ParameterSynchronizer<ModelledParameter, ComplexParameter> getComplexParameterSynchronizer();

    public ParameterSynchronizer<Parameter, InteractionEvidenceParameter> getInteractionParameterSynchronizer();

    public ParameterSynchronizer<Parameter, ParticipantEvidenceParameter> getParticipantEvidenceParameterSynchronizer();

    public ParameterSynchronizer<Parameter, FeatureEvidenceParameter> getFeatureParameterSynchronizer();

    public IntactDbSynchronizer<Organism, IntactOrganism> getOrganismSynchronizer();

    public <I extends AbstractIntactRange> IntactDbSynchronizer<Range, I> getRangeSynchronizer(Class<I> intactClass);

    public IntactDbSynchronizer<Range, ModelledRange> getModelledRangeSynchronizer();

    public IntactDbSynchronizer<Range, ExperimentalRange> getExperimentalRangeSynchronizer();

    public IntactDbSynchronizer<Preference, Preference> getPreferenceSynchronizer();

    public IntactDbSynchronizer<Role, Role> getRoleSynchronizer();

    public IntactDbSynchronizer<User, User> getUserSynchronizer();

    public IntactDbSynchronizer<User, User> getUserReadOnlySynchronizer();

    public IntactDbSynchronizer<Publication, IntactPublication> getPublicationSynchronizer();

    public IntactDbSynchronizer<Experiment, IntactExperiment> getExperimentSynchronizer();

    public IntactDbSynchronizer<LifeCycleEvent, ComplexLifeCycleEvent> getComplexLifecycleSynchronizer();

    public IntactDbSynchronizer<LifeCycleEvent, PublicationLifeCycleEvent> getPublicationLifecycleSynchronizer();

    public IntactDbSynchronizer<InteractionEvidence, IntactInteractionEvidence> getInteractionSynchronizer();

    public IntactDbSynchronizer<Feature, AbstractIntactFeature> getFeatureSynchronizer();

    public IntactDbSynchronizer<FeatureEvidence, IntactFeatureEvidence> getFeatureEvidenceSynchronizer();

    public IntactDbSynchronizer<ModelledFeature, IntactModelledFeature> getModelledFeatureSynchronizer();

    public IntactDbSynchronizer<VariableParameter, IntactVariableParameter> getVariableParameterSynchronizer();

    public IntactDbSynchronizer<VariableParameterValue, IntactVariableParameterValue> getVariableParameterValueSynchronizer();

    public IntactDbSynchronizer<VariableParameterValueSet, IntactVariableParameterValueSet> getVariableParameterValueSetSynchronizer();

    public ParticipantSynchronizer<Participant, AbstractIntactParticipant> getParticipantSynchronizer();

    public ParticipantSynchronizer<ModelledParticipant, IntactModelledParticipant> getModelledParticipantSynchronizer();

    public ParticipantSynchronizer<ParticipantEvidence, IntactParticipantEvidence> getParticipantEvidenceSynchronizer();

    public UserContext getUserContext();
}
