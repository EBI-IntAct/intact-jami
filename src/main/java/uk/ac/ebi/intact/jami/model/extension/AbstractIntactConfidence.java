package uk.ac.ebi.intact.jami.model.extension;

import org.hibernate.annotations.Target;
import psidev.psi.mi.jami.model.Confidence;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.utils.comparator.confidence.UnambiguousConfidenceComparator;
import uk.ac.ebi.intact.jami.model.AbstractIntactPrimaryObject;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Abstract class for confidence
 * Note: this implementation was chosen because confidences do not make sense without their parents and are not shared by different entities
 * It is then better to have several confidence tables, one for each entity rather than one big confidence table and x join tables.
 *
 * It would be better to never query for a confidence without involving its parent.
 *
 * Future improvements: this class would become an entity with Inheritance=TABLE_PER_CLASS and all subclasses would be a different table.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14/01/14</pre>
 */
@MappedSuperclass
public abstract class AbstractIntactConfidence extends AbstractIntactPrimaryObject implements Confidence{

    private CvTerm type;
    private String value;

    protected AbstractIntactConfidence(){
        super();
    }

    public AbstractIntactConfidence(CvTerm type, String value){
        super();
        if (type == null){
            throw new IllegalArgumentException("The confidence type is required and cannot be null");
        }
        this.type = type;
        if (value == null){
            throw new IllegalArgumentException("The confidence value is required and cannot be null");
        }
        this.value = value;
    }

    @ManyToOne(targetEntity = IntactCvTerm.class, optional = false)
    @JoinColumn( name = "confidencetype_ac", referencedColumnName = "ac")
    @Target(IntactCvTerm.class)
    @NotNull
    public CvTerm getType() {
        return this.type;
    }

    public void setType(CvTerm type) {
        if (type == null){
            throw new IllegalArgumentException("The confidence type is required and cannot be null");
        }
        this.type = type;
    }

    @Size(max = IntactUtils.MAX_DESCRIPTION_LEN)
    @NotNull
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        if (value == null){
            throw new IllegalArgumentException("The confidence value is required and cannot be null");
        }
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }

        if (!(o instanceof Confidence)){
            return false;
        }

        return UnambiguousConfidenceComparator.areEquals(this, (Confidence) o);
    }

    @Override
    public String toString() {
        return type.toString() + ": " + value;
    }

    @Override
    public int hashCode() {
        return UnambiguousConfidenceComparator.hashCode(this);
    }
}
