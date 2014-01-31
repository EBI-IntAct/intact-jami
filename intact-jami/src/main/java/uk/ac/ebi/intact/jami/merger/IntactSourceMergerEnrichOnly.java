package uk.ac.ebi.intact.jami.merger;

import psidev.psi.mi.jami.bridges.fetcher.CvTermFetcher;
import psidev.psi.mi.jami.enricher.PublicationEnricher;
import psidev.psi.mi.jami.enricher.SourceEnricher;
import psidev.psi.mi.jami.enricher.impl.FullSourceEnricher;
import psidev.psi.mi.jami.enricher.listener.CvTermEnricherListener;
import psidev.psi.mi.jami.model.Source;
import uk.ac.ebi.intact.jami.model.extension.IntactSource;
import uk.ac.ebi.intact.jami.synchronizer.IntactSourceSynchronizer;

/**
 * Source merger based on the jami source enricher.
 * It will only add missing info, it does not override anything
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/01/14</pre>
 */

public class IntactSourceMergerEnrichOnly extends IntactDbMergerEnrichOnly<Source, IntactSource> implements SourceEnricher {

    public IntactSourceMergerEnrichOnly(IntactSourceSynchronizer intactSynchronizer){
        super(IntactSource.class, new FullSourceEnricher(intactSynchronizer));
    }

    public void setPublicationEnricher(PublicationEnricher enricher) {
        getBasicEnricher().setPublicationEnricher(enricher);
    }

    public PublicationEnricher getPublicationEnricher() {
        return getBasicEnricher().getPublicationEnricher();
    }

    @Override
    protected SourceEnricher getBasicEnricher() {
        return (SourceEnricher)super.getBasicEnricher();
    }

    public CvTermFetcher<Source> getCvTermFetcher() {
        return getBasicEnricher().getCvTermFetcher();
    }

    public void setCvTermEnricherListener(CvTermEnricherListener<Source> listener) {
         getBasicEnricher().setCvTermEnricherListener(listener);
    }

    public CvTermEnricherListener<Source> getCvTermEnricherListener() {
        return getBasicEnricher().getCvTermEnricherListener();
    }
}