package uk.ac.ebi.intact.jami.synchronizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.fetcher.OrganismFetcher;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.jami.merger.OrganismMergerEnrichOnly;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.synchronizer.impl.AliasSynchronizerTemplate;
import uk.ac.ebi.intact.jami.synchronizer.impl.CvTermSynchronizer;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Default finder/synchronizer for organisms
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27/01/14</pre>
 */

public class IntactOrganismSynchronizer extends AbstractIntactDbSynchronizer<Organism, IntactOrganism> implements OrganismFetcher{
    private Map<Organism, IntactOrganism> persistedObjects;

    private IntactDbSynchronizer<Alias, OrganismAlias> aliasSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> cellTypeSynchronizer;
    private IntactDbSynchronizer<CvTerm, IntactCvTerm> tissueSynchronizer;

    private static final Log log = LogFactory.getLog(CvTermSynchronizer.class);

    public IntactOrganismSynchronizer(EntityManager entityManager){
        super(entityManager, IntactOrganism.class);
        // to keep track of persisted cvs
        this.persistedObjects = new HashMap<Organism, IntactOrganism>();
    }

    public IntactOrganism find(Organism term) throws FinderException {
        Query query;
        if (term == null){
            return null;
        }
        else if (this.persistedObjects.containsKey(term)){
            return this.persistedObjects.get(term);
        }
        // we have a simple organism. Only check its taxid
        else if (term.getCellType() == null && term.getTissue() == null){
            query = getEntityManager().createQuery("select o from IntactOrganism o " +
                    "where o.cellType is null " +
                    "and o.tissue is null " +
                    "and o.persistentTaxid = :taxid");
            query.setParameter("taxid", Integer.toString(term.getTaxId()));
        }
        // we have a celltype/tissue to find first
        else {
            if (term.getCellType() != null && term.getTissue() != null){
                CvTerm existingCell = getCellTypeSynchronizer().find(term.getCellType());
                CvTerm existingTissue = getTissueSynchronizer().find(term.getTissue());
                // cell line or tissue is not known so the biosource does not exist in IntAct
                if (existingCell == null || existingTissue == null){
                    return null;
                }
                else {
                    query = getEntityManager().createQuery("select o from IntactOrganism o " +
                            "join o.cellType as cell " +
                            "join o.tissue as t " +
                            "where cell.ac = :cellAc " +
                            "and t.ac = :tissueAc " +
                            "and o.persistentTaxid = :taxid");
                    query.setParameter("cellAc", ((IntactCvTerm)existingCell).getAc());
                    query.setParameter("tissueAc", ((IntactCvTerm)existingTissue).getAc());
                    query.setParameter("taxid", Integer.toString(term.getTaxId()));
                }
            }
            else if (term.getCellType() != null){
                CvTerm existingCell = getCellTypeSynchronizer().find(term.getCellType());
                // cell line or tissue is not known so the biosource does not exist in IntAct
                if (existingCell == null){
                    return null;
                }
                else {
                    query = getEntityManager().createQuery("select o from IntactOrganism o " +
                            "join o.cellType as cell " +
                            "where cell.ac = :cellAc " +
                            "and o.persistentTaxid = :taxid");
                    query.setParameter("cellAc", ((IntactCvTerm)existingCell).getAc());
                    query.setParameter("taxid", Integer.toString(term.getTaxId()));
                }
            }
            else{
                CvTerm existingTissue = getTissueSynchronizer().find(term.getTissue());
                // cell line or tissue is not known so the biosource does not exist in IntAct
                if (existingTissue == null){
                    return null;
                }
                else {
                    query = getEntityManager().createQuery("select o from IntactOrganism o " +
                            "join o.tissue as t " +
                            "where t.ac = :tissueAc " +
                            "and o.persistentTaxid = :taxid");
                    query.setParameter("tissueAc", ((IntactCvTerm)existingTissue).getAc());
                    query.setParameter("taxid", Integer.toString(term.getTaxId()));
                }
            }
        }
        Collection<IntactOrganism> organism = query.getResultList();
        if (organism.size() == 1){
            return organism.iterator().next();
        }
        else if (organism.size() > 1){
            throw new FinderException("The organism "+term + " can match "+organism.size()+" organisms in the database and we cannot determine which one is valid.");
        }
        return null;
    }

