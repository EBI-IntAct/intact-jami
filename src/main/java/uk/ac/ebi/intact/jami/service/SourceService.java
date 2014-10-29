package uk.ac.ebi.intact.jami.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.Source;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.interceptor.IntactTransactionSynchronization;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;

import java.util.*;

/**
 * Source service
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/02/14</pre>
 */
@Service(value = "sourceService")
@Lazy
public class SourceService implements IntactService<Source>{

    @Autowired
    @Qualifier("intactDao")
    private IntactDao intactDAO;
    @Autowired
    @Qualifier("intactTransactionSynchronization")
    private IntactTransactionSynchronization afterCommitExecutor;

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public long countAll() {
        return this.intactDAO.getSourceDao().countAll();
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public Iterator<Source> iterateAll() {
        return new IntactQueryResultIterator<Source>(this);
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public List<Source> fetchIntactObjects(int first, int max) {
        return new ArrayList<Source>(this.intactDAO.getSourceDao().getAll("ac", first, max));
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public long countAll(String countQuery, Map<String, Object> parameters) {
        return this.intactDAO.getSourceDao().countByQuery(countQuery, parameters);
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public Iterator<Source> iterateAll(String queryCount, String query, Map<String, Object> parameters) {
        return new IntactQueryResultIterator<Source>(this, query, queryCount, parameters);
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public List<Source> fetchIntactObjects(String query, Map<String, Object> parameters, int first, int max) {
        return new ArrayList<Source>(this.intactDAO.getSourceDao().getByQuery(query, parameters, first, max));
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void saveOrUpdate(Source object) throws PersisterException, FinderException, SynchronizerException {
        afterCommitExecutor.registerDaoForSynchronization(intactDAO);
        // we can synchronize the complex with the database now
        intactDAO.getSynchronizerContext().getSourceSynchronizer().synchronize(object, true);
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void saveOrUpdate(Collection<? extends Source> objects) throws SynchronizerException, PersisterException, FinderException {
        afterCommitExecutor.registerDaoForSynchronization(intactDAO);
        for (Source source : objects){
            // we can synchronize the complex with the database now
            intactDAO.getSynchronizerContext().getSourceSynchronizer().synchronize(source, true);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void delete(Source object) throws PersisterException, FinderException, SynchronizerException {
        afterCommitExecutor.registerDaoForSynchronization(intactDAO);
        this.intactDAO.getSynchronizerContext().getSourceSynchronizer().delete(object);
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void delete(Collection<? extends Source> objects) throws SynchronizerException, PersisterException, FinderException {
        afterCommitExecutor.registerDaoForSynchronization(intactDAO);
        for (Source source : objects){
            this.intactDAO.getSynchronizerContext().getSourceSynchronizer().delete(source);
        }
    }
}
