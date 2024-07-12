package uk.ac.ebi.intact.jami.model.extension;

import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.utils.CvTermUtils;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("humap")
public class ComplexHumapXref extends AbstractXrefWithEvidenceType {

    // TODO: replace the following id with MI id when this term is created in OLS and imported in the DB
    public static final String HUMAP_DATABASE_MI = "IA:3601";
    public static final String HUMAP_DATABASE = "humap";

    protected ComplexHumapXref() {
    }

    public ComplexHumapXref(CvTerm database, String id) {
        super(database, id);
    }

    public ComplexHumapXref(CvTerm database, String id, CvTerm qualifier) {
        super(database, id, qualifier);
    }

    public ComplexHumapXref(CvTerm database, String id, String version) {
        super(database, id, version);
    }

    public ComplexHumapXref(CvTerm database, String id, String version, CvTerm qualifier) {
        super(database, id, version, qualifier);
    }

    @Override
    public void setDatabase(CvTerm cvDatabase) {
        if (cvDatabase != null && cvDatabase.getShortName() != null && !CvTermUtils.isCvTerm(cvDatabase, HUMAP_DATABASE_MI, HUMAP_DATABASE)) {
            throw new IllegalArgumentException("A Complex huMAP cross reference can only have huMAP as a database.");
        }
        super.setDatabase(cvDatabase);
    }
}
