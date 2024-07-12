package uk.ac.ebi.intact.jami.model.extension;

import org.hibernate.annotations.Target;
import psidev.psi.mi.jami.model.CvTerm;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractXrefWithEvidenceType extends InteractorXref {

    private CvTerm evidenceType;

    protected AbstractXrefWithEvidenceType() {
    }

    public AbstractXrefWithEvidenceType(CvTerm database, String id) {
        super(database, id);
    }

    public AbstractXrefWithEvidenceType(CvTerm database, String id, CvTerm qualifier) {
        super(database, id, qualifier);
    }

    public AbstractXrefWithEvidenceType(CvTerm database, String id, String version) {
        super(database, id, version);
    }

    public AbstractXrefWithEvidenceType(CvTerm database, String id, String version, CvTerm qualifier) {
        super(database, id, version, qualifier);
    }

    @ManyToOne(targetEntity = IntactCvTerm.class)
    @JoinColumn(name = "evidencetype_ac")
    @Target(IntactCvTerm.class)
    public CvTerm getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(CvTerm evidenceType) {
        this.evidenceType = evidenceType;
    }
}
