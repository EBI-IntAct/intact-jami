/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.jami.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.dialect.*;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.Target;
import uk.ac.ebi.intact.jami.context.IntactJamiPersistenceProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * IntAct schema utils, that contains methods to create/drop the database schema, create DDLs...
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 */
public class IntactSchemaUtils {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(IntactSchemaUtils.class);

    private static final IntactJamiPersistenceProvider persistenceProvider = new IntactJamiPersistenceProvider();

    private IntactSchemaUtils() {
    }

    /**
     * Generates the DDL schema
     * @param dialect the dialect to use (complete class name for the hibernate dialect object)
     * @return an array containing the SQL statements
     */
    public static String[] generateCreateSchemaDDL(String dialect) {
        return exportSchema(
                "create",
                new SchemaExport((MetadataImplementor) persistenceProvider.getBasicMetaDataBuilder(dialect).build()),
                Target.SCRIPT,
                SchemaExport.Type.CREATE);
    }

    public static String[] generateUpdateSchemaDDL(String dialect, Connection connection) throws SQLException {
        return exportSchema(
                "update",
                new SchemaExport((MetadataImplementor) persistenceProvider.getBasicMetaDataBuilder(dialect).build(), connection),
                Target.BOTH,
                SchemaExport.Type.BOTH);
    }

    public static String[] generateDropSchemaDDL(String dialect) {
        return exportSchema(
                "drop",
                new SchemaExport((MetadataImplementor) persistenceProvider.getBasicMetaDataBuilder(dialect).build()),
                Target.SCRIPT,
                SchemaExport.Type.DROP);
    }

    private static String[] exportSchema(String tempFileName, SchemaExport export, Target target, SchemaExport.Type exportType) {
        String[] sqls;
        try {
            File file = File.createTempFile(tempFileName, ".sql");
            export.setDelimiter(";")
                    .setOutputFile(file.getAbsolutePath())
                    .execute(target, exportType);
            try (Stream<String> lines = Files.lines(file.toPath())) {
                sqls = lines.toArray(String[]::new);
            }
            if (file.delete()) log.debug("Temp file deleted");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sqls;
    }

    /**
     * Generates the DDL schema for Oracle 9i.
     * @return an array containing the SQL statements
     */
    public static String[] generateCreateSchemaDDLForOracle() {
        return generateCreateSchemaDDL(Oracle10gDialect.class.getName());
    }

    /**
     * Generates the UPDATE DDL schema for Oracle .
     * @return an array containing the SQL statements
     */
    public static String[] generateUpdateSchemaDDLForOracle(Connection connection) throws SQLException {
        return generateUpdateSchemaDDL(Oracle10gDialect.class.getName(), connection);
    }

    /**
     * Generates the DDL schema for PostgreSQL.
     * @return an array containing the SQL statements
     */
    public static String[] generateCreateSchemaDDLForPostgreSQL() {
        return generateCreateSchemaDDL(PostgreSQL82Dialect.class.getName());
    }

    /**
     * Generates the UPDATE DDL schema for PostgreSQL .
     * @return an array containing the SQL statements
     */
    public static String[] generateUpdateSchemaDDLForPostgreSQL(Connection connection) throws SQLException {
        return generateUpdateSchemaDDL(PostgreSQL82Dialect.class.getName(), connection);
    }

    /**
     * Generates the DDL schema for HSQL DB.
     * @return an array containing the SQL statements
     */
    public static String[] generateCreateSchemaDDLForHSQL() {
        return generateCreateSchemaDDL(HSQLDialect.class.getName());
    }

    /**
     * Generates the UPDATE DDL schema for HSQL .
     * @return an array containing the SQL statements
     */
    public static String[] generateUpdateSchemaDDLForHSQL(Connection connection) throws SQLException {
        return generateUpdateSchemaDDL(HSQLDialect.class.getName(), connection);
    }


    /**
     * Generates the DDL schema for HSQL DB.
     * @return an array containing the SQL statements
     */
    public static String[] generateCreateSchemaDDLForH2() {
        return generateCreateSchemaDDL(H2Dialect.class.getName());
    }

    /**
     * Generates the UPDATE DDL schema for H2.
     * @return an array containing the SQL statements
     */
    public static String[] generateUpdateSchemaDDLForH2(Connection connection) throws SQLException {
        return generateUpdateSchemaDDL(H2Dialect.class.getName(), connection);
    }

    public static String[] getTableNames() {
        return persistenceProvider.getBasicMetaDataBuilder(Oracle10gDialect.class.getName()).build()
                .getEntityBindings()
                .stream()
                .map(persistentClass -> {
                    List<String> tableNames = new ArrayList<>();
                    if (!persistentClass.isAbstract()) {
                        tableNames.add(persistentClass.getTable().getName());
                    }
                    Iterator propertiesIterator = persistentClass.getPropertyIterator();
                    while (propertiesIterator.hasNext()) {
                        Property property = (Property) propertiesIterator.next();
                        if (property.getValue().getType().isCollectionType()) {
                            Table collectionTable = ((Collection) property.getValue()).getCollectionTable();
                            if (collectionTable != null) {
                                tableNames.add(collectionTable.getName());
                            }
                        }
                    }
                    return tableNames;
                })
                .flatMap(List::stream)
                .distinct()
                .toArray(String[]::new);
    }

    /**
     * Generates the DDL schema for Oracle
     * @return an array containing the SQL statements
     */
    public static String[] generateDropSchemaDDLForOracle() {
        return generateDropSchemaDDL(Oracle10gDialect.class.getName());
    }

    /**
     * Generates the DDL schema for PostgreSQL
     * @return an array containing the SQL statements
     */
    public static String[] generateDropSchemaDDLForPostgreSQL() {
        return generateDropSchemaDDL(PostgreSQL82Dialect.class.getName());
    }

    /**
     * Creates a schema and initialize the database
     */
    public static void createSchema() {
        createSchema(true);
    }

    /**
     * Creates a schema
     * @param initializeDatabase If false, do not initialize the database (e.g. don't create Institution)
     */
    public static void createSchema(boolean initializeDatabase) {
        if (log.isDebugEnabled()) log.debug("Creating schema");

        SchemaExport se = newSchemaExport();
        se.create(false, true);
    }

    protected static SchemaExport newSchemaExport() {
        MetadataSources metadata = new MetadataSources(new StandardServiceRegistryBuilder().build());
        return new SchemaExport((MetadataImplementor) persistenceProvider.configure(metadata.getMetadataBuilder()).build());
    }

    /**
     * Drops the current schema, emptying the database
     */
    public static void dropSchema() {
        if (log.isDebugEnabled()) log.debug("Dropping schema");

        SchemaExport se = newSchemaExport();
        se.drop(false, true);
    }

    /**
     * Drops and creates the schema, initializing intact. Beware that it commits transactions
     */
    public static void resetSchema() {
        resetSchema(true);
    }

    /**
     * Drops and creates the schema. Beware that it commits transactions
     * @param initializeDatabase If false, do not initialize the database (e.g. don't create Institution)
     */
    public static void resetSchema(boolean initializeDatabase) {
        if (log.isDebugEnabled()) log.debug("Resetting schema");

        dropSchema();
        createSchema(initializeDatabase);
    }
}