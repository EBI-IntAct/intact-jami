/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.jami.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.id.IntegralDataTypeHolder;
import org.hibernate.id.SequenceGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.type.Type;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.context.IntactContext;

import java.io.Serializable;
import java.util.Properties;

/**
 * Generates AC for IntAct objects
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 */
public class ComplexAcGenerator extends SequenceGenerator {

    private static final Log log = LogFactory.getLog( ComplexAcGenerator.class );

    /**
     * The sequence parameter
     */
    public static final String SEQUENCE = "sequence";
    // To avoid the use of the schema in the sequence name a public synonim was added after created the sequence
    // INTACT_AC sequence works in the same way
    // grant select on INTACT.COMPLEX_AC to SCHEMA_USER;
    // create public synonym COMPLEX_AC for INTACT.COMPLEX_AC;
    // grant select,delete,insert,update on IA_COMPLEX_AC to SCHEMA_USER;
    // create public synonym IA_COMPLEX_AC for INTACT.IA_COMPLEX_AC;
    public static final String COMPLEX_AC_SEQUENCE_NAME = "complex_ac";

    /**
     * @HACK if we don't override this method with the specific Long type, the generator cannot be initialized.
     * @return
     */
    @Override
    protected IntegralDataTypeHolder buildHolder() {
        return IdentifierGeneratorHelper.getIntegralDataTypeHolder(Long.class);
    }

    @Override
    public void configure( Type type, Properties properties, Dialect dialect ) throws MappingException {
        String defaultSeqValue = "hibernate_sequence";
        String sequenceName = ConfigurationHelper.getString(SEQUENCE, properties, defaultSeqValue);

        // use "intact_ac" only if the default sequence name is provided
        if ( sequenceName.equals( defaultSeqValue ) ) {
            sequenceName = COMPLEX_AC_SEQUENCE_NAME;
            properties.put( SEQUENCE, sequenceName );
        }
        super.configure( type, properties, dialect );
    }

    /**
     * The ID is the concatenation of the prefix and a sequence provided by the database, separated
     * by a dash.
     *
     * @param sessionImplementor a hibernate session implementor
     * @param object             the object being persisted
     *
     * @return the new generated ID
     *
     * @throws HibernateException if something goes wrong
     */
    @Override
    public Serializable generate( SessionImplementor sessionImplementor, Object object ) throws HibernateException {
        String prefix="CPX";
        IntactContext intactContext = ApplicationContextProvider.getBean("intactJamiContext");
        if (intactContext != null) {
            prefix = intactContext.getIntactConfiguration().getComplexAcPrefix();
        }
        //We add the default version
        String id = prefix + "-" + super.generate( sessionImplementor, object );

        log.trace( "Assigning Complex Ac: " + id );

        return id;
    }


    @Override
    public String getSequenceName() {
        return COMPLEX_AC_SEQUENCE_NAME;
    }
}
