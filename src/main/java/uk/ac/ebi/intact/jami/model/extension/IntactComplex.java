package uk.ac.ebi.intact.jami.model.extension;

import org.hibernate.Hibernate;
import org.hibernate.annotations.*;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.model.Source;
import psidev.psi.mi.jami.model.impl.DefaultChecksum;
import psidev.psi.mi.jami.utils.AliasUtils;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.utils.ChecksumUtils;
import psidev.psi.mi.jami.utils.XrefUtils;
import psidev.psi.mi.jami.utils.collection.AbstractListHavingProperties;
import uk.ac.ebi.intact.jami.model.lifecycle.ComplexLifeCycleEvent;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleEvent;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleStatus;
import uk.ac.ebi.intact.jami.model.lifecycle.Releasable;
import uk.ac.ebi.intact.jami.model.listener.ComplexParameterListener;
import uk.ac.ebi.intact.jami.model.user.User;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Intact implementation of complex
 *
 * NOTE: The participants have the ownership of the relation between complex and participants. It means that to persist the relationship between interaction and participants,
 * the property getInteraction in the participant must be pointing to the right complex. It is then recommended to use the provided addParticipant and removeParticipant methods to add/remove participants
 * from the complex
 * NOTE: the method getExperiments has been kept only for backward compatibility with intact-core. As soon as intact-core is removed, the getExperiments can be removed.
 * This method should never been used in any applications
 * NOTE: getCreatedDate and getUpdatedDate are transient methods as the AbstractIntactAudit parent class contains the relevant persistent audit methods.
 * NOTE: checksums are not persistent for complexes
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/01/14</pre>
 */
@Entity
@DiscriminatorValue( "complex" )
@EntityListeners(value = {ComplexParameterListener.class})
@Where(clause = "category = 'complex'")
@Cacheable
public class IntactComplex extends IntactInteractor implements Complex,Releasable{

    private Collection<InteractionEvidence> interactionEvidences;
    private Collection<ModelledParticipant> components;
    private transient Annotation physicalProperties;
    private Collection<ModelledConfidence> confidences;
    private Collection<ModelledParameter> parameters;

    private Source source;
    private Collection<CooperativeEffect> cooperativeEffects;
    private transient Checksum rigid;
    private CvTerm interactionType;

    private transient Alias recommendedName;
    private transient Alias systematicName;
    private Collection<Experiment> experiments;
    private List<LifeCycleEvent> lifecycleEvents;
    private transient LifeCycleStatus status;
    private CvTerm evidenceType;
    private User currentOwner;
    private User currentReviewer;

    private transient CvTerm cvStatus;

    private transient Annotation toBeReviewed;
    private transient Annotation onHold;
    private transient Annotation accepted;
    private transient Annotation correctionComment;

    private transient Xref complexAcXref;

    protected IntactComplex(){
        super();
    }

    public IntactComplex(String name, CvTerm interactorType) {
        super(name, interactorType);
    }

    public IntactComplex(String name, String fullName, CvTerm interactorType) {
        super(name, fullName, interactorType);
    }

    public IntactComplex(String name, CvTerm interactorType, Organism organism) {
        super(name, interactorType, organism);
    }

    public IntactComplex(String name, String fullName, CvTerm interactorType, Organism organism) {
        super(name, fullName, interactorType, organism);
    }

    public IntactComplex(String name, CvTerm interactorType, Xref uniqueId) {
        super(name, interactorType, uniqueId);
    }

    public IntactComplex(String name, String fullName, CvTerm interactorType, Xref uniqueId) {
        super(name, fullName, interactorType, uniqueId);
    }

    public IntactComplex(String name, CvTerm interactorType, Organism organism, Xref uniqueId) {
        super(name, interactorType, organism, uniqueId);
    }

    public IntactComplex(String name, String fullName, CvTerm interactorType, Organism organism, Xref uniqueId) {
        super(name, fullName, interactorType, organism, uniqueId);
    }

    public IntactComplex(String name) {
        super(name);
    }

    public IntactComplex(String name, String fullName) {
        super(name, fullName);
    }

    public IntactComplex(String name, Organism organism) {
        super(name, organism);
    }

    public IntactComplex(String name, String fullName, Organism organism) {
        super(name, fullName, organism);
    }

    public IntactComplex(String name, Xref uniqueId) {
        super(name, uniqueId);
    }

    public IntactComplex(String name, String fullName, Xref uniqueId) {
        super(name, fullName, uniqueId);
    }

    public IntactComplex(String name, Organism organism, Xref uniqueId) {
        super(name, organism, uniqueId);
    }

