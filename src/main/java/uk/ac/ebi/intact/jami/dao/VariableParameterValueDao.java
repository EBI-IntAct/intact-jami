/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.jami.dao;

import uk.ac.ebi.intact.jami.model.extension.IntactVariableParameterValue;

import java.util.Collection;

/**
 * DAO for variable parameter value
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 */
public interface VariableParameterValueDao extends IntactBaseDao<IntactVariableParameterValue> {

    public Collection<IntactVariableParameterValue> getByParameterAc(String parentAc);
}
