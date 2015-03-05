package uk.ac.ebi.intact.jami.synchronizer.impl;

import psidev.psi.mi.jami.model.ResultingSequence;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.jami.context.SynchronizerContext;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactResultingSequence;
import uk.ac.ebi.intact.jami.model.extension.ExperimentalRange;
import uk.ac.ebi.intact.jami.model.extension.ExperimentalResultingSequence;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;

import java.util.ArrayList;
import java.util.List;

/**
 * Default synchronizer/finder for Ranges attached to feature evidences
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27/01/14</pre>
 */

public class ExperimentalRangeSynchronizer extends RangeSynchronizerTemplate<ExperimentalRange> {

    public ExperimentalRangeSynchronizer(SynchronizerContext context){
        super(context, ExperimentalRange.class);
    }

    @Override
    protected void prepareResultingSequence(ExperimentalRange object, boolean enableSynchronization) throws FinderException, PersisterException, SynchronizerException {
        // prepare ResultingSequence
        if (object.getResultingSequence() != null){
            if (!(object.getResultingSequence() instanceof ExperimentalResultingSequence)){
                ResultingSequence reSeq = object.getResultingSequence();
                object.setResultingSequence(new ExperimentalResultingSequence(reSeq.getOriginalSequence(), reSeq.getNewSequence()));
                object.getResultingSequence().getXrefs().addAll(reSeq.getXrefs());
            }

            // prepare xrefs of resulting sequence
            prepareXrefs((ExperimentalResultingSequence)object.getResultingSequence(), enableSynchronization);

        }
    }

    @Override
    protected void prepareXrefs(AbstractIntactResultingSequence intactObj, boolean enableSynchronization) throws FinderException, PersisterException, SynchronizerException {
        if (intactObj.areXrefsInitialized()){
            List<Xref> xrefsToPersist = new ArrayList<Xref>(intactObj.getXrefs());
            intactObj.getXrefs().clear();
            int index = 0;
            try{
                for (Xref xref : xrefsToPersist){
                    // do not persist or merge xrefs because of cascades
                    Xref objRef = enableSynchronization ?
                            getContext().getExperimentalResultingSequenceXrefSynchronizer().synchronize(xref, false) :
                            getContext().getExperimentalResultingSequenceXrefSynchronizer().convertToPersistentObject(xref);
                    // we have a different instance because needed to be synchronized
                    if (objRef != null && !intactObj.getXrefs().contains(objRef)){
                        intactObj.getXrefs().add(objRef);
                    }
                    index++;
                }
            }
            finally {
                // always add previous properties in case of exception
                if (index < xrefsToPersist.size() - 1){
                    for (int i = index; i < xrefsToPersist.size(); i++){
                        intactObj.getXrefs().add(xrefsToPersist.get(i));
                    }
                }
            }
        }
    }
}
