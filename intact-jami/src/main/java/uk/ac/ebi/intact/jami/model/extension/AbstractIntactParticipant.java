package uk.ac.ebi.intact.jami.model.extension;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Target;
import psidev.psi.mi.jami.listener.EntityInteractorChangeListener;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.jami.model.AbstractIntactPrimaryObject;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Abstract class for intact participants
 *
 * NOTE; all entities are in the same table for backward compatibility with intact-core. In the future, this will be updated
 * We distinguish entities from participant with interaction property in participants. That is why this abstract class is only @MappedSupperClass and
 * the inheritance stratedy is delegated to IntActModelledParticipant and IntactParticipantEvidence
 * In the meantime, because of backward compatibility issues,we use a where statement in IntActModelledParticipant and IntactParticipantEvidence.
 * NOTE: The participants have the ownership of the relation between interaction and participants. It means that to persist the relationship between interaction and participants,
 * the property getInteraction must be pointing to the right interaction.
 * NOTE: if the participant is not a direct participant of an interaction but is part of a participantSet, the interaction back reference will not be persistent.
 * NOTE: The features have the ownership of the relation between participant and features. It means that to persist the relationship between participant and features,
 * the property getParticipant in the feature must be pointing to the right participant. It is then recommended to use the provided addFeature and removeFeature methods to add/remove features
 * from the participant
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15/01/14</pre>
 */
@MappedSuperclass
public abstract class AbstractIntactParticipant<I extends Interaction, F extends Feature> extends AbstractIntactPrimaryObject implements Participant<I, F> {

    private Interactor interactor;
    private CvTerm biologicalRole;
    private Collection<Xref> xrefs;
    private Collection<Annotation> annotations;
    private Collection<Alias> aliases;
    private Stoichiometry stoichiometry;
    private Collection<CausalRelationship> causalRelationships;
    private Collection<F> features;
    private EntityInteractorChangeListener changeListener;

    private I interaction;

    private String shortName;

    protected AbstractIntactParticipant(){
        super();
    }

    public AbstractIntactParticipant(Interactor interactor){
        super();
        if (interactor == null){
            throw new IllegalArgumentException("The interactor cannot be null.");
        }
        this.interactor = interactor;
    }

    public AbstractIntactParticipant(Interactor interactor, CvTerm bioRole){
        super();
        if (interactor == null){
            throw new IllegalArgumentException("The interactor cannot be null.");
        }
        this.interactor = interactor;
        this.biologicalRole = bioRole;
    }

    public AbstractIntactParticipant(Interactor interactor, Stoichiometry stoichiometry){
        this(interactor);
        this.stoichiometry = stoichiometry;
    }

    public AbstractIntactParticipant(Interactor interactor, CvTerm bioRole, Stoichiometry stoichiometry){
        this(interactor, bioRole);
        this.stoichiometry = stoichiometry;
    }

    @Transient
    public I getInteraction() {
        return this.interaction;
    }

    public void setInteraction(I interaction) {
        this.interaction = interaction;
    }

    public void setInteractionAndAddParticipant(I interaction) {
        if (this.interaction != null){
            this.interaction.removeParticipant(this);
        }

        if (interaction != null){
            interaction.addParticipant(this);
        }
    }

    @ManyToOne( targetEntity = IntactInteractor.class, optional = false)
    @JoinColumn( name = "interactor_ac", referencedColumnName = "ac")
    @Target(IntactInteractor.class)
    @NotNull
    public Interactor getInteractor() {
        return this.interactor;
    }

    public void setInteractor(Interactor interactor) {
        if (interactor == null){
            throw new IllegalArgumentException("The interactor cannot be null.");
        }
        Interactor oldInteractor = this.interactor;
        this.interactor = interactor;
        if (oldInteractor != this.interactor){
            if (this.changeListener != null){
                this.changeListener.onInteractorUpdate(this, oldInteractor);
            }
        }
    }

    @ManyToOne(targetEntity = IntactCvTerm.class)
    @JoinColumn( name = "biologicalrole_ac", referencedColumnName = "ac")
    @Target(IntactCvTerm.class)
    public CvTerm getBiologicalRole() {
        if (this.biologicalRole == null){
            this.biologicalRole = IntactUtils.createMIBiologicalRole(Participant.UNSPECIFIED_ROLE, Participant.UNSPECIFIED_ROLE_MI);
        }
        return this.biologicalRole;
    }

    public void setBiologicalRole(CvTerm bioRole) {
        biologicalRole = bioRole;
    }

    @Transient
    public Collection<CausalRelationship> getCausalRelationships() {
        if (this.causalRelationships == null){
            initialiseCausalRelationships();
        }
        return this.causalRelationships;
    }

    @Transient
    public Collection<Xref> getXrefs() {
        if (xrefs == null){
            initialiseXrefs();
        }
        return this.xrefs;
    }

    @Transient
    /**
     * WARNING: The join table is for backward compatibility with intact-core.
     * When intact-core will be removed, the join table would disappear wnd the relation would become
     * @JoinColumn(name="parent_ac", referencedColumnName="ac")
     */
    public Collection<Annotation> getAnnotations() {
        if (annotations == null){
            initialiseAnnotations();
        }
        return this.annotations;
    }