    public IntactOrganism persist(IntactOrganism object) throws FinderException, PersisterException, SynchronizerException {
        // only persist if not already done
        if (!this.persistedObjects.containsKey(object)){
            return this.persistedObjects.get(object);
        }

        IntactOrganism persisted = super.persist(object);
        this.persistedObjects.put(object, persisted);

        return persisted;
    }

    @Override
    public IntactOrganism synchronize(Organism object, boolean persist) throws FinderException, PersisterException, SynchronizerException {
        // only synchronize if not already done
        if (!this.persistedObjects.containsKey(object)){
            return this.persistedObjects.get(object);
        }

        IntactOrganism org = super.synchronize(object, persist);
        this.persistedObjects.put(object, org);
        return org;
    }

    public void synchronizeProperties(IntactOrganism intactOrganism) throws FinderException, PersisterException, SynchronizerException {
        // then check shortlabel/synchronize
        prepareAndSynchronizeCommonName(intactOrganism);
        // then check full name
        prepareFullName(intactOrganism);
        // then check aliases
        prepareAliases(intactOrganism);
        // then check annotations
        prepareCellTypeAndTissue(intactOrganism);
    }

    public void clearCache() {
        this.persistedObjects.clear();
        getAliasSynchronizer().clearCache();
        getCellTypeSynchronizer().clearCache();
        getTissueSynchronizer().clearCache();
    }

    public IntactOrganism fetchByTaxID(int taxID) throws BridgeFailedException {
        Query query = getEntityManager().createQuery("select o from IntactOrganism o " +
                "where o.cellType is null " +
                "and o.tissue is null " +
                "and o.persistentTaxid = :taxid");
        query.setParameter("taxid", Integer.toString(taxID));
        Collection<IntactOrganism> organism = query.getResultList();
        if (organism.size() == 1){
            return organism.iterator().next();
        }
        else if (organism.size() > 1){
            throw new BridgeFailedException("The organism "+taxID + " can match "+organism.size()+" organisms in the database and we cannot determine which one is valid.");
        }
        return null;
    }

    public Collection<Organism> fetchByTaxIDs(Collection<Integer> taxIDs) throws BridgeFailedException {
        if (taxIDs == null){
            throw new IllegalArgumentException("The taxids cannot be null.");
        }

        Collection<Organism> results = new ArrayList<Organism>(taxIDs.size());
        for (int id : taxIDs){
            Organism element = fetchByTaxID(id);
            if (element != null){
                results.add(element);
            }
        }
        return results;
    }

    public IntactDbSynchronizer<Alias, OrganismAlias> getAliasSynchronizer() {
        if (this.aliasSynchronizer == null){
            this.aliasSynchronizer = new AliasSynchronizerTemplate<OrganismAlias>(getEntityManager(), OrganismAlias.class);
        }
        return aliasSynchronizer;
    }

