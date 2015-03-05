/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.jami.model;

import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Represents a sequence chunk of a polymer.
 *
 * @deprecated in the future, the polymer will have a proper sequence column and we would not need the SequenceChunk class anymore
 *
 * @author marine Dumousseau
 */
@Entity
@Table( name = "ia_sequence_chunk" )
@Deprecated
public class SequenceChunk extends AbstractIntactPrimaryObject {

    ///////////////////////////////////////
    //attributes

    /**
     * The content of the sequence chunk.
     */
    private String sequenceChunk;

    /**
     * Chunk order.
     */
    private int sequenceIndex;

    ///////////////////////////////////////
    // constructors
    public SequenceChunk() {
    }

    public SequenceChunk(int aSequenceIndex, String aSequenceChunk) {
        this.sequenceIndex = aSequenceIndex;
        this.sequenceChunk = aSequenceChunk;
    }

    ///////////////////////////////////////
    // associations

    ///////////////////////////////////////
    //access methods for attributes

    @Column( name = "sequence_chunk", length = IntactUtils.MAX_SEQ_LENGTH_PER_CHUNK, nullable = false)
    @NotNull
    @Size(max = IntactUtils.MAX_SEQ_LENGTH_PER_CHUNK)
    public String getSequenceChunk() {
        return sequenceChunk;
    }

    public void setSequenceChunk( String sequenceChunk ) {
        this.sequenceChunk = sequenceChunk;
    }

    @Column( name = "sequence_index" )
    public int getSequenceIndex() {
        return sequenceIndex;
    }

    public void setSequenceIndex( int sequenceIndex ) {
        this.sequenceIndex = sequenceIndex;
    }

} // end Xref




