/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.jami.model;

import com.sun.corba.se.spi.ior.Identifiable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
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
public class IntactAcGenerator implements IdentifierGenerator, Configurable {

    private static final Log log = LogFactory.getLog(IntactAcGenerator.class);

    private String sequencePrefix;
    private String sequenceCallSyntax;
    public static final String SEQUENCE_PREFIX = "sequence_prefix";

    /*    @Override
        public void configure( Type type, Properties properties, ServiceRegistry serviceRegistry ) throws MappingException {
            String defaultSeqValue = "hibernate_sequence";
            String sequenceName = ConfigurationHelper.getString(SEQUENCE, properties, defaultSeqValue);

            // use "intact_ac" only if the default sequence name is provided
            if ( sequenceName.equals( defaultSeqValue ) ) {
                sequenceName = INTACT_AC_SEQUENCE_NAME;
                properties.put( SEQUENCE, sequenceName );
            }
            super.configure( type, properties, serviceRegistry );
        }*/
    @Override
    public void configure(
            Type type, Properties params, ServiceRegistry serviceRegistry)
            throws MappingException {
        final JdbcEnvironment jdbcEnvironment =
                serviceRegistry.getService(JdbcEnvironment.class);
        final Dialect dialect = jdbcEnvironment.getDialect();

        sequencePrefix = ConfigurationHelper.getString(
                SEQUENCE_PREFIX,
                params,
                "SEQ_");

        final String sequencePerEntitySuffix = ConfigurationHelper.getString(
                SequenceStyleGenerator.CONFIG_SEQUENCE_PER_ENTITY_SUFFIX,
                params,
                SequenceStyleGenerator.DEF_SEQUENCE_SUFFIX);

        final String defaultSequenceName = ConfigurationHelper.getBoolean(
                SequenceStyleGenerator.CONFIG_PREFER_SEQUENCE_PER_ENTITY,
                params,
                false)
                ? params.getProperty(JPA_ENTITY_NAME) + sequencePerEntitySuffix
                : SequenceStyleGenerator.DEF_SEQUENCE_NAME;

        sequenceCallSyntax = dialect.getSequenceNextValString(
                ConfigurationHelper.getString(
                        SequenceStyleGenerator.SEQUENCE_PARAM,
                        params,
                        defaultSequenceName));


    }


    /**
     * The ID is the concatenation of the prefix and a sequence provided by the database, separated
     * by a dash.
     *
     * @param sessionImplementor a hibernate session implementor
     * @param object             the object being persisted
     * @return the new generated ID
     * @throws org.hibernate.HibernateException if something goes wrong
     */
    /* @Override
   public Serializable generate( SessionImplementor sessionImplementor, Object object ) throws HibernateException {

        String prefix="UNK";
        IntactContext intactContext = ApplicationContextProvider.getBean("intactJamiContext");
        if (intactContext != null) {
            prefix = intactContext.getIntactConfiguration().getAcPrefix();
        }
        String id = prefix + "-" + super.generate( sessionImplementor, object );

        log.trace( "Assigning Id: " + id );

        return id;
    }*/
    @Override
    public Serializable generate(SessionImplementor sessionImplementor, Object object) throws HibernateException {
        if (object instanceof Identifiable) {
            Identifiable identifiable = (Identifiable) object;
            Serializable id = identifiable.getId();
            if (id != null) {
                return id;
            }
        }
        String prefix = "UNK";
        IntactContext intactContext = ApplicationContextProvider.getBean("intactJamiContext");
        if (intactContext != null) {
            prefix = intactContext.getIntactConfiguration().getAcPrefix();
        }

        long seqValue = ((Number) Session.class.cast(sessionImplementor)
                .createSQLQuery(sequenceCallSyntax)
                .uniqueResult()).longValue();

        return prefix + "-" + seqValue;
    }


}