    public IntactComplex(String name, String fullName, Organism organism, Xref uniqueId) {
        super(name, fullName, organism, uniqueId);
    }

    @Override
    @Transient
    public Collection<Annotation> getAnnotations() {
        return super.getAnnotations();
    }

    @Override
    @Transient
    public Collection<Checksum> getChecksums() {
        return super.getChecksums();
    }

    @Override
    @Transient
    public Collection<Xref> getXrefs() {
        return super.getXrefs();
    }

    @Override
    @Transient
    public Collection<Xref> getIdentifiers() {
        return super.getIdentifiers();
    }

    @Override
    @Transient
    public Collection<Alias> getAliases() {
        return super.getAliases();
    }

    @Transient
    /**
     * NOTE: in the future, should be persisted and cvStatus should be removed
     */
    public LifeCycleStatus getStatus() {
        if (this.status == null){
            initialiseStatus();
        }
        return status;
    }

    public void setStatus(LifeCycleStatus status) {
        this.status = status != null ? status : LifeCycleStatus.NEW;
        this.cvStatus = this.status.toCvTerm();
    }

    private void initialiseStatus() {
        if (this.cvStatus == null){
            this.status = LifeCycleStatus.NEW;
        }
        else{
            this.status = LifeCycleStatus.toLifeCycleStatus(this.cvStatus);
        }
    }

    @ManyToOne(targetEntity = IntactCvTerm.class)
    @JoinColumn( name = "status_ac", referencedColumnName = "ac" )
    @ForeignKey(name="FK_COMPLEX_STATUS")
    @Target(IntactCvTerm.class)
    /**
     * NOTE: in the future, should be persisted and cvStatus should be removed
     * @deprecated use getStatus instead
     */
    @Deprecated
    public CvTerm getCvStatus() {
        if (this.cvStatus == null){
            this.cvStatus = getStatus().toCvTerm();
        }
        return this.cvStatus;
    }

    /**
     *
     * @param status
     * @deprecated use setStatus instead
     */
    @Deprecated
    public void setCvStatus( CvTerm status ) {
        this.cvStatus = status;
        this.status = null;
    }

    @ManyToOne( targetEntity = User.class )
    @JoinColumn( name = "owner_pk", referencedColumnName = "ac" )
    @ForeignKey(name="FK_COMPLEX_OWNER")
    @Target(User.class)
    public User getCurrentOwner() {
        return currentOwner;
    }

    public void setCurrentOwner( User currentOwner ) {
        this.currentOwner = currentOwner;
    }

    @ManyToOne( targetEntity = User.class )
    @JoinColumn( name = "reviewer_pk", referencedColumnName = "ac" )
    @ForeignKey(name="FK_COMPLEX_REVIEWER")
    @Target(User.class)
    public User getCurrentReviewer() {
        return currentReviewer;
    }

    public void setCurrentReviewer( User currentReviewer ) {
        this.currentReviewer = currentReviewer;
    }

    @Override
    public boolean areLifeCycleEventsInitialized() {
        return Hibernate.isInitialized(getLifecycleEvents());
    }

