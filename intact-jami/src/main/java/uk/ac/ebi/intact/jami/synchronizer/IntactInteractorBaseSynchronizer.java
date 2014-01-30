package uk.ac.ebi.intact.jami.synchronizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.fetcher.InteractorFetcher;
import psidev.psi.mi.jami.factory.InteractorFactory;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.clone.CvTermCloner;
import psidev.psi.mi.jami.utils.clone.InteractorCloner;
import psidev.psi.mi.jami.utils.comparator.interactor.UnambiguousExactInteractorBaseComparator;
import psidev.psi.mi.jami.utils.comparator.interactor.UnambiguousExactInteractorComparator;
import uk.ac.ebi.intact.jami.merger.IntactInteractorBaseMergerEnrichOnly;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Synchronizer for interactors
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/14</pre>
 */

public class IntactInteractorBaseSynchronizer<T extends Interactor, I extends IntactInteractor> extends AbstractIntactDbSynchronizer<T, I>
implements InteractorFetcher<T>{
    private Map<I, I> persistedObjects;

    private IntactDbSynchronizer<Alias, InteractorAlias> aliasSynchronizer;
    private IntactDbSynchronizer<Annotation, InteractorAnnotation> annotationSynchronizer;
    private IntactDbSynchronizer<Xref, InteractorXref> xrefSynchronizer;
    private IntactDbSynchronizer<Checksum, InteractorChecksum> checksumSynchronizer;

    private IntactDbSynchronizer<Organism, IntactOrganism> organismSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> interactorTypeSynchronizer;

    private static final Log log = LogFactory.getLog(IntactInteractorBaseSynchronizer.class);

    public IntactInteractorBaseSynchronizer(EntityManager entityManager, Class<I> intactClass){
        super(entityManager, intactClass);
        // to keep track of persisted cvs
        initialisePersistedObjectMap();
        this.aliasSynchronizer = new IntactAliasSynchronizer(entityManager, InteractorAlias.class);
        this.annotationSynchronizer = new IntactAnnotationsSynchronizer(entityManager, InteractorAnnotation.class);
        this.xrefSynchronizer = new IntactXrefSynchronizer(entityManager, InteractorXref.class);

        this.organismSynchronizer = new IntactOrganismSynchronizer(entityManager);
        this.interactorTypeSynchronizer = new IntactCvTermSynchronizer(entityManager, IntactUtils.INTERACTOR_TYPE_OBJCLASS);
        this.checksumSynchronizer = new IntactChecksumSynchronizer<InteractorChecksum>(entityManager, InteractorChecksum.class);
    }

    public IntactInteractorBaseSynchronizer(EntityManager entityManager, Class<I> intactClass,
                                        IntactDbSynchronizer<Alias, InteractorAlias> aliasSynchronizer,
                                    IntactDbSynchronizer<Annotation, InteractorAnnotation> annotationSynchronizer, IntactDbSynchronizer<Xref, InteractorXref> xrefSynchronizer,
                                    IntactDbSynchronizer<Organism, IntactOrganism> organismSynchronizer, IntactDbSynchronizer<CvTerm, IntactCvTerm> typeSynchronizer,
                                    IntactDbSynchronizer<Checksum, InteractorChecksum> checksumSynchronizer){
        super(entityManager, intactClass);
        // to keep track of persisted cvs
        initialisePersistedObjectMap();
        this.aliasSynchronizer = aliasSynchronizer != null ? aliasSynchronizer : new IntactAliasSynchronizer(entityManager, InteractorAlias.class);
        this.annotationSynchronizer = annotationSynchronizer != null ? annotationSynchronizer : new IntactAnnotationsSynchronizer(entityManager, InteractorAnnotation.class);
        this.xrefSynchronizer = xrefSynchronizer != null ? xrefSynchronizer : new IntactXrefSynchronizer(entityManager, InteractorXref.class);

        this.organismSynchronizer = organismSynchronizer != null ? organismSynchronizer : new IntactOrganismSynchronizer(entityManager);
        this.interactorTypeSynchronizer = typeSynchronizer != null ? typeSynchronizer : new IntactCvTermSynchronizer(entityManager, IntactUtils.INTERACTOR_TYPE_OBJCLASS);
        this.checksumSynchronizer = checksumSynchronizer != null ? checksumSynchronizer : new IntactChecksumSynchronizer<InteractorChecksum>(entityManager, InteractorChecksum.class);
    }

    public I find(T term) throws FinderException{
        Query query;
        if (term == null){
            return null;
        }
        else if (this.persistedObjects.containsKey(term)){
            return this.persistedObjects.get(term);
        }
        else{
            IntactCvTerm existingType = this.interactorTypeSynchronizer.find(term.getInteractorType());
            // could not retrieve the interactor type so this interactor does not exist in IntAct
            if (existingType == null){
                return null;
            }
            IntactOrganism existingOrganism = null;
            if (term.getOrganism() != null){
                existingOrganism = this.organismSynchronizer.find(term.getOrganism());
                // could not retrieve the organism so this interactor does not exist in IntAct
                if (existingOrganism == null){
                    return null;
                }
            }

            // try to fetch interactor using identifiers
            Collection<I> results = findByIdentifier(term, existingOrganism, existingType);
            if (results.isEmpty()){
                // fetch using other properties
                results = findByOtherProperties(term, existingType, existingOrganism);
                if (results.isEmpty()){
                    // fetch using shortname
                    query = findByName(term, existingType, existingOrganism);
                    results = query.getResultList();
                }
            }

            I retrievedInstance = postFilter(term, results);
            if (retrievedInstance != null){
                return retrievedInstance;
            }
            else if (results.size() > 1){
                throw new FinderException("The interactor "+term + " can match "+results.size()+" interactors in the database and we cannot determine which one is valid.");
            }
            return retrievedInstance;
        }
    }

    // nothing to do here
    protected I postFilter(T term, Collection<I> results) {
        if (results.size() == 1){
            return results.iterator().next();
        }
        return null;
    }

    // nothing to do here
    protected Collection<I> findByOtherProperties(T term, IntactCvTerm existingType, IntactOrganism existingOrganism) {
        return Collections.EMPTY_LIST;
    }

    protected Query findByName(T term, IntactCvTerm existingType, IntactOrganism existingOrganism) {
        Query query;
        if (existingOrganism == null){
            query = getEntityManager().createQuery("select i from "+getIntactClass()+" i " +
                    "join i.interactorType as t " +
                    "where i.shortName = :name " +
                    "and i.organism is null " +
                    "and t.ac = :typeAc");
            query.setParameter("name", term.getShortName().trim().toLowerCase());
            query.setParameter("typeAc", existingType.getAc());
        }
        else{
            query = getEntityManager().createQuery("select i from "+getIntactClass()+" i " +
                    "join i.interactorType as t " +
                    "join i.organism as o " +
                    "where i.shortName = :name " +
                    "and o.ac = :orgAc " +
                    "and t.ac = :typeAc");
            query.setParameter("name", term.getShortName().trim().toLowerCase());
            query.setParameter("orgAc", existingOrganism.getAc());
            query.setParameter("typeAc", existingType.getAc());
        }
        return query;
    }

    public I persist(I object) throws FinderException, PersisterException, SynchronizerException{
        // only persist if not already done
        if (!this.persistedObjects.containsKey(object)){
            return this.persistedObjects.get(object);
        }

        this.persistedObjects.put(object, object);

        return super.persist(object);
    }

    public void synchronizeProperties(I intactInteractor) throws FinderException, PersisterException, SynchronizerException {
        // then check shortlabel/synchronize
        prepareAndSynchronizeShortLabel(intactInteractor);
        // then check full name
        prepareFullName(intactInteractor);
        // then check organism
        if (intactInteractor.getOrganism() != null){
            intactInteractor.setOrganism(this.organismSynchronizer.synchronize(intactInteractor.getOrganism(), true));
        }
        // then check interactor type
        intactInteractor.setInteractorType(this.interactorTypeSynchronizer.synchronize(intactInteractor.getInteractorType(), true));
        // then check checksums
        prepareChecksums(intactInteractor);
        // then check aliases
        prepareAliases(intactInteractor);
        // then check annotations
        prepareAnnotations(intactInteractor);
        // then check xrefs
        prepareXrefs(intactInteractor);
    }

    public void clearCache() {
        this.persistedObjects.clear();
        this.aliasSynchronizer.clearCache();
        this.xrefSynchronizer.clearCache();
        this.annotationSynchronizer.clearCache();
        this.organismSynchronizer.clearCache();
        this.interactorTypeSynchronizer.clearCache();
    }

    protected void initialisePersistedObjectMap() {
        this.persistedObjects = new TreeMap<I, I>(new UnambiguousExactInteractorBaseComparator());
    }

    protected void setPersistedObjects(Map<I, I> persistedObjects) {
        this.persistedObjects = persistedObjects;
    }

    @Override
    protected Object extractIdentifier(I object) {
        return object.getAc();
    }

    @Override
    protected I instantiateNewPersistentInstance(T object, Class<? extends I> intactClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        I newInteractor = intactClass.getConstructor(String.class).newInstance(object.getShortName());
        InteractorCloner.copyAndOverrideBasicInteractorProperties(object, newInteractor);
        return newInteractor;
    }

    protected void prepareChecksums(I intactInteractor) throws FinderException, PersisterException, SynchronizerException {
        if (intactInteractor.areChecksumsInitialized()){
            List<Checksum> checksumToPersist = new ArrayList<Checksum>(intactInteractor.getPersistentChecksums());
            for (Checksum checksum : checksumToPersist){
                // do not persist or merge checksum because of cascades
                Checksum interactorCheck = this.checksumSynchronizer.synchronize(checksum, false);
                // we have a different instance because needed to be synchronized
                if (interactorCheck != checksum){
                    intactInteractor.getPersistentChecksums().remove(checksum);
                    intactInteractor.getPersistentChecksums().add(interactorCheck);
                }
            }
        }
    }


    protected void prepareXrefs(I intactInteractor) throws FinderException, PersisterException, SynchronizerException {
        if (intactInteractor.areXrefsInitialized()){
            List<Xref> xrefsToPersist = new ArrayList<Xref>(intactInteractor.getPersistentXrefs());
            for (Xref xref : xrefsToPersist){
                // do not persist or merge xrefs because of cascades
                Xref cvXref = this.xrefSynchronizer.synchronize(xref, false);
                // we have a different instance because needed to be synchronized
                if (cvXref != xref){
                    intactInteractor.getPersistentXrefs().remove(xref);
                    intactInteractor.getPersistentXrefs().add(cvXref);
                }
            }
        }
    }

    protected void prepareAnnotations(I intactInteractor) throws FinderException, PersisterException, SynchronizerException {
        if (intactInteractor.areAnnotationsInitialized()){
            List<Annotation> annotationsToPersist = new ArrayList<Annotation>(intactInteractor.getPersistentAnnotations());
            for (Annotation annotation : annotationsToPersist){
                // do not persist or merge annotations because of cascades
                Annotation cvAnnotation = this.annotationSynchronizer.synchronize(annotation, false);
                // we have a different instance because needed to be synchronized
                if (cvAnnotation != annotation){
                    intactInteractor.getPersistentAnnotations().remove(annotation);
                    intactInteractor.getPersistentAnnotations().add(cvAnnotation);
                }
            }
        }
    }

    protected void prepareAliases(I intactInteractor) throws FinderException, PersisterException, SynchronizerException {
        if (intactInteractor.areAliasesInitialized()){
            List<Alias> aliasesToPersist = new ArrayList<Alias>(intactInteractor.getPersistentAliases());
            for (Alias alias : aliasesToPersist){
                // do not persist or merge alias because of cascades
                Alias cvAlias = this.aliasSynchronizer.synchronize(alias, false);
                // we have a different instance because needed to be synchronized
                if (cvAlias != alias){
                    intactInteractor.getPersistentAliases().remove(alias);
                    intactInteractor.getPersistentAliases().add(cvAlias);
                }
            }
        }
    }

    protected void prepareFullName(I intactInteractor) {
        // truncate if necessary
        if (intactInteractor.getFullName() != null && IntactUtils.MAX_FULL_NAME_LEN < intactInteractor.getFullName().length()){
            intactInteractor.setFullName(intactInteractor.getFullName().substring(0, IntactUtils.MAX_FULL_NAME_LEN));
        }
    }

    protected void prepareAndSynchronizeShortLabel(I intactInteractor) {
        // truncate if necessary
        if (IntactUtils.MAX_SHORT_LABEL_LEN < intactInteractor.getShortName().length()){
            log.warn("Interactor shortLabel too long: "+intactInteractor.getShortName()+", will be truncated to "+ IntactUtils.MAX_SHORT_LABEL_LEN+" characters.");
            intactInteractor.setShortName(intactInteractor.getShortName().substring(0, IntactUtils.MAX_SHORT_LABEL_LEN));
        }
        String name = intactInteractor.getShortName().trim().toLowerCase();
        List<String> existingInteractors = Collections.EMPTY_LIST;
        boolean first = true;
        do{
            if (first){
                first = false;
            }
            else if (name.length() > 2){
                name = name.substring(0, name.length() - 1);
            }
            // check if short name already exist, if yes, synchronize with existing label
            Query query = getEntityManager().createQuery("select i.shortName from IntactInteractor i " +
                    "where i.shortName = :name or i.shortName like :nameWithSuffix"
                    + (intactInteractor.getAc() != null ? "and i.ac <> :interactorAc" : ""));
            query.setParameter("name", name);
            query.setParameter("nameWithSuffix", name+"-%");
            if (intactInteractor.getAc() != null){
                query.setParameter("interactorAc", intactInteractor.getAc());
            }
            existingInteractors = query.getResultList();
            String nameInSync = IntactUtils.synchronizeShortlabel(intactInteractor.getShortName(), existingInteractors, IntactUtils.MAX_SHORT_LABEL_LEN);
            intactInteractor.setShortName(nameInSync);
        }
        while(name.length() > 1 && !existingInteractors.isEmpty());
    }

    protected Collection<I> findByIdentifier(T term, IntactOrganism existingOrganism, IntactCvTerm existingType) throws FinderException {
        if (term.getIdentifiers().isEmpty()){
             return null;
        }
        Query query=null;
        Collection<I> totalInteractors = new ArrayList<I>();
        // no organism for this interactor.
        if (existingOrganism == null){
            for (Xref ref : term.getIdentifiers()){
                query = getEntityManager().createQuery("select i from "+getIntactClass()+" i " +
                        "join i.interactorType as t " +
                        "where i.ac = :id " +
                        "and i.organism is null " +
                        "and t.ac = :typeAc");
                query.setParameter("id", ref.getId());
                query.setParameter("typeAc", existingType.getAc());
                Collection<I> interactors = query.getResultList();
                if (!interactors.isEmpty()){
                    return interactors;
                }
                else{
                    query = getEntityManager().createQuery("select i from "+getIntactClass()+" i " +
                            "join i.persistentXrefs as x " +
                            "join x.database as d " +
                            "join x.qualifier as q " +
                            "join i.interactorType as t " +
                            "where (q.shortName = :identity or q.shortName = :secondaryAc)" +
                            "and d.shortName = :db " +
                            "and x.id = :id " +
                            "and t.ac = :typeAc " +
                            "and i.organism is null");
                    query.setParameter("identity", Xref.IDENTITY);
                    query.setParameter("secondaryAc", Xref.SECONDARY);
                    query.setParameter("db", ref.getDatabase().getShortName());
                    query.setParameter("id", ref.getId());
                    query.setParameter("typeAc", existingType.getAc());

                    interactors = query.getResultList();
                    if (interactors.size() == 1){
                        return interactors;
                    }
                    else if (interactors.size() > 1){
                        totalInteractors.addAll(interactors);
                    }
                }
            }
        }
        // organism for this interactor
        else{
            for (Xref ref : term.getIdentifiers()){
                query = getEntityManager().createQuery("select i from "+getIntactClass()+" i " +
                        "join i.organism as o " +
                        "join i.interactorType as t " +
                        "where i.ac = :id " +
                        "and t.ac = :typeAc " +
                        "and o.ac = :orgAc");
                query.setParameter("id", ref.getId());
                query.setParameter("typeAc", existingType.getAc());
                query.setParameter("orgAc", existingOrganism.getAc());
                Collection<I> interactors = query.getResultList();
                if (!interactors.isEmpty()){
                    return interactors;
                }
                else{
                    query = getEntityManager().createQuery("select i from "+getIntactClass()+" i " +
                            "join i.persistentXrefs as x " +
                            "join x.database as d " +
                            "join x.qualifier as q " +
                            "join i.organism as o " +
                            "join i.interactorType as t " +
                            "where (q.shortName = :identity or q.shortName = :secondaryAc)" +
                            "and d.shortName = :db " +
                            "and x.id = :id " +
                            "and t.ac = :typeAc " +
                            "and o.ac = :orgAc");
                    query.setParameter("identity", Xref.IDENTITY);
                    query.setParameter("secondaryAc", Xref.SECONDARY);
                    query.setParameter("db", ref.getDatabase().getShortName());
                    query.setParameter("id", ref.getId());
                    query.setParameter("typeAc", existingType.getAc());
                    query.setParameter("orgAc", existingOrganism.getAc());

                    interactors = query.getResultList();
                    if (interactors.size() == 1){
                        return interactors;
                    }
                    else if (interactors.size() > 1){
                        totalInteractors.addAll(interactors);
                    }
                }
            }
        }

        return totalInteractors;
    }

    public Collection<T> fetchByIdentifier(String identifier) throws BridgeFailedException {
        if (identifier == null){
            throw new IllegalArgumentException("The identifier cannot be null.");
        }
        Query query = getEntityManager().createQuery("select i from "+getIntactClass()+" i " +
                "where i.ac = :id");
        query.setParameter("id", identifier);
        Collection<T> interactors = query.getResultList();
        if (!interactors.isEmpty()){
            return interactors;
        }
        else{
            query = getEntityManager().createQuery("select i from "+getIntactClass()+" i " +
                    "join i.persistentXrefs as x " +
                    "join x.qualifier as q " +
                    "where (q.shortName = :identity or q.shortName = :secondaryAc)" +
                    "and x.id = :id");
            query.setParameter("identity", Xref.IDENTITY);
            query.setParameter("secondaryAc", Xref.SECONDARY);
            query.setParameter("id", identifier);
            return query.getResultList();
        }
    }

    public Collection<T> fetchByIdentifiers(Collection<String> identifiers) throws BridgeFailedException {
        if (identifiers == null){
            throw new IllegalArgumentException("The identifiers cannot be null.");
        }

        Collection<T> results = new ArrayList<T>(identifiers.size());
        for (String id : identifiers){
            Collection<T> element = fetchByIdentifier(id);
            if (element != null){
                results.addAll(element);
            }
        }
        return results;
    }

    @Override
    protected void initialiseDefaultMerger() {
        super.setIntactMerger(new IntactInteractorBaseMergerEnrichOnly<T,I>(this));
    }
}