    public void setAliasSynchronizer(IntactDbSynchronizer<Alias, OrganismAlias> aliasSynchronizer) {
        this.aliasSynchronizer = aliasSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getCellTypeSynchronizer() {
        if (this.cellTypeSynchronizer == null){
            this.cellTypeSynchronizer = new CvTermSynchronizer(getEntityManager(), IntactUtils.CELL_TYPE_OBJCLASS);
        }
        return cellTypeSynchronizer;
    }

    public void setCellTypeSynchronizer(IntactDbSynchronizer<CvTerm, IntactCvTerm> cellTypeSynchronizer) {
        this.cellTypeSynchronizer = cellTypeSynchronizer;
    }

    public IntactDbSynchronizer<CvTerm, IntactCvTerm> getTissueSynchronizer() {
        if (this.tissueSynchronizer == null){
            this.tissueSynchronizer = new CvTermSynchronizer(getEntityManager(), IntactUtils.TISSUE_OBJCLASS);
        }
        return tissueSynchronizer;
    }

    public void setTissueSynchronizer(IntactDbSynchronizer<CvTerm, IntactCvTerm> tissueSynchronizer) {
        this.tissueSynchronizer = tissueSynchronizer;
    }

    protected void prepareCellTypeAndTissue(IntactOrganism intactOrganism) throws FinderException, PersisterException, SynchronizerException {
        if (intactOrganism.getCellType() != null){
            intactOrganism.setCellType(getCellTypeSynchronizer().synchronize(intactOrganism.getCellType(), true));
        }
        if (intactOrganism.getTissue() != null){
            intactOrganism.setTissue(getTissueSynchronizer().synchronize(intactOrganism.getTissue(), true));
        }
    }

    protected void prepareAliases(IntactOrganism intactOrganism) throws FinderException, PersisterException, SynchronizerException {
        if (intactOrganism.areAliasesInitialized()){
            List<Alias> aliasesToPersist = new ArrayList<Alias>(intactOrganism.getAliases());
            for (Alias alias : aliasesToPersist){
                // do not persist or merge alias because of cascades
                Alias organismAlias = getAliasSynchronizer().synchronize(alias, false);
                // we have a different instance because needed to be synchronized
                if (organismAlias != alias){
                    intactOrganism.getAliases().remove(alias);
                    intactOrganism.getAliases().add(organismAlias);
                }
            }
        }
    }

    protected void prepareFullName(IntactOrganism intactOrganism) {
        // truncate if necessary
        if (intactOrganism.getScientificName() != null && IntactUtils.MAX_FULL_NAME_LEN < intactOrganism.getScientificName().length()){
            log.warn("Organism scientific name too long: "+intactOrganism.getScientificName()+", will be truncated to "+ IntactUtils.MAX_FULL_NAME_LEN+" characters.");
            intactOrganism.setScientificName(intactOrganism.getScientificName().substring(0, IntactUtils.MAX_FULL_NAME_LEN));
        }
    }

    protected void prepareAndSynchronizeCommonName(IntactOrganism intactOrganism) {
        // set shortname if not done yet
        if (intactOrganism.getCommonName() == null){
            intactOrganism.setCommonName(intactOrganism.getScientificName() != null ? intactOrganism.getScientificName() : Integer.toString(intactOrganism.getTaxId()));
            if (intactOrganism.getCellType() != null){
                intactOrganism.setCommonName(intactOrganism.getCommonName()+"-"+intactOrganism.getCellType().getShortName());
            }
            else if (intactOrganism.getTissue() != null){
                intactOrganism.setCommonName(intactOrganism.getCommonName()+"-"+intactOrganism.getTissue().getShortName());
            }
            else if (intactOrganism.getTissue() != null){
                intactOrganism.setCommonName(intactOrganism.getCommonName()+"-"+intactOrganism.getCompartment().getShortName());
            }
        }
        // truncate if necessary
        if (IntactUtils.MAX_SHORT_LABEL_LEN < intactOrganism.getCommonName().length()){
            log.warn("Organism shortLabel too long: "+intactOrganism.getCommonName()+", will be truncated to "+ IntactUtils.MAX_SHORT_LABEL_LEN+" characters.");
            intactOrganism.setCommonName(intactOrganism.getCommonName().substring(0, IntactUtils.MAX_SHORT_LABEL_LEN));
        }
        boolean first = true;
        String name;
        List<String> existingOrganism;
        do{
            name = intactOrganism.getCommonName().trim().toLowerCase();
            existingOrganism = Collections.EMPTY_LIST;
            String originalName = first ? name : IntactUtils.excludeLastNumberInShortLabel(name);

            if (first){
                first = false;
            }
            else if (originalName.length() > 1){
                name = originalName.substring(0, originalName.length() - 1);
            }
            // check if short name already exist, if yes, synchronize with existing label
            Query query = getEntityManager().createQuery("select o.shortName from IntactOrganism o " +
                    "where o.commonName = :name or o.commonName like :nameWithSuffix"
                    + (intactOrganism.getAc() != null ? "and o.ac <> :organismAc" : ""));
            query.setParameter("name", name);
            query.setParameter("nameWithSuffix", name+"-%");
            if (intactOrganism.getAc() != null){
                query.setParameter("interactorAc", intactOrganism.getAc());
            }
            existingOrganism = query.getResultList();
            String nameInSync = IntactUtils.synchronizeShortlabel(name, existingOrganism, IntactUtils.MAX_SHORT_LABEL_LEN, false);
            intactOrganism.setCommonName(nameInSync);
        }
        while(name.length() > 1 && !existingOrganism.isEmpty());
    }

    @Override
    protected Object extractIdentifier(IntactOrganism object) {
        return object.getAc();
    }

    @Override
    protected IntactOrganism instantiateNewPersistentInstance(Organism object, Class<? extends IntactOrganism> intactClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return intactClass.getConstructor(Integer.class).newInstance(object.getTaxId());
    }

    @Override
    protected void initialiseDefaultMerger() {
        super.setIntactMerger(new OrganismMergerEnrichOnly(this));
    }
}
