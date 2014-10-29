package uk.ac.ebi.intact.jami.synchronizer.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.model.Alias;
import psidev.psi.mi.jami.model.CvTerm;
import uk.ac.ebi.intact.jami.context.SynchronizerContext;
import uk.ac.ebi.intact.jami.merger.IntactDbMergerIgnoringPersistentObject;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactAlias;
import uk.ac.ebi.intact.jami.synchronizer.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;

/**
 * Finder/persister for alias
 *
 * It does not cache persisted aliases. It only synchronize the alias type (with persist = true) to make sure that the alias type
 * is persisted before so the alias can be persisted
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/01/14</pre>
 */

public class AliasSynchronizerTemplate<A extends AbstractIntactAlias> extends AbstractIntactDbSynchronizer<Alias, A> implements AliasSynchronizer<A> {

    private static final Log log = LogFactory.getLog(AliasSynchronizerTemplate.class);

    public AliasSynchronizerTemplate(SynchronizerContext context, Class<? extends A> aliasClass){
        super(context, aliasClass);
    }

    public A find(Alias object) throws FinderException {
        return null;
    }

    @Override
    public Collection<A> findAll(Alias object) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<String> findAllMatchingAcs(Alias object) {
        return Collections.EMPTY_LIST;
    }

    public void synchronizeProperties(A object) throws FinderException, PersisterException, SynchronizerException {
        if (object.getType() != null){
            CvTerm type = object.getType();
            object.setType(getContext().getAliasTypeSynchronizer().synchronize(type, true));
        }
    }

    public void clearCache() {
        // nothing to clear
    }

    @Override
    protected Object extractIdentifier(A object) {
        return object.getAc();
    }

    @Override
    protected A instantiateNewPersistentInstance(Alias object, Class<? extends A> intactClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return intactClass.getConstructor(CvTerm.class, String.class).newInstance(object.getType(), object.getName());
    }

    @Override
    protected void storeInCache(Alias originalObject, A persistentObject, A existingInstance) {
        // nothing to do
    }

    @Override
    protected A fetchObjectFromCache(Alias object) {
        return null;
    }

    @Override
    protected boolean isObjectStoredInCache(Alias object) {
        return false;
    }

    @Override
    protected boolean containsObjectInstance(Alias object) {
        return false;
    }

    @Override
    protected void removeObjectInstanceFromIdentityCache(Alias object) {
        // nothing to do
    }

    @Override
    protected A fetchMatchingObjectFromIdentityCache(Alias object) {
        return null;
    }

    @Override
    protected void convertPersistableProperties(A object) throws SynchronizerException, PersisterException, FinderException {
        if (object.getType() != null){
            CvTerm type = object.getType();
            object.setType(getContext().getAliasTypeSynchronizer().convertToPersistentObject(type));
        }
    }

    @Override
    protected void storeObjectInIdentityCache(Alias originalObject, A persistableObject) {
        // nothing to cache here
    }

    @Override
    protected boolean isObjectDirty(Alias originalObject) {
        return false;
    }

    @Override
    protected void initialiseDefaultMerger() {
        super.setIntactMerger(new IntactDbMergerIgnoringPersistentObject<Alias, A>(this));
    }
}
