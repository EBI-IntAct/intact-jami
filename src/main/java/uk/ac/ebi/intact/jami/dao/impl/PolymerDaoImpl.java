package uk.ac.ebi.intact.jami.dao.impl;

import psidev.psi.mi.jami.model.Polymer;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.jami.context.SynchronizerContext;
import uk.ac.ebi.intact.jami.dao.PolymerDao;
import uk.ac.ebi.intact.jami.model.extension.IntactPolymer;
import uk.ac.ebi.intact.jami.synchronizer.IntactDbSynchronizer;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collection;

/**
 * Implementation of polymerDao
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/14</pre>
 */
public class PolymerDaoImpl<T extends Polymer, P extends IntactPolymer> extends InteractorDaoImpl<T,P> implements PolymerDao<P>{

    public PolymerDaoImpl(EntityManager entityManager, SynchronizerContext context) {
        super((Class<P>)IntactPolymer.class, entityManager, context);
    }

    public PolymerDaoImpl(Class<P> entityClass, EntityManager entityManager, SynchronizerContext context) {
        super(entityClass, entityManager, context);
    }

    public String getSequenceByPolymerAc(String ac) {
        IntactPolymer polymer = getEntityManager().find(IntactPolymer.class, ac);
        if (polymer == null){
            return null;
        }
        return polymer.getSequence();
    }

    public Collection<P> getByCanonicalIds(String dbMI, Collection<String> primaryIds) {
        Query query = getEntityManager().createQuery("select distinct f " +
                "from " + getEntityClass().getSimpleName() + " f "  +
                "where exists( " +
                "  select f2 " +
                "  from " + getEntityClass().getSimpleName() + " f2 " +
                "  join f2.dbXrefs as xref " +
                "  join xref.database as database " +
                "  join xref.qualifier as qualifier " +
                "  where f.ac = f2.ac " +
                "  and database.identifier = :dbMi " +
                "  and qualifier.identifier in (:identityMi, :secondaryMi, :chainParentMi, :isoformParentMi) " +
                "  and xref.id in (:primaryIds) " +
                ") or exists( " +
                "  select chain_parent " +
                "  from " + getEntityClass().getSimpleName() + " chain_parent " +
                "  join chain_parent.dbXrefs as chain_parent_xref " +
                "  join chain_parent_xref.database as chain_parent_database " +
                "  join chain_parent_xref.qualifier as chain_parent_qualifier " +
                "  where f.ac = chain_parent.ac " +
                "  and chain_parent_database.identifier = :intactMi " +
                "  and chain_parent_qualifier.identifier in (:chainParentMi, :isoformParentMi) " +
                "  and exists( " +
                "    select f2 " +
                "    from " + getEntityClass().getSimpleName() + " f2 " +
                "    join f2.dbXrefs as xref " +
                "    join xref.database as database " +
                "    join xref.qualifier as qualifier " +
                "    where chain_parent_xref.id = f2.ac " +
                "    and database.identifier = :dbMi " +
                "    and qualifier.identifier in (:identityMi, :secondaryMi) " +
                "    and xref.id in (:primaryIds) " +
                "  )" +
                ")");
        query.setParameter("identityMi", Xref.IDENTITY_MI);
        query.setParameter("secondaryMi", Xref.SECONDARY_MI);
        query.setParameter("chainParentMi", Xref.CHAIN_PARENT_MI);
        query.setParameter("isoformParentMi", Xref.ISOFORM_PARENT_MI);
        query.setParameter("intactMi", Xref.INTACT_MI);
        query.setParameter("dbMi", dbMI);
        query.setParameter("primaryIds", primaryIds);
        return query.getResultList();
    }

    @Override
    public IntactDbSynchronizer getDbSynchronizer() {
        return getSynchronizerContext().getPolymerSynchronizer();
    }
}
