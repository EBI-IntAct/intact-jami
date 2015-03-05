package uk.ac.ebi.intact.jami.merger;

import psidev.psi.mi.jami.enricher.impl.full.FullFeatureEvidenceEnricher;
import psidev.psi.mi.jami.model.FeatureEvidence;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;

/**
 * Feature merger based on the jami feature evidence enricher.
 * It will only add missing info, it does not override anything
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/01/14</pre>
 */

public class FeatureEvidenceMergerEnrichOnly extends FeatureMergerEnrichOnly<FeatureEvidence, IntactFeatureEvidence> {

    public FeatureEvidenceMergerEnrichOnly() {
        super(IntactFeatureEvidence.class, new FullFeatureEvidenceEnricher());
    }
}
