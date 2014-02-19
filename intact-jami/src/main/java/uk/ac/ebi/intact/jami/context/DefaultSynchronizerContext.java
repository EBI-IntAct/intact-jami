package uk.ac.ebi.intact.jami.context;

import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.jami.model.ComplexLifecycleEvent;
import uk.ac.ebi.intact.jami.model.LifeCycleEvent;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.model.user.Preference;
import uk.ac.ebi.intact.jami.model.user.Role;
import uk.ac.ebi.intact.jami.model.user.User;
import uk.ac.ebi.intact.jami.synchronizer.*;
import uk.ac.ebi.intact.jami.synchronizer.impl.*;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.persistence.EntityManager;

/**
 * Default implementation of SynchronizerContext
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>19/02/14</pre>
 */

public class DefaultSynchronizerContext implements SynchronizerContext{
    private EntityManager entityManager;

    // cv synchronizer
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> databaseSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> qualifierSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> topicSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> aliasTypeSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> unitSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> featureTypeSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> experimentalRoleSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> biologicalRoleSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> interactionDetectionMethodSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> interactionTypeSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> participantDetectionMethodSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> experimentalPreparationSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> interactorTypeSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> rangeStatusSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> confidenceTypeSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> parameterTypeSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> cellTypeSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> tissueSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> featureDetectionMethodSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> lifecycleStatusSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> lifecycleEventSynchronizer;

    // source synchronizer
    private IntactDbSynchronizer<Source, IntactSource> sourceSynchronizer;

    // aliases synchronizers
    private AliasSynchronizer aliasSynchronizer;

    // annotation synchronizers
    private AnnotationSynchronizer annotationSynchronizer;

    // xrefs synchronizers
    private XrefSynchronizer xrefSynchronizer;

    // cooperative effect synchronizers
    private CooperativeEffectSynchronizer cooperativeEffectSynchronizer;
    private CooperativeEffectSynchronizer preAssemblySynchronizer;
    private CooperativeEffectSynchronizer allosterySynchronizer;

    // interactor synchronizers
    private InteractorSynchronizer<Interactor, IntactInteractor> interactorSynchronizer;
    private InteractorSynchronizer interactorBaseSynchronizer;
    private InteractorSynchronizer polymerSynchronizer;
    private InteractorSynchronizer<Complex, IntactComplex> complexSynchronizer;
    private InteractorSynchronizer<InteractorPool, IntactInteractorPool> interactorPoolSynchronizer;

    // causal relationship synchronizers
    private IntactDbSynchronizer<CausalRelationship, IntactCausalRelationship> causalRelationshipSynchronizer;

    // checksum synchronizers
    private uk.ac.ebi.intact.jami.synchronizer.ChecksumSynchronizer checksumSynchronizer;

    // confidence synchronizers
    private ConfidenceSynchronizer confidenceSynchronizer;

    // parameters synchronizers
    private ParameterSynchronizer parameterSynchronizer;

    // organism synchronizers
    private IntactDbSynchronizer<Organism, IntactOrganism> organismSynchronizer;

    // range synchronizers
    private IntactDbSynchronizer<Range, IntactRange> rangeSynchronizer;

    // preference synchronizers
    private IntactDbSynchronizer<Preference, Preference> preferenceSynchronizer;

    // role synchronizers
    private IntactDbSynchronizer<Role, Role> roleSynchronizer;

    // user synchronizers
    private IntactDbSynchronizer<User, User> userSynchronizer;

    // publication synchronizers
    private IntactDbSynchronizer<Publication, IntactPublication> publicationSynchronizer;

    // experiment synchronizers
    private IntactDbSynchronizer<Experiment, IntactExperiment> experimentSynchronizer;

    // interaction synchronizers
    private IntactDbSynchronizer<InteractionEvidence, IntactInteractionEvidence> interactionEvidenceSynchronizer;

    // cooperativity evidence synchronizers
    private IntactDbSynchronizer<CooperativityEvidence, IntactCooperativityEvidence> cooperativityEvidenceSynchronizer;

    // lifecycle synchronizers
    private LifecycleEventSynchronizer lifecycleSynchronizer;

    // feature synchronizers
    private IntactDbSynchronizer<FeatureEvidence, IntactFeatureEvidence> featureEvidenceSynchronizer;
    private IntactDbSynchronizer<ModelledFeature, IntactModelledFeature> modelledFeatureSynchronizer;

