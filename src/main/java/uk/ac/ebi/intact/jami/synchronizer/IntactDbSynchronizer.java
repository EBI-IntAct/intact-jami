package uk.ac.ebi.intact.jami.synchronizer;

import uk.ac.ebi.intact.jami.merger.IntactDbMerger;
import uk.ac.ebi.intact.jami.model.audit.Auditable;

import java.util.Collection;

/**
 * Interface for finders that can retrieve existing instances in the DB given an object
 * that can be transient.
 * It can also persist new Intact objects
 * If an object is cached, it will return the cached object and ignore changes to the object to synchronize
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/01/14</pre>
 */

public interface IntactDbSynchronizer<I, T extends Auditable> {


    /**
     * Finds a unique existing object in the database based on a business key
     * @param object
     * @return
     * @throws FinderException if several objects already exist in the database and it does not know which one to return
     */
    public T find(I object) throws FinderException;

    /**
     * Finds all existing object in the database based on a business key
     * @param object
     * @return  the collection of duplicated objects matching this object in the database
     */
    public Collection<T> findAll(I object);

    /**
     * Finds all existing object acs in the database based on a business key
     * @param object
     * @return  the collection of duplicated object acs matching this object in the database
     */
    public Collection<String> findAllMatchingAcs(I object);

    /**
     * Persist this object instance (MUST be an instance annotated with hibernate annotations) and
     * synchronize its properties when needed
     * @param object
     * @return the persisted instance
     * @throws FinderException
     * @throws PersisterException
     * @throws SynchronizerException
     */
    public T persist(T object) throws FinderException,PersisterException,SynchronizerException;

    /**
     * Synchronize all the properties of this object with the database.
     * It will persist transcient properties that are not managed with hibernate cascades.
     * @param object
     * @throws FinderException
     * @throws PersisterException
     * @throws SynchronizerException
     */
    public void synchronizeProperties(T object) throws FinderException,PersisterException,SynchronizerException;

    /**
     * Synchronize the given instance and its properties with the database,
     * If the object is not annotated with hibernate annotations, a new persistable object may be created and synchronized with the database.
     * If the object is already persisted in the database, it will reload the object from the database and merge it.
     * @param object
     * @param persist : if true, will persist the object if it is a transient object
     * @return the object synchronized and persisted.
     * @throws FinderException
     * @throws PersisterException
     * @throws SynchronizerException
     */
    public T synchronize(I object, boolean persist) throws FinderException,PersisterException,SynchronizerException;

    /**
     * Clear cached objects
     */
    public void clearCache();

    public IntactDbMerger<I,T> getIntactMerger();

    public void setIntactMerger(IntactDbMerger<I,T> intactMerger);

    public Class<? extends T> getIntactClass();

    public void setIntactClass(Class<? extends T> intactClass);

    /**
     * Delete the object and synchronize changes with database when needed
     * @param object
     * @return true if the object was deleted from the database
     */
    public boolean delete(I object);

    /**
     * If the object is not annotated with hibernate annotations or is lazy and needs to be treated with caution (merging detached objects for instance),
     * a new persistable object may be created. All the properties of this object will be converted to persistable hibernate entities
     * if it is not already the case.
     * @param object
     * @return the new object instance that is fully persistable but IS NOT synchronized with existing persistent objects in the database
     */
    public T convertToPersistentObject(I object) throws SynchronizerException, PersisterException, FinderException;;
}
