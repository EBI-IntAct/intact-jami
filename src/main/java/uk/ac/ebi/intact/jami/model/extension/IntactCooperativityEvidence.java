package uk.ac.ebi.intact.jami.model.extension;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Target;
import psidev.psi.mi.jami.model.CooperativityEvidence;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Publication;
import psidev.psi.mi.jami.utils.comparator.cooperativity.UnambiguousCooperativityEvidenceComparator;
import uk.ac.ebi.intact.jami.model.audit.AbstractAuditable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Intact implementation of cooperativity evidence
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14/01/14</pre>
 */
@Entity
@Table(name = "ia_cooperativity_evidence")
public class IntactCooperativityEvidence extends AbstractAuditable implements CooperativityEvidence{
    private Publication publication;
    private Collection<CvTerm> evidenceMethods;
    private Long id;

    protected IntactCooperativityEvidence(){

    }

    public IntactCooperativityEvidence(Publication publication){
        if (publication == null){
            throw new IllegalArgumentException("The publication cannot be null in a CooperativityEvidence");
        }
        this.publication = publication;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEFAULT_ID_SEQ")
    @SequenceGenerator(name="DEFAULT_ID_SEQ", sequenceName="DEFAULT_ID_SEQ", initialValue = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne( targetEntity = IntactPublication.class, optional = false)
    @JoinColumn( name = "publication_ac", referencedColumnName = "ac" )
    @Target(IntactPublication.class)
    @NotNull
    public Publication getPublication() {
        return this.publication;
    }

    public void setPublication(Publication publication) {
        if (publication == null){
            throw new IllegalArgumentException("The publication cannot be null in a CooperativityEvidence");
        }
        this.publication = publication;
    }

    @ManyToMany(targetEntity=IntactCvTerm.class)
    @JoinTable(
            name="ia_cooperativity_evidence2method",
            joinColumns=@JoinColumn(name="evidence_id"),
            inverseJoinColumns=@JoinColumn(name="method_ac")
    )
    @Target(IntactCvTerm.class)
    public Collection<CvTerm> getEvidenceMethods() {

        if (evidenceMethods == null){
            initialiseEvidenceMethods();
        }
        return evidenceMethods;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }

        if (!(o instanceof CooperativityEvidence)){
            return false;
        }

        return UnambiguousCooperativityEvidenceComparator.areEquals(this, (CooperativityEvidence) o);
    }

    @Override
    public int hashCode() {
        return UnambiguousCooperativityEvidenceComparator.hashCode(this);
    }

    @Override
    public String toString() {
        return publication != null ? publication.toString() : super.toString();
    }

    @Transient
    public boolean areEvidenceMethodsInitialized(){
        return Hibernate.isInitialized(getEvidenceMethods());
    }

    private void initialiseEvidenceMethods(){
        this.evidenceMethods = new ArrayList<CvTerm>();
    }

    private void setEvidenceMethods(Collection<CvTerm> evidenceMethods) {
        this.evidenceMethods = evidenceMethods;
    }
}
