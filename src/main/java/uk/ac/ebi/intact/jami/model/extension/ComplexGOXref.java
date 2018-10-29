package uk.ac.ebi.intact.jami.model.extension;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Target;
import org.hibernate.validator.constraints.NotBlank;
import org.junit.After;
import org.junit.AfterClass;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.utils.CvTermUtils;
import uk.ac.ebi.intact.jami.constraints.CGXValidator;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * Implementation of xref for interactors
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/01/14</pre>
 */
@Entity
@DiscriminatorValue( "go" )
@CGXValidator
public class ComplexGOXref extends InteractorXref{

    private CvTerm evidenceType;
    private String pubmed;

    protected ComplexGOXref() {
    }

    public ComplexGOXref(String id, CvTerm qualifier) {
        super(IntactUtils.createMIDatabase(Xref.GO, Xref.GO_MI), id, qualifier);
    }

    public ComplexGOXref(String id, String version, CvTerm qualifier) {
        super(IntactUtils.createMIDatabase(Xref.GO, Xref.GO_MI), id, version, qualifier);
    }

    public ComplexGOXref(String id, String version) {
        super(IntactUtils.createMIDatabase(Xref.GO, Xref.GO_MI), id, version);
    }

    public ComplexGOXref(String id) {
        super(IntactUtils.createMIDatabase(Xref.GO, Xref.GO_MI), id);
    }

    @Override
    public void setDatabase(CvTerm cvDatabase) {
        super.setDatabase(cvDatabase);
    }

    @ManyToOne(targetEntity = IntactCvTerm.class)
    @JoinColumn( name = "evidencetype_ac" )
    @Target(IntactCvTerm.class)
    public CvTerm getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(CvTerm evidenceType) {
        this.evidenceType = evidenceType;
    }


    @Column(name = "pubmed")
    @Size( max = IntactUtils.MAX_ID_LEN )
    public String getPubmed() {
        return pubmed;
    }

    public void setPubmed(String pubmed) {
        this.pubmed = pubmed;
    }
}
