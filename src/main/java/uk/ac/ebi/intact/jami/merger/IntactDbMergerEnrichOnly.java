package uk.ac.ebi.intact.jami.merger;

import psidev.psi.mi.jami.enricher.MIEnricher;
import uk.ac.ebi.intact.jami.model.audit.Auditable;

/**
 * This IntAct merger, only enrich properties of object loaded from the database and
 * does not override any of its properties that are different from the source object properties.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/01/14</pre>
 */

public class IntactDbMergerEnrichOnly<I,A extends Auditable> extends AbstractIntactDbMerger<I,A> {

    public IntactDbMergerEnrichOnly() {
        super((Class<A>)Auditable.class);
    }

    public IntactDbMergerEnrichOnly(MIEnricher<I> basicEnricher) {
        super((Class<A>)Auditable.class, basicEnricher);
    }

    public IntactDbMergerEnrichOnly(Class<A> intactClass) {
        super(intactClass);
    }

    public IntactDbMergerEnrichOnly(Class<A> intactClass, MIEnricher<I> basicEnricher) {
        super(intactClass, basicEnricher);
    }

    @Override
    protected void mergeOtherProperties(A obj1, A obj2) {
        // nothing to do
    }

    @Override
    protected void mergeCreator(A obj1, A obj2) {
        if (obj2.getCreator() == null){
            obj2.setCreator(obj1.getCreator());
        }
    }

    @Override
    protected void mergeCreatedDate(A obj1, A obj2) {
        if (obj2.getCreated() == null){
            obj2.setCreated(obj1.getCreated());
        }
    }

    @Override
    protected void mergeUpdateDate(A obj1, A obj2) {
        if (obj2.getUpdated() == null){
            obj2.setUpdated(obj1.getUpdated());
        }
    }

    @Override
    protected void mergeUpdator(A obj1, A obj2) {
        if (obj2.getUpdator() == null){
            obj2.setUpdator(obj1.getUpdator());
        }
    }
}
