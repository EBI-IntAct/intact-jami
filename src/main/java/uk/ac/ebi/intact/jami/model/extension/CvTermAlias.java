package uk.ac.ebi.intact.jami.model.extension;

import psidev.psi.mi.jami.model.CvTerm;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Implementation of alias for cv terms
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/01/14</pre>
 */
@Entity
@Table( name = "ia_controlledvocab_alias" )
public class CvTermAlias extends AbstractIntactAlias{

    protected CvTermAlias() {
    }

    public CvTermAlias(CvTerm type, String name) {
        super(type, name);
    }

    public CvTermAlias(String name) {
        super(name);
    }
}
