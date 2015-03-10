package uk.ac.ebi.intact.jami.model.extension;

import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.ModelledConfidence;
import psidev.psi.mi.jami.model.Publication;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Intact implementation of complex confidence
 *
 * Future improvements: this table will be ia_complex_confidence once we deprecates intact-core and split complexes
 * and interaction evidences in two separate tables
 *
 * For the moment, publications in confidences are not persistent but may be in the future
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14/01/14</pre>
 */
@Entity
@Table(name = "ia_confidence")
public class ComplexConfidence extends AbstractIntactConfidence implements ModelledConfidence{

    private Publication publication;

    protected ComplexConfidence() {
    }

    public ComplexConfidence(CvTerm type, String value) {
        super(type, value);
    }

    @Transient
    public Publication getPublication() {
        return this.publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }
}
