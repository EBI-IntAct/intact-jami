package uk.ac.ebi.intact.jami.dao;

import uk.ac.ebi.intact.jami.model.extension.IntactPolymer;

import java.util.Collection;

/**
 * Interactor DAO
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/14</pre>
 */

public interface PolymerDao<I extends IntactPolymer> extends InteractorDao<I> {

    String getSequenceByPolymerAc( String polymerAc );

    public Collection<I> getByCanonicalIds(String dbMI, Collection<String> primaryIds);
}
