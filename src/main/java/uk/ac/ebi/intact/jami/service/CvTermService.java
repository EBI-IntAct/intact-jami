package uk.ac.ebi.intact.jami.service;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.CvTerm;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.interceptor.IntactTransactionSynchronization;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * Cv term service
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/02/14</pre>
 */
@Service(value = "cvTermService")
@Lazy
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class CvTermService implements IntactService<CvTerm>{

    @Resource(name = "intactDao")
    private IntactDao intactDAO;
    @Resource(name = "intactTransactionSynchronization")
    private IntactTransactionSynchronization afterCommitExecutor;
    private String objClass;

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public long countAll() {
        return this.intactDAO.getCvTermDao().countAll();
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public Iterator<CvTerm> iterateAll() {
        // use proxy and not this for transactional annotations to work
        return new IntactQueryResultIterator<CvTerm>((CvTermService) ApplicationContextProvider.getBean("cvTermService"));
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public List<CvTerm> fetchIntactObjects(int first, int max) {
        return new ArrayList<CvTerm>(this.intactDAO.getCvTermDao().getAll("ac", first, max));
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public long countAll(String countQuery, Map<String, Object> parameters) {
        return this.intactDAO.getCvTermDao().countByQuery(countQuery, parameters);
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public Iterator<CvTerm> iterateAll(String queryCount, String query, Map<String, Object> parameters) {
        // use proxy and not this for transactional annotations to work
        return new IntactQueryResultIterator<CvTerm>((CvTermService) ApplicationContextProvider.getBean("cvTermService"), query, queryCount, parameters);
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public List<CvTerm> fetchIntactObjects(String query, Map<String, Object> parameters, int first, int max) {
        return new ArrayList<CvTerm>(this.intactDAO.getCvTermDao().getByQuery(query, parameters, first, max));
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public List<CvTerm> fetchIntactObjects(String query, Map<String, Object> parameters) {
        return new ArrayList<CvTerm>(this.intactDAO.getCvTermDao().getByQuery(query, parameters, 0, Integer.MAX_VALUE));
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public Iterator<CvTerm> iterateAll(boolean loadLazyCollections) {
        // use proxy and not this for transactional annotations to work
        return new IntactQueryResultIterator<CvTerm>((CvTermService) ApplicationContextProvider.getBean("cvTermService"), loadLazyCollections);
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public List<CvTerm> fetchIntactObjects(int first, int max, boolean loadLazyCollections) {
        List<CvTerm> results = new ArrayList<CvTerm>(this.intactDAO.getCvTermDao().getAll("ac", first, max));
        initialiseLazyCvTerm(loadLazyCollections, results);
        return results;
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public Iterator<CvTerm> iterateAll(String countQuery, String query, Map<String, Object> parameters, boolean loadLazyCollections) {
        // use proxy and not this for transactional annotations to work
        return new IntactQueryResultIterator<CvTerm>((CvTermService) ApplicationContextProvider.getBean("cvTermService"), query, countQuery, parameters, loadLazyCollections);
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public List<CvTerm> fetchIntactObjects(String query, Map<String, Object> parameters, int first, int max, boolean loadLazyCollections) {
        List<CvTerm> results = new ArrayList<CvTerm>(this.intactDAO.getCvTermDao().getByQuery(query, parameters, first, max));
        initialiseLazyCvTerm(loadLazyCollections, results);
        return results;
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public List<CvTerm> fetchIntactObjects(String query, Map<String, Object> parameters, boolean loadLazyCollections) {
        List<CvTerm> results = new ArrayList<CvTerm>(this.intactDAO.getCvTermDao().getByQuery(query, parameters, 0, Integer.MAX_VALUE));
        initialiseLazyCvTerm(loadLazyCollections, results);
        return results;
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void saveOrUpdate(CvTerm object) throws PersisterException, FinderException, SynchronizerException {
        afterCommitExecutor.registerDaoForSynchronization(intactDAO);
        // we can synchronize the complex with the database now
        intactDAO.getSynchronizerContext().getCvSynchronizer(this.objClass).synchronize(object, true);
        this.intactDAO.getSynchronizerContext().getCvSynchronizer(this.objClass).flush();
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void saveOrUpdate(Collection<? extends CvTerm> objects) throws SynchronizerException, PersisterException, FinderException {
        afterCommitExecutor.registerDaoForSynchronization(intactDAO);
        for (CvTerm cv : objects){
            // we can synchronize the complex with the database now
            intactDAO.getSynchronizerContext().getCvSynchronizer(this.objClass).synchronize(cv, true);
        }
        this.intactDAO.getSynchronizerContext().getCvSynchronizer(this.objClass).flush();
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void delete(CvTerm object) throws PersisterException, FinderException, SynchronizerException {
        afterCommitExecutor.registerDaoForSynchronization(intactDAO);
        this.intactDAO.getSynchronizerContext().getCvSynchronizer(this.objClass).delete(object);
        this.intactDAO.getSynchronizerContext().getCvSynchronizer(this.objClass).flush();
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public void delete(Collection<? extends CvTerm> objects) throws SynchronizerException, PersisterException, FinderException {
        afterCommitExecutor.registerDaoForSynchronization(intactDAO);
        for (CvTerm cv : objects){
            this.intactDAO.getSynchronizerContext().getCvSynchronizer(this.objClass).delete(cv);
        }
        this.intactDAO.getSynchronizerContext().getCvSynchronizer(this.objClass).flush();
    }

    public String getObjClass() {
        return objClass;
    }

    public void setObjClass(String objClass) {
        this.objClass = objClass;
    }

    public IntactDao getIntactDao() {
        return intactDAO;
    }

    private void initialiseLazyCvTerm(boolean loadLazyCollections, List<CvTerm> results) {
        if (loadLazyCollections){
            for (CvTerm cv : results){
                IntactUtils.initialiseCvTerm((IntactCvTerm) cv);
            }
        }
    }
}
