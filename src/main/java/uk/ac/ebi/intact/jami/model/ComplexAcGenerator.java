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
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.context.IntactContext;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * Generates AC for IntAct objects
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 */
public class ComplexAcGenerator extends SequenceStyleGenerator {

    private static final Log log = LogFactory.getLog(ComplexAcGenerator.class);
    private String sequenceCallSyntax;
    // To avoid the use of the schema in the sequence name a public synonym was added after created the sequence
    // INTACT_AC sequence works in the same way
    // grant select on INTACT.COMPLEX_AC to REPLACE_SCHEMA_USER;
    // create public synonym COMPLEX_AC for INTACT.COMPLEX_AC;
    // grant select,delete,insert,update on IA_COMPLEX_AC to REPLACE_SCHEMA_USER;
    // create public synonym IA_COMPLEX_AC for INTACT.IA_COMPLEX_AC;
    public static final String COMPLEX_AC_SEQUENCE_NAME = "complex_ac";

    @Override
    public void configure(Type type, Properties properties, ServiceRegistry serviceRegistry) throws MappingException {
        final JdbcEnvironment jdbcEnvironment = serviceRegistry.getService(JdbcEnvironment.class);
        final Dialect dialect = jdbcEnvironment.getDialect();

        //String defaultSeqValue = "hibernate_sequence";
        String sequenceName = ConfigurationHelper.getString(SEQUENCE_PARAM, properties, DEF_SEQUENCE_NAME);

        // use "intact_ac" only if the default sequence name is provided
        if (sequenceName.equals(DEF_SEQUENCE_NAME)) {
            sequenceName = COMPLEX_AC_SEQUENCE_NAME;
            properties.put(SEQUENCE_PARAM, sequenceName);
        }

        final String sequencePerEntitySuffix = ConfigurationHelper.getString(CONFIG_SEQUENCE_PER_ENTITY_SUFFIX, properties, DEF_SEQUENCE_SUFFIX);

        final String defaultSequenceName = ConfigurationHelper.getBoolean(CONFIG_PREFER_SEQUENCE_PER_ENTITY, properties, false)
                ? properties.getProperty(JPA_ENTITY_NAME) + sequencePerEntitySuffix
                : DEF_SEQUENCE_NAME;

        sequenceCallSyntax = dialect.getSequenceNextValString(ConfigurationHelper.getString(SEQUENCE_PARAM, properties, defaultSequenceName));
        super.configure(type, properties, serviceRegistry);
    }

    @Override
    public Serializable generate(SessionImplementor sessionImplementor, Object object) throws HibernateException {
         //TODO... Change the logic
        String prefix = "CPX";
        String stringId = null;
        IntactContext intactContext = ApplicationContextProvider.getBean("intactJamiContext");
        if (intactContext != null) {
            prefix = intactContext.getIntactConfiguration().getComplexAcPrefix();
        }

        Connection connection = sessionImplementor.connection();
        try {
            PreparedStatement ps = connection
                    .prepareStatement(sequenceCallSyntax);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long id = rs.getLong(1);
                stringId = prefix + "-" + id;
            }
            log.trace("Assigning Complex Ac: " + stringId);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return stringId;
    }


}
