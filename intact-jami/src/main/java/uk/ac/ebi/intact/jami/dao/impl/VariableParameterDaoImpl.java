package uk.ac.ebi.intact.jami.dao.impl;

import org.springframework.stereotype.Repository;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.jami.context.DefaultSynchronizerContext;
import uk.ac.ebi.intact.jami.context.SynchronizerContext;
import uk.ac.ebi.intact.jami.dao.VariableParameterDao;
import uk.ac.ebi.intact.jami.model.extension.IntactVariableParameter;
import uk.ac.ebi.intact.jami.model.user.User;
import uk.ac.ebi.intact.jami.synchronizer.IntactDbSynchronizer;
import uk.ac.ebi.intact.jami.synchronizer.impl.VariableParameterSynchronizer;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collection;

/**
 * Implementation of alias dao
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/01/14</pre>
 */
public class VariableParameterDaoImpl extends AbstractIntactBaseDao<VariableParameter, IntactVariableParameter> implements VariableParameterDao {

    public VariableParameterDaoImpl(EntityManager entityManager, SynchronizerContext context) {
        super(IntactVariableParameter.class, entityManager, context);
    }

    public Collection<IntactVariableParameter> getByDescription(String desc) {
        Query query = getEntityManager().createQuery("select v from IntactVariableParameter v where v.description = :description");
        query.setParameter("description",desc);
        return query.getResultList();
    }

    public Collection<IntactVariableParameter> getByDescriptionLike(String desc) {
        Query query = getEntityManager().createQuery("select v from IntactVariableParameter v where upper(v.description) like :description");
        query.setParameter("description","%"+desc.toUpperCase()+"%");
        return query.getResultList();
    }

    public Collection<IntactVariableParameter> getByUnit(String unitName, String unitMI) {
        Query query;
        if (unitName == null && unitMI == null){
            query = getEntityManager().createQuery("select v from IntactVariableParameter v where v.unit is null");
        }
        else if (unitMI != null){
            query = getEntityManager().createQuery("select distinct v from IntactVariableParameter v " +
                    "join v.unit as u " +
                    "join u.dbXrefs as x " +
                    "join x.database as d " +
                    "join x.qualifier as q " +
                    "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                    "and d.shortName = :psimi " +
                    "and x.id = :mi");
            query.setParameter("identity", Xref.IDENTITY);
            query.setParameter("secondaryAc", Xref.SECONDARY);
            query.setParameter("psimi", CvTerm.PSI_MI);
            query.setParameter("mi", unitMI);
        }
        else{
            query = getEntityManager().createQuery("select v from IntactVariableParameter v " +
                    "join v.unit as u " +
                    "where u.shortName = :unitName");
            query.setParameter("unitName", unitName);
        }
        return query.getResultList();
    }

    public Collection<IntactVariableParameter> getByExperimentAc(String parentAc) {
        Query query = getEntityManager().createQuery("select v from IntactVariableParameter v " +
                "join v.experiment as p " +
                "where p.ac = :ac ");
        query.setParameter("ac",parentAc);
        return query.getResultList();
    }

    @Override
    public IntactDbSynchronizer<VariableParameter, IntactVariableParameter> getDbSynchronizer() {
        return getSynchronizerContext().getVariableParameterSynchronizer();
    }
}