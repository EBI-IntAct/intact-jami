package uk.ac.ebi.intact.jami.context;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.relational.AuxiliaryDatabaseObject;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;
import uk.ac.ebi.intact.jami.model.ComplexAcGenerator;
import uk.ac.ebi.intact.jami.model.IntactAcGenerator;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.jami.sequence.SequenceAuxiliaryDatabaseObject;

import java.util.List;
import java.util.Properties;

@Component
public class IntactJamiPersistenceProvider extends HibernatePersistenceProvider {

    public MetadataBuilder getBasicMetaDataBuilder(String dialect) {
        StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();

        Properties properties = new Properties();
        if (dialect != null && !dialect.isBlank()) {
            registryBuilder.applySetting(Environment.DIALECT, dialect);
            properties.setProperty(Environment.DIALECT, dialect);
        }

        MetadataSources metadata = new MetadataSources(registryBuilder.build());
        HibernateConfig basicConfiguration = getBasicConfiguration(properties);
        basicConfiguration.getEntityClasses().forEach(metadata::addAnnotatedClass); // Add package classes
        return configure(metadata.getMetadataBuilder()); // Add custom sequences
    }

    public HibernateConfig getBasicConfiguration(Properties props) {
        if (props == null) props = new Properties();
        final LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        final IntactHibernateJpaVendorAdapter jpaVendorAdapter = new IntactHibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabasePlatform(Dialect.getDialect(props).getClass().getName());
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.afterPropertiesSet();

        factoryBean.getNativeEntityManagerFactory().close();

        HibernateConfig config = new HibernateConfig();
        return config.scanPackages(IntactPrimaryObject.class.getPackageName());
    }

    private final static List<AuxiliaryDatabaseObject> auxiliaryDatabaseObjects = List.of(
            new SequenceAuxiliaryDatabaseObject(IntactAcGenerator.INTACT_AC_SEQUENCE_NAME, 1),
            new SequenceAuxiliaryDatabaseObject(ComplexAcGenerator.COMPLEX_AC_SEQUENCE_NAME, 1)
    );

    public MetadataBuilder configure(MetadataBuilder configuration) {
        auxiliaryDatabaseObjects.forEach(configuration::applyAuxiliaryDatabaseObject);
        return configuration;
    }
}