    @Transient
    public Collection<Alias> getAliases() {
        if (aliases == null){
            initialiseAliases();
        }
        return this.aliases;
    }

    @Embedded
    @Target(IntactStoichiometry.class)
    public Stoichiometry getStoichiometry() {
        return this.stoichiometry;
    }

    public void setStoichiometry(Integer stoichiometry) {
        if (stoichiometry == null){
            this.stoichiometry = null;
        }
        else {
            this.stoichiometry = new IntactStoichiometry(stoichiometry, stoichiometry);
        }
    }

    public void setStoichiometry(Stoichiometry stoichiometry) {
        this.stoichiometry = stoichiometry;
    }

    @Transient
    /**
     *  NOTE: The features have the ownership of the relation between participant and features. It means that to persist the relationship between participant and features,
     * the property getParticipant in the feature must be pointing to the right participant. It is then recommended to use the provided addFeature and removeFeature methods to add/remove features
     * from the participant
     */
    public Collection<F> getFeatures() {
        if (features == null){
            initialiseFeatures();
        }
        return this.features;
    }

    @Transient
    public EntityInteractorChangeListener getChangeListener() {
        return this.changeListener;
    }

    public void setChangeListener(EntityInteractorChangeListener listener) {
        this.changeListener = listener;
    }

    public boolean addFeature(F feature) {

        if (feature == null){
            return false;
        }

        if (getFeatures().add(feature)){
            feature.setParticipant(this);
            return true;
        }
        return false;
    }

    public boolean removeFeature(F feature) {

        if (feature == null){
            return false;
        }

        if (getFeatures().remove(feature)){
            feature.setParticipant(null);
            return true;
        }
        return false;
    }

    public boolean addAllFeatures(Collection<? extends F> features) {
        if (features == null){
            return false;
        }

        boolean added = false;
        for (F feature : features){
            if (addFeature(feature)){
                added = true;
            }
        }
        return added;
    }

    public boolean removeAllFeatures(Collection<? extends F> features) {
        if (features == null){
            return false;
        }

        boolean added = false;
        for (F feature : features){
            if (removeFeature(feature)){
                added = true;
            }
        }
        return added;
    }

    @Override
    public String toString() {
        return interactor.toString() + " ( " + biologicalRole.toString() + ")" + (stoichiometry != null ? ", stoichiometry: " + stoichiometry.toString() : "");
    }

    @Transient
    public boolean areXrefsInitialized(){
        return Hibernate.isInitialized(getXrefs());
    }

    @Transient
    public boolean areAliasesInitialized(){
        return Hibernate.isInitialized(getAliases());
    }

    @Transient
    public boolean areAnnotationsInitialized(){
        return Hibernate.isInitialized(getAnnotations());
    }

    @Transient
    public boolean areFeaturesInitialized(){
        return Hibernate.isInitialized(getFeatures());
    }

    @Transient
    public boolean areCausalRelationshipsInitialized(){
        return Hibernate.isInitialized(getCausalRelationships());
    }

    @Transient
    /**
     * The parent interaction is not null if this participant is not part of a participant pool and is a direct participant of an interaction.
     * The parent pool is important as it is used to decide if we persist the interaction_ac or not.
     * We only persist the interaction_ac if the participant is not part of a participant pool
     */
    protected I getDbParentInteraction() {
        return interaction;
    }

    protected void setDbParentInteraction(I parentInteraction) {
        this.interaction = parentInteraction;
    }

    protected void initialiseXrefs() {
        this.xrefs = new ArrayList<Xref>();
    }

    protected void initialiseAnnotations() {
        this.annotations = new ArrayList<Annotation>();
    }

    protected void initialiseAliases(){
        this.aliases = new ArrayList<Alias>();
    }

    protected void initialiseFeatures(){
        this.features = new ArrayList<F>();
    }

    protected void initialiseCausalRelationships(){
        this.causalRelationships = new ArrayList<CausalRelationship>();
    }

    protected void setAnnotations(Collection<Annotation> annotations) {
        this.annotations = annotations;
    }

    protected void setCausalRelationships(Collection<CausalRelationship> causalRelationships) {
        this.causalRelationships = causalRelationships;
    }

    protected void setXrefs(Collection<Xref> xrefs) {
        this.xrefs = xrefs;
    }

    protected void setAliases(Collection<Alias> aliases) {
        this.aliases = aliases;
    }

    protected void setFeatures(Collection<F> features) {
        this.features = features;
    }

    @Column(name = "shortlabel", nullable = false)
    @Size( min = 1, max = IntactUtils.MAX_SHORT_LABEL_LEN )
    @NotNull
    @Deprecated
    /**
     * @deprecated only for backward compatibility with intact core.
     */
    protected String getShortLabel() {
        if (this.shortName == null){
            this.shortName = "N/A";
        }
        return shortName;
    }

    /**
     *
     * @param shortName
     * @deprecated only for backward compatibility with intact core
     */
    @Deprecated
    protected void setShortLabel(String shortName) {
        this.shortName = shortName;
    }
}