    // variable parameter synchronizers
    private IntactDbSynchronizer<VariableParameter, IntactVariableParameter> variableParameterSynchronizer;

    // variable parameter value synchronizers
    private IntactDbSynchronizer<VariableParameterValue, IntactVariableParameterValue> variableParameterValueSynchronizer;

    // variable parameter value set
    private IntactDbSynchronizer<VariableParameterValueSet, IntactVariableParameterValueSet> variableParameterValueSetSynchronizer;

    // participant synchronizers
    private EntitySynchronizer<AbstractIntactEntity> entitySynchronizer;
    private EntitySynchronizer entityBaseSynchronizer;
    private EntitySynchronizer entityPoolSynchronizer;

    public DefaultSynchronizerContext(EntityManager entityManager){
        if (this.entityManager == null){
            throw new IllegalArgumentException("Entity manager cannot be null in an IntAct database synchronizer context");
        }
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getDatabaseSynchronizer() {
        if (this.databaseSynchronizer == null){
            this.databaseSynchronizer = new CvTermSynchronizer(this, IntactUtils.DATABASE_OBJCLASS);
        }
        return databaseSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getQualifierSynchronizer() {
        if (this.qualifierSynchronizer == null){
            this.qualifierSynchronizer = new CvTermSynchronizer(this, IntactUtils.QUALIFIER_OBJCLASS);
        }
        return qualifierSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getTopicSynchronizer() {
        if (this.topicSynchronizer == null){
            this.topicSynchronizer = new CvTermSynchronizer(this, IntactUtils.TOPIC_OBJCLASS);
        }
        return topicSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getAliasTypeSynchronizer() {
        if (this.aliasTypeSynchronizer == null){
            this.aliasTypeSynchronizer = new CvTermSynchronizer(this, IntactUtils.ALIAS_TYPE_OBJCLASS);
        }
        return aliasTypeSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getUnitSynchronizer() {
        if (this.unitSynchronizer == null){
            this.unitSynchronizer = new CvTermSynchronizer(this, IntactUtils.UNIT_OBJCLASS);
        }
        return unitSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getFeatureTypeSynchronizer() {
        if (this.featureTypeSynchronizer == null){
            this.featureTypeSynchronizer = new CvTermSynchronizer(this, IntactUtils.FEATURE_TYPE_OBJCLASS);
        }
        return featureTypeSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getExperimentalRoleSynchronizer() {
        if (this.experimentalRoleSynchronizer == null){
            this.experimentalRoleSynchronizer = new CvTermSynchronizer(this, IntactUtils.EXPERIMENTAL_ROLE_OBJCLASS);
        }
        return experimentalRoleSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getBiologicalRoleSynchronizer() {
        if (this.biologicalRoleSynchronizer == null){
            this.biologicalRoleSynchronizer = new CvTermSynchronizer(this, IntactUtils.BIOLOGICAL_ROLE_OBJCLASS);
        }
        return biologicalRoleSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getInteractionDetectionMethodSynchronizer() {
        if (this.interactionDetectionMethodSynchronizer == null){
            this.interactionDetectionMethodSynchronizer = new CvTermSynchronizer(this, IntactUtils.INTERACTION_DETECTION_METHOD_OBJCLASS);
        }
        return interactionDetectionMethodSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getInteractionTypeSynchronizer() {
        if (this.interactionTypeSynchronizer == null){
            this.interactionTypeSynchronizer = new CvTermSynchronizer(this, IntactUtils.INTERACTION_TYPE_OBJCLASS);
        }
        return interactionTypeSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getParticipantDetectionMethodSynchronizer() {
        if (this.participantDetectionMethodSynchronizer == null){
            this.participantDetectionMethodSynchronizer = new CvTermSynchronizer(this, IntactUtils.PARTICIPANT_DETECTION_METHOD_OBJCLASS);
        }
        return participantDetectionMethodSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getExperimentalPreparationSynchronizer() {
        if (this.experimentalPreparationSynchronizer == null){
            this.experimentalPreparationSynchronizer = new CvTermSynchronizer(this, IntactUtils.PARTICIPANT_EXPERIMENTAL_PREPARATION_OBJCLASS);
        }
        return experimentalPreparationSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getInteractorTypeSynchronizer() {
        if (this.interactorTypeSynchronizer == null){
            this.interactorTypeSynchronizer = new CvTermSynchronizer(this, IntactUtils.INTERACTOR_TYPE_OBJCLASS);
        }
        return interactorTypeSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getRangeStatusSynchronizer() {
        if (this.rangeStatusSynchronizer == null){
            this.rangeStatusSynchronizer = new CvTermSynchronizer(this, IntactUtils.RANGE_STATUS_OBJCLASS);
        }
        return rangeStatusSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getConfidenceTypeSynchronizer() {
        if (this.confidenceTypeSynchronizer == null){
            this.confidenceTypeSynchronizer = new CvTermSynchronizer(this, IntactUtils.CONFIDENCE_TYPE_OBJCLASS);
        }
        return confidenceTypeSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getParameterTypeSynchronizer() {
        if (this.parameterTypeSynchronizer == null){
            this.parameterTypeSynchronizer = new CvTermSynchronizer(this, IntactUtils.PARAMETER_TYPE_OBJCLASS);
        }
        return parameterTypeSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getCellTypeSynchronizer() {
        if (this.cellTypeSynchronizer == null){
            this.cellTypeSynchronizer = new CvTermSynchronizer(this, IntactUtils.CELL_TYPE_OBJCLASS);
        }
        return cellTypeSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getTissueSynchronizer() {
        if (this.tissueSynchronizer == null){
            this.tissueSynchronizer = new CvTermSynchronizer(this, IntactUtils.TISSUE_OBJCLASS);
        }
        return tissueSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getFeatureDetectionMethodSynchronizer() {
        if (this.featureDetectionMethodSynchronizer == null){
            this.featureDetectionMethodSynchronizer = new CvTermSynchronizer(this, IntactUtils.FEATURE_METHOD_OBJCLASS);
        }
        return featureDetectionMethodSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getLifecycleStatusSynchronizer() {
        if (this.lifecycleStatusSynchronizer == null){
            this.lifecycleStatusSynchronizer = new CvTermSynchronizer(this, IntactUtils.PUBLICATION_STATUS_OBJCLASS);
        }
        return lifecycleStatusSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getLifecycleEventSynchronizer() {
        if (this.lifecycleEventSynchronizer == null){
            this.lifecycleEventSynchronizer = new CvTermSynchronizer(this, IntactUtils.LIFECYCLE_EVENT_OBJCLASS);
        }
        return lifecycleEventSynchronizer;
    }

    public AliasSynchronizer<CvTermAlias> getCvAliasSynchronizer() {
        initialiseAliasTemplateIfNotDone();
        this.aliasSynchronizer.setIntactClass(CvTermAlias.class);
        return this.aliasSynchronizer;
    }

    public XrefSynchronizer<CvTermXref> getCvXrefSynchronizer() {
        initialiseXrefTemplateIfNotDone();
        this.xrefSynchronizer.setIntactClass(CvTermXref.class);
        return this.xrefSynchronizer;
    }

    public AnnotationSynchronizer<CvTermAnnotation> getCvAnnotationSynchronizer() {
        initialiseAnnotationTemplateIfNodDone();
        this.annotationSynchronizer.setIntactClass(CvTermAnnotation.class);
        return this.annotationSynchronizer;
    }

    public IntactDbSynchronizer<Source, IntactSource> getSourceSynchronizer() {
        if (this.sourceSynchronizer == null){
            this.sourceSynchronizer = new SourceSynchronizer(this);
        }
        return sourceSynchronizer;
    }

    public AliasSynchronizer<SourceAlias> getSourceAliasSynchronizer() {
        initialiseAliasTemplateIfNotDone();
        this.aliasSynchronizer.setIntactClass(SourceAlias.class);
        return this.aliasSynchronizer;
    }

    public XrefSynchronizer<SourceXref> getSourceXrefSynchronizer() {
        initialiseXrefTemplateIfNotDone();
        this.xrefSynchronizer.setIntactClass(SourceXref.class);
        return this.xrefSynchronizer;
    }

    public AnnotationSynchronizer<SourceAnnotation> getSourceAnnotationSynchronizer() {
        initialiseAnnotationTemplateIfNodDone();
        this.annotationSynchronizer.setIntactClass(SourceAnnotation.class);
        return this.annotationSynchronizer;
    }

    public AliasSynchronizer<OrganismAlias> getOrganismAliasSynchronizer() {
        initialiseAliasTemplateIfNotDone();
        this.aliasSynchronizer.setIntactClass(OrganismAlias.class);
        return this.aliasSynchronizer;
    }

    public AliasSynchronizer<FeatureAlias> getFeatureAliasSynchronizer() {
        initialiseAliasTemplateIfNotDone();
        this.aliasSynchronizer.setIntactClass(FeatureAlias.class);
        return this.aliasSynchronizer;
    }

    public AliasSynchronizer<EntityAlias> getEntityAliasSynchronizer() {
        initialiseAliasTemplateIfNotDone();
        this.aliasSynchronizer.setIntactClass(EntityAlias.class);
        return this.aliasSynchronizer;
    }

    public AliasSynchronizer<InteractorAlias> getInteractorAliasSynchronizer() {
        initialiseAliasTemplateIfNotDone();
        this.aliasSynchronizer.setIntactClass(InteractorAlias.class);
        return this.aliasSynchronizer;
    }

    public XrefSynchronizer<PublicationXref> getPublicationXrefSynchronizer() {
        initialiseXrefTemplateIfNotDone();
        this.xrefSynchronizer.setIntactClass(PublicationXref.class);
        return this.xrefSynchronizer;
    }

    public XrefSynchronizer<ExperimentXref> getExperimentXrefSynchronizer() {
        initialiseXrefTemplateIfNotDone();
        this.xrefSynchronizer.setIntactClass(ExperimentXref.class);
        return this.xrefSynchronizer;
    }

    public XrefSynchronizer<InteractionXref> getInteractionXrefSynchronizer() {
        initialiseXrefTemplateIfNotDone();
        this.xrefSynchronizer.setIntactClass(InteractionXref.class);
        return this.xrefSynchronizer;
    }

    public XrefSynchronizer<InteractorXref> getInteractorXrefSynchronizer() {
        initialiseXrefTemplateIfNotDone();
        this.xrefSynchronizer.setIntactClass(InteractorXref.class);
        return this.xrefSynchronizer;
    }

    public XrefSynchronizer<FeatureXref> getFeatureXrefSynchronizer() {
        initialiseXrefTemplateIfNotDone();
        this.xrefSynchronizer.setIntactClass(FeatureXref.class);
        return this.xrefSynchronizer;
    }

    public XrefSynchronizer<EntityXref> getEntityXrefSynchronizer() {
        initialiseXrefTemplateIfNotDone();
        this.xrefSynchronizer.setIntactClass(EntityXref.class);
        return this.xrefSynchronizer;
    }

    public XrefSynchronizer<ResultingSequenceXref> getResultingSequenceXrefSynchronizer() {
        initialiseXrefTemplateIfNotDone();
        this.xrefSynchronizer.setIntactClass(ResultingSequenceXref.class);
        return this.xrefSynchronizer;
    }

    public AnnotationSynchronizer<PublicationAnnotation> getPublicationAnnotationSynchronizer() {
        initialiseAnnotationTemplateIfNodDone();
        this.annotationSynchronizer.setIntactClass(PublicationAnnotation.class);
        return this.annotationSynchronizer;
    }

    public AnnotationSynchronizer<ExperimentAnnotation> getExperimentAnnotationSynchronizer() {
        initialiseAnnotationTemplateIfNodDone();
        this.annotationSynchronizer.setIntactClass(ExperimentAnnotation.class);
        return this.annotationSynchronizer;
    }

    public AnnotationSynchronizer<InteractionAnnotation> getInteractionAnnotationSynchronizer() {
        initialiseAnnotationTemplateIfNodDone();
        this.annotationSynchronizer.setIntactClass(InteractionAnnotation.class);
        return this.annotationSynchronizer;
    }

    public AnnotationSynchronizer<InteractorAnnotation> getInteractorAnnotationSynchronizer() {
        initialiseAnnotationTemplateIfNodDone();
        this.annotationSynchronizer.setIntactClass(InteractorAnnotation.class);
        return this.annotationSynchronizer;
    }

    public AnnotationSynchronizer<FeatureAnnotation> getFeatureAnnotationSynchronizer() {
        initialiseAnnotationTemplateIfNodDone();
        this.annotationSynchronizer.setIntactClass(FeatureAnnotation.class);
        return this.annotationSynchronizer;
    }

    public AnnotationSynchronizer<EntityAnnotation> getEntityAnnotationSynchronizer() {
        initialiseAnnotationTemplateIfNodDone();
        this.annotationSynchronizer.setIntactClass(EntityAnnotation.class);
        return this.annotationSynchronizer;
    }

    public AnnotationSynchronizer<CooperativeEffectAnnotation> getCooperativeEffectAnnotationSynchronizer() {
        initialiseAnnotationTemplateIfNodDone();
        this.annotationSynchronizer.setIntactClass(CooperativeEffectAnnotation.class);
        return this.annotationSynchronizer;
    }

    public CooperativeEffectSynchronizer<CooperativeEffect, AbstractIntactCooperativeEffect> getCooperativeEffectSynchronizer() {
        if (this.cooperativeEffectSynchronizer == null){
           this.cooperativeEffectSynchronizer = new CompositeCooperativeEffectSynchronizer(this);
        }
        return cooperativeEffectSynchronizer;
    }

    public CooperativeEffectSynchronizer<Preassembly, IntactPreassembly> getPreAssemblySynchronizer() {
        if (this.preAssemblySynchronizer == null){
            this.preAssemblySynchronizer = new CooperativeEffectSynchronizerTemplate(this, IntactPreassembly.class);
        }
        return this.preAssemblySynchronizer;
    }

    public CooperativeEffectSynchronizer<Allostery, IntactAllostery> getAllosterySynchronizer() {
        if (this.allosterySynchronizer == null){
            this.allosterySynchronizer = new AllosterySynchronizer(this);
        }
        return this.allosterySynchronizer;
    }

    public InteractorSynchronizer<Complex, IntactComplex> getComplexSynchronizer() {
        if (this.complexSynchronizer == null){
           this.complexSynchronizer = new ComplexSynchronizer(this);
        }
        return this.complexSynchronizer;
    }

    public InteractorSynchronizer<Interactor, IntactInteractor> getInteractorSynchronizer() {
        if (this.interactorSynchronizer == null){
            this.interactorSynchronizer = new CompositeInteractorSynchronizer(this);
        }
        return this.interactorSynchronizer;
    }

    public InteractorSynchronizer<Interactor, IntactInteractor> getInteractorBaseSynchronizer() {
        initialiseInteractorTemplateIfNotDone();
        this.interactorBaseSynchronizer.setIntactClass(IntactInteractor.class);
        return this.interactorBaseSynchronizer;
    }

    public InteractorSynchronizer<Polymer, IntactPolymer> getPolymerSynchronizer() {
        initialisePolymerTemplateIfNotDone();
        this.polymerSynchronizer.setIntactClass(IntactPolymer.class);
        return this.polymerSynchronizer;
    }

    public InteractorSynchronizer<Protein, IntactProtein> getProteinSynchronizer() {
        initialisePolymerTemplateIfNotDone();
        this.polymerSynchronizer.setIntactClass(IntactProtein.class);
        return this.polymerSynchronizer;
    }

    public InteractorSynchronizer<NucleicAcid, IntactNucleicAcid> getNucleicAcidSynchronizer() {
        initialisePolymerTemplateIfNotDone();
        this.polymerSynchronizer.setIntactClass(IntactNucleicAcid.class);
        return this.polymerSynchronizer;
    }

    public InteractorSynchronizer<Molecule, IntactMolecule> getMoleculeSynchronizer() {
        initialiseInteractorTemplateIfNotDone();
        this.interactorBaseSynchronizer.setIntactClass(IntactMolecule.class);
        return this.interactorBaseSynchronizer;
    }

    public InteractorSynchronizer<Gene, IntactGene> getGeneSynchronizer() {
        initialiseInteractorTemplateIfNotDone();
        this.interactorBaseSynchronizer.setIntactClass(IntactGene.class);
        return this.interactorBaseSynchronizer;
    }

    public InteractorSynchronizer<InteractorPool, IntactInteractorPool> getInteractorPoolSynchronizer() {
        if (this.interactorPoolSynchronizer == null){
            this.interactorPoolSynchronizer = new InteractorPoolSynchronizer(this);
        }
        return this.interactorPoolSynchronizer;
    }

    public InteractorSynchronizer<BioactiveEntity, IntactBioactiveEntity> getBioactiveEntitySynchronizer() {
        initialiseInteractorTemplateIfNotDone();
        this.interactorBaseSynchronizer.setIntactClass(IntactBioactiveEntity.class);
        return this.interactorBaseSynchronizer;
    }

    public IntactDbSynchronizer<CausalRelationship, IntactCausalRelationship> getCausalRelationshipSynchronizer() {
        if (this.causalRelationshipSynchronizer == null){
            this.causalRelationshipSynchronizer = new CausalRelationchipSynchronizer(this);
        }
        return causalRelationshipSynchronizer;
    }

    public uk.ac.ebi.intact.jami.synchronizer.ChecksumSynchronizer getInteractionChecksumSynchronizer() {
        initialiseChecksumTemplateIfNodDone();
        this.checksumSynchronizer.setIntactClass(InteractionChecksum.class);
        return this.checksumSynchronizer;
    }


    public uk.ac.ebi.intact.jami.synchronizer.ChecksumSynchronizer getInteractorChecksumSynchronizer() {
        initialiseChecksumTemplateIfNodDone();
        this.checksumSynchronizer.setIntactClass(InteractorChecksum.class);
        return this.checksumSynchronizer;
    }

    public ConfidenceSynchronizer<Confidence, InteractionEvidenceConfidence> getInteractionConfidenceSynchronizer() {
        initialiseConfidenceTemplateIfNodDone();
        this.confidenceSynchronizer.setIntactClass(InteractionEvidenceConfidence.class);
        return this.confidenceSynchronizer;
    }

    public ConfidenceSynchronizer<ModelledConfidence, ComplexConfidence> getComplexConfidenceSynchronizer() {
        initialiseConfidenceTemplateIfNodDone();
        this.confidenceSynchronizer.setIntactClass(ComplexConfidence.class);
        return this.confidenceSynchronizer;
    }

    public ConfidenceSynchronizer<Confidence, ExperimentalEntityConfidence> getEntityConfidenceSynchronizer() {
        initialiseConfidenceTemplateIfNodDone();
        this.confidenceSynchronizer.setIntactClass(ExperimentalEntityConfidence.class);
        return this.confidenceSynchronizer;
    }

    public ParameterSynchronizer<Parameter, InteractionEvidenceParameter> getInteractionParameterSynchronizer(){
        initialiseParameterTemplateIfNodDone();
        this.parameterSynchronizer.setIntactClass(InteractionEvidenceParameter.class);
        return this.parameterSynchronizer;
    }

    public ParameterSynchronizer<Parameter, ExperimentalEntityParameter> getEntityParameterSynchronizer(){
        initialiseParameterTemplateIfNodDone();
        this.parameterSynchronizer.setIntactClass(ExperimentalEntityParameter.class);
        return this.parameterSynchronizer;
    }

    public ParameterSynchronizer<ModelledParameter, ComplexParameter> getComplexParameterSynchronizer() {
        initialiseParameterTemplateIfNodDone();
        this.parameterSynchronizer.setIntactClass(ComplexParameter.class);
        return this.parameterSynchronizer;
    }

    public IntactDbSynchronizer<Organism, IntactOrganism> getOrganismSynchronizer() {
        if (this.organismSynchronizer == null){
            this.organismSynchronizer = new OrganismSynchronizer(this);
        }
        return organismSynchronizer;
    }

    public IntactDbSynchronizer<Range, IntactRange> getRangeSynchronizer() {
        if (this.rangeSynchronizer == null){
            this.rangeSynchronizer = new RangeSynchronizer(this);
        }
        return rangeSynchronizer;
    }

    public IntactDbSynchronizer<Preference, Preference> getPreferenceSynchronizer() {
        if (this.preferenceSynchronizer == null){
            this.preferenceSynchronizer = new PreferenceSynchronizer(this);
        }
        return preferenceSynchronizer;
    }

    public IntactDbSynchronizer<Role, Role> getRoleSynchronizer() {
        if (this.roleSynchronizer == null){
            this.roleSynchronizer = new RoleSynchronizer(this);
        }
        return roleSynchronizer;
    }

    public IntactDbSynchronizer<User, User> getUserSynchronizer() {
        if (this.userSynchronizer == null){
            this.userSynchronizer = new UserSynchronizer(this);
        }
        return userSynchronizer;
    }

    public IntactDbSynchronizer<LifeCycleEvent, ComplexLifecycleEvent> getComplexLifecycleSynchronizer() {
        return null;
    }

    public IntactDbSynchronizer<Publication, IntactPublication> getPublicationSynchronizer() {
        return publicationSynchronizer;
    }

    public IntactDbSynchronizer<Experiment, IntactExperiment> getExperimentSynchronizer() {
        return experimentSynchronizer;
    }

    public IntactDbSynchronizer<InteractionEvidence, IntactInteractionEvidence> getInteractionEvidenceSynchronizer() {
        return interactionEvidenceSynchronizer;
    }

    public IntactDbSynchronizer<CooperativityEvidence, IntactCooperativityEvidence> getCooperativityEvidenceSynchronizer() {
        return cooperativityEvidenceSynchronizer;
    }

    public LifecycleEventSynchronizer getLifecycleSynchronizer() {
        return lifecycleSynchronizer;
    }

    public IntactDbSynchronizer<FeatureEvidence, IntactFeatureEvidence> getFeatureEvidenceSynchronizer() {
        return featureEvidenceSynchronizer;
    }

    public IntactDbSynchronizer<ModelledFeature, IntactModelledFeature> getModelledFeatureSynchronizer() {
        return modelledFeatureSynchronizer;
    }

    public IntactDbSynchronizer<VariableParameter, IntactVariableParameter> getVariableParameterSynchronizer() {
        return variableParameterSynchronizer;
    }

    public IntactDbSynchronizer<VariableParameterValue, IntactVariableParameterValue> getVariableParameterValueSynchronizer() {
        return variableParameterValueSynchronizer;
    }

    public IntactDbSynchronizer<VariableParameterValueSet, IntactVariableParameterValueSet> getVariableParameterValueSetSynchronizer() {
        return variableParameterValueSetSynchronizer;
    }

    public EntitySynchronizer<AbstractIntactEntity> getEntitySynchronizer() {
        return entitySynchronizer;
    }

    public EntitySynchronizer getEntityBaseSynchronizer() {
        return entityBaseSynchronizer;
    }

    public void clearCache() {

    }

    private void initialiseAliasTemplateIfNotDone() {
        if (this.aliasSynchronizer == null){
            this.aliasSynchronizer = new AliasSynchronizerTemplate(this, AbstractIntactAlias.class);
        }
    }

    private void initialiseInteractorTemplateIfNotDone() {
        if (this.interactorBaseSynchronizer == null){
            this.interactorBaseSynchronizer = new InteractorSynchronizerTemplate(this, IntactInteractor.class);
        }
    }

    private void initialisePolymerTemplateIfNotDone() {
        if (this.polymerSynchronizer == null){
            this.polymerSynchronizer = new PolymerSynchronizerTemplate(this, IntactPolymer.class);
        }
    }

    private void initialiseAnnotationTemplateIfNodDone() {
        if (this.annotationSynchronizer == null){
            this.annotationSynchronizer = new AnnotationSynchronizerTemplate(this, AbstractIntactAnnotation.class);
        }
    }

    private void initialiseXrefTemplateIfNotDone() {
        if (this.xrefSynchronizer == null){
            this.xrefSynchronizer = new XrefSynchronizerTemplate(this, AbstractIntactXref.class);
        }
    }

    private void initialiseChecksumTemplateIfNodDone() {
        if (this.checksumSynchronizer == null){
            this.checksumSynchronizer = new ChecksumSynchronizerTemplate(this, AbstractIntactChecksum.class);
        }
    }

    private void initialiseConfidenceTemplateIfNodDone() {
        if (this.confidenceSynchronizer == null){
            this.confidenceSynchronizer = new ConfidenceSynchronizerTemplate(this, AbstractIntactConfidence.class);
        }
    }

    private void initialiseParameterTemplateIfNodDone() {
        if (this.parameterSynchronizer == null){
            this.parameterSynchronizer = new ParameterSynchronizerTemplate(this, AbstractIntactParameter.class);
        }
    }
}
