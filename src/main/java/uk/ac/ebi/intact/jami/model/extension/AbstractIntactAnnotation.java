package uk.ac.ebi.intact.jami.model.extension;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Target;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.utils.comparator.annotation.UnambiguousAnnotationComparator;
import uk.ac.ebi.intact.jami.model.AbstractIntactPrimaryObject;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Abstract Intact implementation of annotation
 *
 * Note: this implementation was chosen because annotations do not make sense without their parents and are not shared by different entities
 * It is then better to have several annotation tables, one for each entity rather than one big annotation table and x join tables.
 *
 * However, for backward compatibility with intact-core, we will keep a mapping oneToMany with a join table.
 * All the extensions of AbstractIntactAnnotation (excepted CooperativeEffectAnnotation) will point to the same table ia_annotation.
 * It would be better to never query for an annotation without involving its parent.
 *
 * Future improvements: this class would become an entity with Inheritance=TABLE_PER_CLASS and all subclasses would be a different table
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>07/01/14</pre>
 */
@MappedSuperclass
public abstract class AbstractIntactAnnotation extends AbstractIntactPrimaryObject implements Annotation{

    ///////////////////////////////////////
    //attributes

    /**
     * Text describing one aspect of the annotation of
     * an object.
     */
    private String value;

    ///////////////////////////////////////
    // associations

    /**
     * Type of the annotation
     */
    private CvTerm topic;

    /**
     * This constructor should <b>not</b> be used as it could
     * result in objects with invalid state. It is here for object mapping
     * purposes only and if possible will be made private.
     *
     */
    protected AbstractIntactAnnotation() {
        //super call sets creation time data
        super();
    }

    public AbstractIntactAnnotation(CvTerm topic){
        if (topic == null){
            throw new IllegalArgumentException("The annotation topic is required and cannot be null");
        }
        this.topic = topic;
    }

    public AbstractIntactAnnotation(CvTerm topic, String value){
        this(topic);
        this.value = value;
    }

    ///////////////////////////////////////
    //access methods for attributes
    @Column( name = "description", length = IntactUtils.MAX_DESCRIPTION_LEN )
    @Size( max = IntactUtils.MAX_DESCRIPTION_LEN )
    public String getValue() {
        return value;
    }

    public void setValue( String annotationText ) {
        this.value = annotationText;
    }

    ///////////////////////////////////////
    // access methods for associations
    @ManyToOne(targetEntity = IntactCvTerm.class)
    @JoinColumn( name = "topic_ac", referencedColumnName = "ac")
    @ForeignKey(name = "FK_ANNOTATION$TOPIC")
    @Target(IntactCvTerm.class)
    @NotNull
    public CvTerm getTopic() {
        return topic;
    }

    public void setTopic( CvTerm cvTopic ) {
        if (cvTopic == null){
            throw new IllegalArgumentException("The annotation topic is required and cannot be null");
        }
        this.topic = cvTopic;
    }

    @Override
    public int hashCode() {
        return UnambiguousAnnotationComparator.hashCode(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }

        if (!(o instanceof Annotation)){
            return false;
        }

        return UnambiguousAnnotationComparator.areEquals(this, (Annotation) o);
    }

    @Override
    public String toString() {
        return topic.toString()+(value != null ? ": " + value : "");
    }
}
