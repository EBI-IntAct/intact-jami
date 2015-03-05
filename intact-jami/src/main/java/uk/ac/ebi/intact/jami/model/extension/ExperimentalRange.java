package uk.ac.ebi.intact.jami.model.extension;

import org.hibernate.annotations.Target;
import psidev.psi.mi.jami.model.*;

import javax.persistence.*;
import javax.persistence.Entity;

/**
 * Intact implementation of range attached to feature evidences
 * NOTE: when modelled ranges and experimental ranges are in different tables, the where clause attached to this entity can be removed
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/01/14</pre>
 */
@Entity
@Table(name = "ia_range")
public class ExperimentalRange extends AbstractIntactRange<psidev.psi.mi.jami.model.Entity> {

    protected ExperimentalRange() {
        super();
    }

    public ExperimentalRange(Position start, Position end) {
        super(start, end);
    }

    public ExperimentalRange(Position start, Position end, boolean isLink) {
        super(start, end, isLink);
    }

    public ExperimentalRange(Position start, Position end, ResultingSequence resultingSequence) {
        super(start, end, resultingSequence);
    }

    public ExperimentalRange(Position start, Position end, ParticipantEvidence participant) {
        super(start, end, participant);
    }

    public ExperimentalRange(Position start, Position end, boolean isLink, ResultingSequence resultingSequence) {
        super(start, end, isLink, resultingSequence);
    }

    public ExperimentalRange(Position start, Position end, boolean isLink, ParticipantEvidence participant) {
        super(start, end, isLink, participant);
    }

    @Override
    @Embedded
    @Target(ExperimentalResultingSequence.class)
    public ResultingSequence getResultingSequence() {
        return super.getResultingSequence();
    }

    @ManyToOne(targetEntity = IntactModelledParticipant.class)
    @JoinColumn(name = "modelled_participant_ac", referencedColumnName = "ac")
    @Target(IntactModelledParticipant.class)
    protected ModelledEntity getModelledParticipant() {
        psidev.psi.mi.jami.model.Entity target = getParticipant();
        return target instanceof ModelledEntity ? (ModelledEntity)target : null;
    }

    private void setModelledParticipant(ModelledEntity participant) {
        if (participant != null){
            super.setParticipant(participant);
        }
    }

    @ManyToOne(targetEntity = IntactModelledParticipant.class)
    @JoinColumn(name = "experimental_participant_ac", referencedColumnName = "ac")
    @Target(IntactModelledParticipant.class)
    protected ExperimentalEntity getExperimentalParticipant() {
        psidev.psi.mi.jami.model.Entity target = getParticipant();
        return target instanceof ExperimentalEntity ? (ExperimentalEntity)target : null;
    }

    private void setExperimentalParticipant(ExperimentalEntity participant) {
        if (participant != null){
            super.setParticipant(participant);
        }
    }
}
