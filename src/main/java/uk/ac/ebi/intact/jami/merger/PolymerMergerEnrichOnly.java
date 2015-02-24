package uk.ac.ebi.intact.jami.merger;

import psidev.psi.mi.jami.enricher.impl.full.FullPolymerEnricher;
import psidev.psi.mi.jami.model.Polymer;
import uk.ac.ebi.intact.jami.model.extension.IntactPolymer;
import uk.ac.ebi.intact.jami.synchronizer.impl.PolymerSynchronizerTemplate;

/**
 * Interactor merger based on the jami interactor enricher.
 * It will only add missing info, it does not override anything
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30/01/14</pre>
 */

public class PolymerMergerEnrichOnly<P extends Polymer, T extends IntactPolymer> extends InteractorBaseMergerEnrichOnly<P, T> {

    public PolymerMergerEnrichOnly(PolymerSynchronizerTemplate<P, T> intactSynchronizer){
        super(new FullPolymerEnricher<P>(intactSynchronizer));
    }
}