    @OneToMany( orphanRemoval = true, cascade = CascadeType.ALL, targetEntity = ComplexLifeCycleEvent.class)
    @JoinColumn(name="complex_ac", referencedColumnName="ac")
    @ForeignKey(name="FK_LIFECYCLE_EVENT_COMPLEX")
    @Cascade( value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE} )
    @OrderBy("when, created")
    @Target(ComplexLifeCycleEvent.class)
    public List<LifeCycleEvent> getLifecycleEvents() {
        if (this.lifecycleEvents == null){
            this.lifecycleEvents = new ArrayList<LifeCycleEvent>();
        }
        return lifecycleEvents;
    }

    private void setLifecycleEvents(List<LifeCycleEvent> lifecycleEvents) {
        this.lifecycleEvents = lifecycleEvents;
    }

    @Override
    @Transient
    public String getOnHoldComment() {
        // initialise annotations if necessary
        getAnnotations();
        return onHold != null ? onHold.getValue() : null;
    }

    @Override
    @Transient
    public String getToBeReviewedComment() {
        // initialise annotations if necessary
        getAnnotations();
        return toBeReviewed != null ? toBeReviewed.getValue() : null;
    }

    @Override
    @Transient
    public String getAcceptedComment() {
        // initialise annotations if necessary
        getAnnotations();
        return accepted != null ? accepted.getValue() : null;
    }

    @Override
    public void onToBeReviewed(String message) {
        Collection<Annotation> complexAnnotationList = getAnnotations();

        if (toBeReviewed != null){
            this.toBeReviewed.setValue(message);
        }
        else  {
            CvTerm toBeReviewedTopic = IntactUtils.createMITopic(Releasable.TO_BE_REVIEWED, null);
            this.toBeReviewed = new InteractorAnnotation(toBeReviewedTopic, message);
            complexAnnotationList.add(this.toBeReviewed);
        }
    }

    @Override
    public void onAccepted(String message) {
        Collection<Annotation> complexAnnotationList = getAnnotations();

        if (accepted != null){
            this.accepted.setValue(message);
        }
        else  {
            CvTerm acceptedTopic = IntactUtils.createMITopic(Releasable.ACCEPTED, null);
            this.accepted = new InteractorAnnotation(acceptedTopic, message);
            complexAnnotationList.add(this.accepted);
        }
    }

    @Override
    public void removeCorrectionComment() {
        AnnotationUtils.removeAllAnnotationsWithTopic(getAnnotations(), null, Releasable.CORRECTION_COMMENT);
    }

    @Override
    @Transient
    public String getCorrectionComment() {
        return this.correctionComment != null ? this.correctionComment.getValue() : null;
    }

    @Override
    public void onCorrectionComment(String message) {
        Collection<Annotation> complexAnnotationList = getAnnotations();

        if (correctionComment != null){
            this.correctionComment.setValue(message);
        }
        else  {
            CvTerm onHoldTopic = IntactUtils.createMITopic(Releasable.CORRECTION_COMMENT, null);
            this.correctionComment = new InteractorAnnotation(onHoldTopic, message);
            complexAnnotationList.add(this.correctionComment);
        }
    }

    @Override
    @Transient
    public boolean hasCorrectionComment() {
        return this.correctionComment != null;
    }

    @Override
    @Transient
    public boolean isAccepted() {
        // initialise annotations if necessary
        getAnnotations();
        return accepted != null;
    }

    @Override
    public void removeAccepted() {
        AnnotationUtils.removeAllAnnotationsWithTopic(getAnnotations(), null, Releasable.ACCEPTED);
    }

    @Override
    @Transient
    public boolean isToBeReviewed() {
        // initialise annotations if necessary
        getAnnotations();
        return toBeReviewed != null;
    }

    @Override
    public void removeToBeReviewed() {
        AnnotationUtils.removeAllAnnotationsWithTopic(getAnnotations(), null, Releasable.TO_BE_REVIEWED);
    }

    @Override
    public void onHold(String message) {
        Collection<Annotation> complexAnnotationList = getAnnotations();

        if (onHold != null){
            this.onHold.setValue(message);
        }
        else  {
            CvTerm onHoldTopic = IntactUtils.createMITopic(Releasable.ON_HOLD, null);
            this.onHold = new InteractorAnnotation(onHoldTopic, message);
            complexAnnotationList.add(this.onHold);
        }
    }

    @Override
    @Transient
    public boolean isOnHold() {
        // initialise annotations if necessary
        getAnnotations();
        return onHold != null;
    }

    @Override
    public void removeOnHold() {
        AnnotationUtils.removeAllAnnotationsWithTopic(getAnnotations(), null, Releasable.ON_HOLD);
    }

    @ManyToOne(targetEntity = IntactSource.class)
    @JoinColumn( name = "owner_ac", referencedColumnName = "ac")
    @Target(IntactSource.class)
    @NotNull
    public Source getSource() {
        return this.source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    @ManyToOne(targetEntity = IntactCvTerm.class)
    @JoinColumn( name = "evidence_type_ac", referencedColumnName = "ac")
    @Target(IntactCvTerm.class)
    @NotNull
    public CvTerm getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(CvTerm evidenceType) {
        this.evidenceType = evidenceType;
    }

    @OneToMany( mappedBy = "dbParentInteraction", orphanRemoval = true,
            cascade = {CascadeType.ALL}, targetEntity = IntactModelledParticipant.class)
    @Cascade( value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE} )
    @Target(IntactModelledParticipant.class)
    /**
    * NOTE: The participants have the ownership of the relation between complex and participants. It means that to persist the relationship between interaction and participants,
    * the property getInteraction in the participant must be pointing to the right complex. It is then recommended to use the provided addParticipant and removeParticipant methods to add/remove participants
    * from the complex
    **/
    public Collection<ModelledParticipant> getParticipants() {
        if (components == null){
            initialiseComponents();
        }
        return this.components;
    }

    private void setParticipants(Collection<ModelledParticipant> components) {
        this.components = components;
    }

    public boolean addParticipant(ModelledParticipant part) {
        if (part == null){
            return false;
        }
        if (components == null){
            initialiseComponents();
        }
        part.setInteraction(this);
        return components.add(part);
    }

    public boolean removeParticipant(ModelledParticipant part) {
        if (part == null){
            return false;
        }
        if (components == null){
            initialiseComponents();
        }
        part.setInteraction(null);
        return components.remove(part);
    }

    public boolean addAllParticipants(Collection<? extends ModelledParticipant> participants) {
        if (participants == null){
            return false;
        }

        boolean added = false;
        for (ModelledParticipant p : participants){
            if (addParticipant(p)){
                added = true;
            }
        }
        return added;
    }

    public boolean removeAllParticipants(Collection<? extends ModelledParticipant> participants) {
        if (participants == null){
            return false;
        }

        boolean removed = false;
        for (ModelledParticipant p : participants){
            if (removeParticipant(p)){
                removed = true;
            }
        }
        return removed;
    }

    @OneToMany( orphanRemoval = true,
            cascade = {CascadeType.ALL}, targetEntity = ComplexConfidence.class)
    @JoinColumn(name="interaction_ac", referencedColumnName="ac")
    @Cascade( value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE} )
    @Target(ComplexConfidence.class)
    public Collection<ModelledConfidence> getModelledConfidences() {
        if (confidences == null){
            initialiseConfidences();
        }
        return this.confidences;
    }

    private void setModelledConfidences(Collection<ModelledConfidence> confidences) {
        this.confidences = confidences;
    }

    @OneToMany( orphanRemoval = true,
            cascade = {CascadeType.ALL}, targetEntity = ComplexParameter.class)
    @JoinColumn(name="interaction_ac", referencedColumnName="ac")
    @Cascade( value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE} )
    @Target(ComplexParameter.class)
    public Collection<ModelledParameter> getModelledParameters() {
        if (parameters == null){
            initialiseParameters();
        }
        return this.parameters;
    }

    private void setModelledParameters(Collection<ModelledParameter> parameters) {
        this.parameters = parameters;
    }

    /*@OneToMany( orphanRemoval = true,
            cascade = {CascadeType.ALL}, targetEntity = AbstractIntactCooperativeEffect.class)
    @JoinColumn(name="complex_ac", referencedColumnName="ac")
    @Cascade( value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE} )
    @Target(AbstractIntactCooperativeEffect.class)*/
    @Transient
    /**
     * For the time being, cooperative effects are not persisted in IntAct. We may want to do so in the future using the commented hibernate mapping
     */
    public Collection<CooperativeEffect> getCooperativeEffects() {
        if (cooperativeEffects == null){
            initialiseCooperativeEffects();
        }
        return this.cooperativeEffects;
    }

    private void setCooperativeEffects(Collection<CooperativeEffect> cooperativeEffects) {
        this.cooperativeEffects = cooperativeEffects;
    }

    @Transient
    public String getComplexAc() {
        // initialise xrefs if not done yet
        getXrefs();
        return this.complexAcXref != null ? this.complexAcXref.getId() : null;
    }

    @Transient
    public String getComplexVersion() {
        // initialise xrefs if not done yet
        getXrefs();
        return this.complexAcXref != null ? this.complexAcXref.getVersion() : null;
    }

    @Transient
    public Xref getComplexAcXref() {
        // initialise xrefs if not done yet
        getXrefs();
        return this.complexAcXref;
    }

    @Override
    public void assignComplexAc(String accession, String version) {
        // add new complex ac if not null
        if (accession != null) {
            InteractorXrefList interactionXrefs = (InteractorXrefList) getXrefs();

            CvTerm complexPortalDatabase = IntactUtils.createMIDatabase(Xref.COMPLEX_PORTAL, Xref.COMPLEX_PORTAL_MI);
            CvTerm complexPortalPrimaryQualifier = IntactUtils.createMIQualifier(Xref.COMPLEX_PRIMARY, Xref.COMPLEX_PRIMARY_MI);
            // first remove old ac if not null
            if (this.complexAcXref != null) {
                if (!accession.equals(complexAcXref.getId())) {
                    // first remove old complexAcXref and creates the new one;
                    interactionXrefs.remove(this.complexAcXref);
                    this.complexAcXref = new InteractorXref(complexPortalDatabase, accession, version, complexPortalPrimaryQualifier);
                    interactionXrefs.add(this.complexAcXref);
                }
            } else {
                this.complexAcXref = new InteractorXref(complexPortalDatabase, accession, version, complexPortalPrimaryQualifier);
                interactionXrefs.add(this.complexAcXref);
            }
        } else {
            throw new IllegalArgumentException("The complex ac has to be non null.");
        }
    }

    @Override
    public void assignComplexAc(String accession) {
        // add new complex ac if not null
        if (accession != null) {
            String id;
            String version;

            //It checks if the accession is valid and split the version if it is provided
            String[] splittedComplexAc = accession.split("\\.");
            if (splittedComplexAc.length == 1) {
                id = splittedComplexAc[0];
                version = "1";
            } else if (splittedComplexAc.length == 2) {
                {
                    id = splittedComplexAc[0];
                    version = splittedComplexAc[1];
                }
            } else {
                throw new IllegalArgumentException("The complex ac has a non valid format (e.g. CPX-12345.1)");
            }
            assignComplexAc(id, version);

        } else {
            throw new IllegalArgumentException("The complex ac has to be non null.");
        }
    }

    @Transient
    public String getPhysicalProperties() {
        // initialise annotations if necessary
        getAnnotations();
        return this.physicalProperties != null ? this.physicalProperties.getValue() : null;
    }

    public void setPhysicalProperties(String properties) {
        Collection<Annotation> complexAnnotationList = getAnnotations();

        // add new physical properties if not null
        if (properties != null){

            CvTerm complexPhysicalProperties = IntactUtils.createMITopic(Annotation.COMPLEX_PROPERTIES, Annotation.COMPLEX_PROPERTIES_MI);
            // first remove old physical property if not null
            if (this.physicalProperties != null){
                this.physicalProperties.setValue(properties);
            }
            else{
                this.physicalProperties = new InteractorAnnotation(complexPhysicalProperties, properties);
                complexAnnotationList.add(this.physicalProperties);
            }
        }
        // remove all physical properties if the collection is not empty
        else if (!complexAnnotationList.isEmpty()) {
            AnnotationUtils.removeAllAnnotationsWithTopic(complexAnnotationList, Annotation.COMPLEX_PROPERTIES_MI, Annotation.COMPLEX_PROPERTIES);
            physicalProperties = null;
        }
    }

    @Transient
    public String getRecommendedName() {
        // initialise aliases if necessary
        getAliases();
        return this.recommendedName != null ? this.recommendedName.getName() : null;
    }

    public void setRecommendedName(String name) {
        Collection<Alias> complexAliasList = getAliases();

        // add new recommended name if not null
        if (name != null){

            CvTerm recommendedName = IntactUtils.createMIAliasType(Alias.COMPLEX_RECOMMENDED_NAME, Alias.COMPLEX_RECOMMENDED_NAME_MI);
            // first remove old recommended name if not null
            if (this.recommendedName != null && !name.equals(this.recommendedName.getName())){
                if (this.recommendedName instanceof AbstractIntactAlias){
                    ((AbstractIntactAlias) this.recommendedName).setName(name);
                }
                else{
                    complexAliasList.remove(this.recommendedName);
                    this.recommendedName = new InteractorAlias(recommendedName, name);
                    complexAliasList.add(this.recommendedName);
                }
            }
            else if (this.recommendedName == null){
                this.recommendedName = new InteractorAlias(recommendedName, name);
                complexAliasList.add(this.recommendedName);
            }
        }
        // remove all recommended name if the collection is not empty
        else if (!complexAliasList.isEmpty()) {
            AliasUtils.removeAllAliasesWithType(complexAliasList, Alias.COMPLEX_RECOMMENDED_NAME_MI, Alias.COMPLEX_RECOMMENDED_NAME);
            recommendedName = null;
        }
    }

    @Transient
    public String getSystematicName() {
        // initialise aliases if necessary
        getAliases();
        return this.systematicName != null ? this.systematicName.getName() : null;
    }

    public void setSystematicName(String name) {
        Collection<Alias> complexAliasList = getAliases();

        // add new systematic name if not null
        if (name != null){

            CvTerm systematicName = IntactUtils.createMIAliasType(Alias.COMPLEX_SYSTEMATIC_NAME, Alias.COMPLEX_SYSTEMATIC_NAME_MI);
            // first remove systematic name  if not null
            if (this.systematicName != null && !name.equals(this.systematicName)){
                if (this.systematicName instanceof AbstractIntactAlias){
                    ((AbstractIntactAlias) this.systematicName).setName(name);
                }
                else{
                    complexAliasList.remove(this.systematicName);
                    this.systematicName = new InteractorAlias(systematicName, name);
                    complexAliasList.add(this.systematicName);
                }
            }
            else if (this.systematicName == null){
                this.systematicName = new InteractorAlias(systematicName, name);
                complexAliasList.add(this.systematicName);
            }
        }
        // remove all systematic name  if the collection is not empty
        else if (!complexAliasList.isEmpty()) {
            AliasUtils.removeAllAliasesWithType(complexAliasList, Alias.COMPLEX_SYSTEMATIC_NAME_MI, Alias.COMPLEX_SYSTEMATIC_NAME);
            systematicName = null;
        }
    }

    @ManyToMany(targetEntity = IntactExperiment.class)
    @JoinTable(
            name = "ia_int2exp",
            joinColumns = {@JoinColumn( name = "interaction_ac" )},
            inverseJoinColumns = {@JoinColumn( name = "experiment_ac" )}
    )
    @Target(IntactExperiment.class)
    @LazyCollection(LazyCollectionOption.FALSE)  //It will contain only one experiment for complexes, should be OK
    @Deprecated
    /**
     * This method should not be used in any applications. It is only for synchronization with the database and backward compatibility
     * with intact-core
     * @deprecated Only kept for backward compatibility with intact core. Complexes should not have experiments
     */
    public Collection<Experiment> getExperiments() {
        if (experiments == null){
            experiments = new ArrayList<Experiment>();
        }
        return experiments;
    }

    private void setExperiments(Collection<Experiment> experiments) {
        this.experiments = experiments;
    }

    @Transient
    public String getRigid() {
        // initialise checksum if necessary
        getChecksums();
        return this.rigid != null ? this.rigid.getValue() : null;
    }

    public void setRigid(String rigid) {
        Collection<Checksum> checksums = getChecksums();
        if (rigid != null){
            CvTerm rigidMethod = IntactUtils.createMITopic(Checksum.RIGID, null);
            // first remove old rigid
            if (this.rigid != null){
                checksums.remove(this.rigid);
            }
            this.rigid = new DefaultChecksum(rigidMethod, rigid);
            checksums.add(this.rigid);
        }
        // remove all smiles if the collection is not empty
        else if (!checksums.isEmpty()) {
            ChecksumUtils.removeAllChecksumWithMethod(checksums, Checksum.RIGID_MI, Checksum.RIGID);
            this.rigid = null;
        }
    }

    @Transient
    public Date getUpdatedDate() {
        return getUpdated();
    }

    public void setUpdatedDate(Date updated) {
        setUpdated(updated);
    }

    @Transient
    public Date getCreatedDate() {
        return getCreated();
    }

    public void setCreatedDate(Date created) {
        setCreated(created);
    }

    @ManyToOne(targetEntity = IntactCvTerm.class)
    @JoinColumn( name = "interactiontype_ac", referencedColumnName = "ac")
    @Target(IntactCvTerm.class)
    public CvTerm getInteractionType() {
        return this.interactionType;
    }

    public void setInteractionType(CvTerm term) {
        this.interactionType = term;
    }

    /*@ManyToMany(targetEntity = IntactInteractionEvidence.class)
    @JoinTable(
            name = "ia_complex2evidence",
            joinColumns = {@JoinColumn( name = "complex_ac" )},
            inverseJoinColumns = {@JoinColumn( name = "evidence_ac" )}
    )
    @Target(IntactInteractionEvidence.class) */
    @Transient
    public Collection<InteractionEvidence> getInteractionEvidences() {
        if (interactionEvidences == null){
            initialiseInteractionEvidences();
        }
        return this.interactionEvidences;
    }

    private void setInteractionEvidences(Collection<InteractionEvidence> interactionEvidences) {
        this.interactionEvidences = interactionEvidences;
    }

    @Transient
    public boolean areParticipantsInitialized(){
        return Hibernate.isInitialized(getParticipants());
    }

    @Transient
    public boolean areCooperativeEffectsInitialized(){
        return Hibernate.isInitialized(getCooperativeEffects());
    }

    @Transient
    public boolean areParametersInitialized(){
        return Hibernate.isInitialized(getModelledParameters());
    }

    @Transient
    public boolean areConfidencesInitialized(){
        return Hibernate.isInitialized(getModelledConfidences());
    }

    @Transient
    public boolean areExperimentsInitialized(){
        return Hibernate.isInitialized(getExperiments());
    }

    @Override
    protected void initialiseDefaultInteractorType() {
        super.setInteractorType(IntactUtils.createIntactMITerm(Complex.COMPLEX, Complex.COMPLEX_MI, IntactUtils.INTERACTOR_TYPE_OBJCLASS));
    }

    @Override
    protected String generateObjClass() {
        return "uk.ac.ebi.intact.model.InteractionImpl";
    }

    @Override
    protected void initialiseChecksums(){
        super.initialiseChecksumsWith(new ComplexChecksumList());
    }

    @Override
    protected boolean needToProcessAnnotationToAdd() {
        return true;
    }

    @Override
    protected boolean needToProcessAliasToAdd() {
        return true;
    }

    @Override
    protected boolean needToProcessAnnotationToRemove() {
        return true;
    }

    @Override
    protected boolean needToProcessAliasToRemove() {
        return true;
    }

    @Override
    protected void processAddedXrefEvent(Xref added) {
        // the added identifier is complex ac
        if (XrefUtils.isXrefFromDatabase(added, Xref.COMPLEX_PORTAL_MI, Xref.COMPLEX_PORTAL) &&
                XrefUtils.doesXrefHaveQualifier(added, Xref.COMPLEX_PRIMARY_MI, Xref.COMPLEX_PRIMARY)) {
            // first remove old ac if not null
            if (complexAcXref != null) {
                // the added xref is complex-primary
                // first remove old complexAcXref and add the new one;
                getXrefs().remove(this.complexAcXref);
            }
            complexAcXref = added;
        }
    }

    @Override
    protected void processRemovedXrefEvent(Xref removed) {
        // the removed identifier is pubmed
        if (complexAcXref != null && complexAcXref.equals(removed)){
            Collection<Xref> existingComplexAcXref = XrefUtils.collectAllXrefsHavingDatabaseAndQualifier(getXrefs(),
                    Xref.COMPLEX_PORTAL_MI, Xref.COMPLEX_PORTAL, Xref.COMPLEX_PRIMARY_MI, Xref.COMPLEX_PRIMARY);
            if (!existingComplexAcXref.isEmpty()){
                complexAcXref = existingComplexAcXref.iterator().next();
            }
        }
    }

    @Override
    protected void clearPropertiesLinkedToXrefs() {
        complexAcXref = null;
    }

    @Override
    protected void processAddedAnnotation(Annotation added) {
        if (physicalProperties == null &&
                AnnotationUtils.doesAnnotationHaveTopic(added, Annotation.COMPLEX_PROPERTIES_MI, Annotation.COMPLEX_PROPERTIES)){
            physicalProperties = added;
        }
        else if (toBeReviewed == null &&
                AnnotationUtils.doesAnnotationHaveTopic(added, null, Releasable.TO_BE_REVIEWED)){
            toBeReviewed = added;
        }
        else if (accepted == null &&
                AnnotationUtils.doesAnnotationHaveTopic(added, null, Releasable.ACCEPTED)){
            accepted = added;
        }
        else if (onHold == null &&
                AnnotationUtils.doesAnnotationHaveTopic(added, null, Releasable.ON_HOLD)){
            onHold = added;
        }
        else if (correctionComment == null &&
                AnnotationUtils.doesAnnotationHaveTopic(added, null, Releasable.CORRECTION_COMMENT)){
            correctionComment = added;
        }
    }


    @Override
    protected void processRemovedAnnotation(Annotation removed) {
        if (physicalProperties != null && physicalProperties.equals(removed)){
            physicalProperties = AnnotationUtils.collectFirstAnnotationWithTopic(getAnnotations(),
                    Annotation.COMPLEX_PROPERTIES_MI, Annotation.COMPLEX_PROPERTIES);
        }
        if (toBeReviewed != null && toBeReviewed.equals(removed)){
            toBeReviewed = AnnotationUtils.collectFirstAnnotationWithTopic(getAnnotations(),
                    null, Releasable.TO_BE_REVIEWED);
        }
        if (accepted != null && accepted.equals(removed)){
            accepted = AnnotationUtils.collectFirstAnnotationWithTopic(getAnnotations(),
                    null, Releasable.ACCEPTED);
        }
        if (onHold != null && onHold.equals(removed)){
            onHold = AnnotationUtils.collectFirstAnnotationWithTopic(getAnnotations(),
                    null, Releasable.ON_HOLD);
        }
        if (correctionComment != null && correctionComment.equals(removed)){
            correctionComment = AnnotationUtils.collectFirstAnnotationWithTopic(getAnnotations(),
                    null, Releasable.CORRECTION_COMMENT);
        }
    }

    @Override
    protected void processAddedAlias(Alias added) {
        if (recommendedName == null && AliasUtils.doesAliasHaveType(added, Alias.COMPLEX_RECOMMENDED_NAME_MI, Alias.COMPLEX_RECOMMENDED_NAME)){
            recommendedName = added;
        }
        else if (systematicName == null && AliasUtils.doesAliasHaveType(added, Alias.COMPLEX_SYSTEMATIC_NAME_MI, Alias.COMPLEX_SYSTEMATIC_NAME)){
            systematicName = added;
        }
    }

    @Override
    protected void processRemovedAlias(Alias removed) {
        if (recommendedName != null && recommendedName.equals(removed)){
            recommendedName = AliasUtils.collectFirstAliasWithType(getAliases(), Alias.COMPLEX_RECOMMENDED_NAME_MI, Alias.COMPLEX_RECOMMENDED_NAME);
        }
        else if (systematicName != null && systematicName.equals(removed)){
            systematicName = AliasUtils.collectFirstAliasWithType(getAliases(), Alias.COMPLEX_SYSTEMATIC_NAME_MI, Alias.COMPLEX_SYSTEMATIC_NAME);
        }
    }

    @Override
    protected void initialiseChecksumsWith(Collection<Checksum> checksum) {
        ComplexChecksumList checksumList = new ComplexChecksumList();
        checksumList.addAllOnly(checksum);
        super.initialiseChecksumsWith(checksumList);
    }

    private void processAddedChecksumEvent(Checksum added) {
        if (rigid == null && ChecksumUtils.doesChecksumHaveMethod(added, Checksum.RIGID_MI, Checksum.RIGID)) {
            // the rigid is not set, we can set the rigid
            rigid = added;
        }
    }

    private void processRemovedChecksumEvent(Checksum removed) {
        if (rigid == removed) {
            rigid = ChecksumUtils.collectFirstChecksumWithMethod(getChecksums(), Checksum.RIGID_MI, Checksum.RIGID);
        }
    }

    private void initialiseInteractionEvidences() {
        this.interactionEvidences = new ArrayList<InteractionEvidence>();
    }

    private void initialiseCooperativeEffects() {
        this.cooperativeEffects = new ArrayList<CooperativeEffect>();
    }

    private void initialiseConfidences() {
        this.confidences = new ArrayList<ModelledConfidence>();
    }

    private void initialiseParameters() {
        this.parameters = new ArrayList<ModelledParameter>();
    }

    private void initialiseComponents() {
        this.components = new ArrayList<ModelledParticipant>();
    }

    private void clearPropertiesLinkedToChecksums() {
        this.rigid = null;
    }

    @Override
    public void resetCachedDbProperties() {
        super.resetCachedDbProperties();
        this.physicalProperties = null;
        this.systematicName = null;
        this.recommendedName = null;
        this.complexAcXref = null;
        this.status = null;
        this.onHold = null;
        this.toBeReviewed = null;
        this.correctionComment = null;
        this.accepted = null;
    }

    @Override
    protected void setDbXrefs(Collection<Xref> persistentXrefs) {
        super.setDbXrefs(persistentXrefs);
        this.complexAcXref = null;
    }

    @Override
    protected void setDbAliases(Collection<Alias> aliases) {
        super.setDbAliases(aliases);
        this.systematicName = null;
        this.recommendedName = null;
    }

    @Override
    protected void setDbAnnotations(Collection<Annotation> annotations) {
        super.setDbAnnotations(annotations);
        this.onHold = null;
        this.toBeReviewed = null;
        this.correctionComment = null;
        this.accepted = null;
        this.physicalProperties = null;
    }

    /**
     * Overrides serialization for xrefs and annotations (inner classes not serializable)
     * @param oos
     * @throws java.io.IOException
     */
    private void writeObject(ObjectOutputStream oos)
            throws IOException {
        // default serialization
        oos.defaultWriteObject();
        // write the status
        oos.writeObject(getCvStatus());
    }

    /**
     * Overrides serialization for xrefs and annotations (inner classes not serializable)
     * @param ois
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException {
        // default deserialization
        ois.defaultReadObject();
        // read default status
        setCvStatus((CvTerm)ois.readObject());
    }

    private class ComplexChecksumList extends AbstractListHavingProperties<Checksum> {
        public ComplexChecksumList(){
            super();
        }

        @Override
        protected void processAddedObjectEvent(Checksum checksum) {
            processAddedChecksumEvent(checksum);
        }

        @Override
        protected void processRemovedObjectEvent(Checksum checksum) {
            processRemovedChecksumEvent(checksum);
        }

        @Override
        protected void clearProperties() {
            clearPropertiesLinkedToChecksums();
        }
    }
}
