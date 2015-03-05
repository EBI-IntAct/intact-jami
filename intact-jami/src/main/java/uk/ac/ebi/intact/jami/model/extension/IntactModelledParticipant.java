package uk.ac.ebi.intact.jami.model.extension;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Target;
import org.hibernate.annotations.Where;
import psidev.psi.mi.jami.model.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Intact implementation of modelled participant
 *
 * NOTE: if the participant is not a direct participant of an interaction but is part of a participantSet,
 * the interaction back reference will not be persistent. Only getDbParentPool will be persisted and getDbParentInteraction will return null
 * even if the participant has a back reference to the interaction.
 * NOTE: For backward compatibility with intact-core, a method getDbExperimentalRoles (deprecated) is present so the synchronizers can fill up a
 * 'neutral component' role for all modelled participants. This method should never be used in any applications and is public only so the synchronizers can
 * synchronize this property.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15/01/14</pre>
 */
@Entity
@Inheritance( strategy = InheritanceType.SINGLE_TABLE )
@Table(name = "ia_component")
@DiscriminatorColumn(name = "category", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("modelled_participant")
@Where(clause = "category = 'modelled_participant'")
public class IntactModelledParticipant extends AbstractIntactParticipant<ModelledInteraction, ModelledFeature> implements ModelledParticipant{

    private Collection<CvTerm> experimentalRoles;
    private Collection<CausalRelationship> relatedCausalRelationships;
    private Collection<CausalRelationship> relatedExperimentalCausalRelationships;
    private Collection<Range> relatedRanges;
    private Collection<Range> relatedExperimentalRanges;

    protected IntactModelledParticipant() {
        super();
    }

    public IntactModelledParticipant(Interactor interactor) {
        super(interactor);
    }

    public IntactModelledParticipant(Interactor interactor, CvTerm bioRole) {
        super(interactor, bioRole);
    }

    public IntactModelledParticipant(Interactor interactor, Stoichiometry stoichiometry) {
        super(interactor, stoichiometry);
    }

    public IntactModelledParticipant(Interactor interactor, CvTerm bioRole, Stoichiometry stoichiometry) {
        super(interactor, bioRole, stoichiometry);
    }

    @OneToMany( cascade = {CascadeType.ALL}, orphanRemoval = true, targetEntity = ModelledParticipantXref.class)
    @JoinColumn(name="parent_ac", referencedColumnName="ac")
    @Cascade( value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE} )
    @Target(ModelledParticipantXref.class)
    @Override
    public Collection<Xref> getXrefs() {
        return super.getXrefs();
    }

    @OneToMany( cascade = {CascadeType.ALL}, orphanRemoval = true, targetEntity = ModelledParticipantAnnotation.class)
    @Cascade( value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE} )
    @JoinTable(
            name="ia_component2annot",
            joinColumns = @JoinColumn( name="component_ac"),
            inverseJoinColumns = @JoinColumn( name="annotation_ac")
    )
    @Target(ModelledParticipantAnnotation.class)
    @Override
    /**
     * WARNING: The join table is for backward compatibility with intact-core.
     * When intact-core will be removed, the join table would disappear wnd the relation would become
     * @JoinColumn(name="parent_ac", referencedColumnName="ac")
     */
    public Collection<Annotation> getAnnotations() {
        return super.getAnnotations();
    }

    @OneToMany( cascade = {CascadeType.ALL}, orphanRemoval = true, targetEntity = ModelledParticipantAlias.class)
    @JoinColumn(name="parent_ac", referencedColumnName="ac")
    @Cascade( value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE} )
    @Target(ModelledParticipantAlias.class)
    @Override
    public Collection<Alias> getAliases() {
        return super.getAliases();
    }

    @OneToMany( cascade = {CascadeType.ALL}, orphanRemoval = true, targetEntity = ModelledCausalRelationship.class)
    @Cascade( value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE} )
    @JoinColumn(name="source_ac", referencedColumnName="ac")
    @Target(ModelledCausalRelationship.class)
    @Override
    public Collection<CausalRelationship> getCausalRelationships() {
        return super.getCausalRelationships();
    }

    @OneToMany( mappedBy = "target", targetEntity = ModelledCausalRelationship.class)
    @Target(ModelledCausalRelationship.class)
    /**
     * List of modelled causal relationships having this participant as target
     */
    public Collection<CausalRelationship> getRelatedCausalRelationships(){
        if (this.relatedCausalRelationships == null){
            this.relatedCausalRelationships = new ArrayList<CausalRelationship>();
        }
        return this.relatedCausalRelationships;
    }

    @OneToMany( mappedBy = "modelledTarget", targetEntity = ExperimentalCausalRelationship.class)
    @Target(ExperimentalCausalRelationship.class)
    /**
     * List of experimental causal relationships having this participant as target
     */
    public Collection<CausalRelationship> getRelatedExperimentalCausalRelationships(){
        if (this.relatedExperimentalCausalRelationships== null){
            this.relatedExperimentalCausalRelationships = new ArrayList<CausalRelationship>();
        }
        return this.relatedExperimentalCausalRelationships;
    }

    @OneToMany( mappedBy = "participant", targetEntity = ModelledRange.class)
    @Target(ModelledRange.class)
    /**
     * List of modelled ranges pointing to this participant
     */
    public Collection<Range> getRelatedRanges(){
        if (this.relatedRanges == null){
            this.relatedRanges = new ArrayList<Range>();
        }
        return this.relatedRanges;
    }

    @OneToMany( mappedBy = "modelledParticipant", targetEntity = ExperimentalRange.class)
    @Target(ExperimentalRange.class)
    /**
     * List of experimental ranges pointing to this participant
     */
    public Collection<Range> getRelatedExperimentalRanges(){
        if (this.relatedExperimentalRanges== null){
            this.relatedExperimentalRanges = new ArrayList<Range>();
        }
        return this.relatedExperimentalRanges;
    }

    @Override
    @OneToMany( mappedBy = "participant", cascade = {CascadeType.ALL}, orphanRemoval = true,
            targetEntity = IntactModelledFeature.class)
    @Cascade( value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE} )
    @Target(IntactModelledFeature.class)
    public Collection<ModelledFeature> getFeatures() {
        return super.getFeatures();
    }

    @Transient
    public boolean areExperimentalRolesInitialized(){
        return Hibernate.isInitialized(getDbExperimentalRoles());
    }

    @ManyToMany(targetEntity = IntactCvTerm.class)
    @JoinTable(
            name = "ia_component2exprole",
            joinColumns = {@JoinColumn( name = "component_ac" )},
            inverseJoinColumns = {@JoinColumn( name = "experimentalrole_ac" )}
    )
    @Target(IntactCvTerm.class)
    /**
     * @deprecated this method is for backward compatibility only. Modelled participant DO NOT have experimental roles so
     * this method should never be called unless we need it for backward compatibility with intact-core and by the synchronizers.
     */
    @Deprecated
    public Collection<CvTerm> getDbExperimentalRoles() {
        if (this.experimentalRoles == null){
            this.experimentalRoles =  new ArrayList<CvTerm>();
        }

        return experimentalRoles;
    }

    @ManyToOne( targetEntity = IntactComplex.class )
    @JoinColumn( name = "interaction_ac" )
    @Target(IntactComplex.class)
    @Override
    protected ModelledInteraction getDbParentInteraction() {
        return super.getDbParentInteraction();
    }

    protected void setDbExperimentalRoles(Collection<CvTerm> experimentalRoles) {
        this.experimentalRoles = experimentalRoles;
    }

    protected void setRelatedCausalRelationships(Collection<CausalRelationship> relatedCausalRelationships) {
        this.relatedCausalRelationships = relatedCausalRelationships;
    }

    protected void setRelatedExperimentalCausalRelationships(Collection<CausalRelationship> relatedCausalRelationships) {
        this.relatedExperimentalCausalRelationships = relatedCausalRelationships;
    }

    protected void setRelatedRanges(Collection<Range> relatedRanges) {
        this.relatedRanges = relatedRanges;
    }

    protected void setRelatedExperimentalRanges(Collection<Range> relatedRanges) {
        this.relatedExperimentalRanges = relatedRanges;
    }

}
