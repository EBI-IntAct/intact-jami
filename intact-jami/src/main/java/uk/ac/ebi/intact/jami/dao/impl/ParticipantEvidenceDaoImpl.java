package uk.ac.ebi.intact.jami.dao.impl;

import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.jami.context.SynchronizerContext;
import uk.ac.ebi.intact.jami.dao.ParticipantEvidenceDao;
import uk.ac.ebi.intact.jami.model.extension.IntactParticipantEvidence;
import uk.ac.ebi.intact.jami.synchronizer.IntactDbSynchronizer;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collection;

/**
 * Implementation of participant evidence dao
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/01/14</pre>
 */
public class ParticipantEvidenceDaoImpl extends ParticipantDaoImpl<ParticipantEvidence, IntactParticipantEvidence>
        implements ParticipantEvidenceDao {

    public ParticipantEvidenceDaoImpl(EntityManager entityManager, SynchronizerContext context) {
        super(IntactParticipantEvidence.class, entityManager, context);
    }

    @Override
    public IntactDbSynchronizer getDbSynchronizer() {
        return getSynchronizerContext().getParticipantEvidenceSynchronizer();
    }

    public Collection<IntactParticipantEvidence> getByExperimentalRole(String typeName, String typeMI, int first, int max) {
        Query query;
        if (typeMI != null){
            query = getEntityManager().createQuery("select distinct f from IntactParticipantEvidence f "  +
                    "join f.dbExperimentalRoles as e " +
                    "join e.dbXrefs as xref " +
                    "join xref.database as d " +
                    "join xref.qualifier as q " +
                    "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                    "and d.shortName = :psimi " +
                    "and xref.id = :mi order by e.ac");
            query.setParameter("identity", Xref.IDENTITY);
            query.setParameter("secondaryAc", Xref.SECONDARY);
            query.setParameter("psimi", CvTerm.PSI_MI);
            query.setParameter("mi", typeMI);
        }
        else{
            query = getEntityManager().createQuery("select f from "+getEntityClass().getSimpleName()+" f "  +
                    "join f.experimentalRole as e " +
                    "where e.shortName = :name order by e.ac");
            query.setParameter("name", typeName);
        }
        query.setFirstResult(first);
        query.setMaxResults(max);
        return query.getResultList();
    }

    public Collection<IntactParticipantEvidence> getByExperimentalPreparation(String name, String mi, int first, int max) {
        Query query;
        if (mi != null){
            query = getEntityManager().createQuery("select distinct f from IntactParticipantEvidence f "  +
                    "join f.experimentalPreparations as e " +
                    "join e.dbXrefs as xref " +
                    "join xref.database as d " +
                    "join xref.qualifier as q " +
                    "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                    "and d.shortName = :psimi " +
                    "and xref.id = :mi order by e.ac");
            query.setParameter("identity", Xref.IDENTITY);
            query.setParameter("secondaryAc", Xref.SECONDARY);
            query.setParameter("psimi", CvTerm.PSI_MI);
            query.setParameter("mi", mi);
        }
        else{
            query = getEntityManager().createQuery("select distinct f from IntactParticipantEvidence f "  +
                    "join f.experimentalPreparations as e " +
                    "where e.shortName = :name order by e.ac");
            query.setParameter("name", name);
        }
        query.setFirstResult(first);
        query.setMaxResults(max);
        return query.getResultList();
    }

    public Collection<IntactParticipantEvidence> getByDetectionMethod(String name, String mi, int first, int max) {
        Query query;
        if (name == null && mi == null){
            query = getEntityManager().createQuery("select distinct f from IntactParticipantEvidence f "  +
                    "where f.dbIdentificationMethods is empty order by f.ac");
        }
        else if (mi != null){
            query = getEntityManager().createQuery("select distinct f from IntactParticipantEvidence f "  +
                    "join f.dbIdentificationMethods as i " +
                    "join i.dbXrefs as xref " +
                    "join xref.database as d " +
                    "join xref.qualifier as q " +
                    "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                    "and d.shortName = :psimi " +
                    "and xref.id = :mi order by f.ac");
            query.setParameter("identity", Xref.IDENTITY);
            query.setParameter("secondaryAc", Xref.SECONDARY);
            query.setParameter("psimi", CvTerm.PSI_MI);
            query.setParameter("mi", mi);
        }
        else{
            query = getEntityManager().createQuery("select distinct f from IntactParticipantEvidence f "  +
                    "join f.dbIdentificationMethods as i " +
                    "where i.shortName = :name order by f.ac");
            query.setParameter("name", name);
        }
        query.setFirstResult(first);
        query.setMaxResults(max);
        return query.getResultList();
    }

    public Collection<IntactParticipantEvidence> getByExpressedInTaxid(String taxid, int first, int max) {
        Query query = getEntityManager().createQuery("select f from IntactParticipantEvidence f "  +
                "join f.expressedInOrganism as o " +
                "where o.dbTaxid = :taxid order by f.ac");
        query.setParameter("taxid", taxid);
        query.setFirstResult(first);
        query.setMaxResults(max);
        return query.getResultList();
    }

    public Collection<IntactParticipantEvidence> getByExpressedInAc(String ac, int first, int max) {
        Query query = getEntityManager().createQuery("select f from IntactParticipantEvidence f "  +
                "join f.expressedInOrganism as o " +
                "where o.ac = :orgAc order by f.ac");
        query.setParameter("orgAc", ac);
        query.setFirstResult(first);
        query.setMaxResults(max);
        return query.getResultList();
    }

    public Collection<IntactParticipantEvidence> getByConfidence(String typeName, String typeMI, String value, int first, int max) {
        Query query;
        if (typeName == null && typeMI == null){
            query = getEntityManager().createQuery("select distinct f from IntactParticipantEvidence f "  +
                    "where f.confidences is empty order by f.ac");
        }
        else if (typeMI != null){
            query = getEntityManager().createQuery("select distinct f from IntactParticipantEvidence f "  +
                    "join f.confidences as c " +
                    "join c.type as t " +
                    "join t.dbXrefs as xref " +
                    "join xref.database as d " +
                    "join xref.qualifier as q " +
                    "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                    "and d.shortName = :psimi " +
                    "and xref.id = :mi " + (value != null ? " and c.value = :confValue ":" ")+
                    "order by f.ac");
            query.setParameter("identity", Xref.IDENTITY);
            query.setParameter("secondaryAc", Xref.SECONDARY);
            query.setParameter("psimi", CvTerm.PSI_MI);
            query.setParameter("mi", typeMI);
            if (value != null){
                query.setParameter("confValue", value);
            }
        }
        else{
            query = getEntityManager().createQuery("select distinct f from IntactParticipantEvidence f "  +
                    "join f.confidences as c " +
                    "join c.type as t " +
                    "where t.shortName = :name "  + (value != null ? " and c.value = :confValue ":" ")+
                    "order by f.ac");
            query.setParameter("name", typeName);
            if (value != null){
                query.setParameter("confValue", value);
            }
        }
        query.setFirstResult(first);
        query.setMaxResults(max);
        return query.getResultList();
    }

    public Collection<IntactParticipantEvidence> getByInteractionAc(String ac) {
        Query query = getEntityManager().createQuery("select f from IntactParticipantEvidence f "  +
                "join f.dbParentInteraction as i " +
                "where i.ac = : interAc");
        query.setParameter("interAc", ac);
        return query.getResultList();
    }

    public Collection<IntactParticipantEvidence> getByParameterType(String typeName, String typeMI) {
        Query query;
        if (typeMI != null){
            query = getEntityManager().createQuery("select distinct i from IntactParticipantEvidence i " +
                    "join i.parameters as p " +
                    "join p.type as t " +
                    "join t.dbXrefs as x " +
                    "join x.database as d " +
                    "join x.qualifier as q " +
                    "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                    "and d.shortName = :psimi " +
                    "and x.id = :mi");
            query.setParameter("identity", Xref.IDENTITY);
            query.setParameter("secondaryAc", Xref.SECONDARY);
            query.setParameter("psimi", CvTerm.PSI_MI);
            query.setParameter("mi", typeMI);
        }
        else{
            query = getEntityManager().createQuery("select distinct i from IntactParticipantEvidence i " +
                    "join i.parameters as p " +
                    "join p.type as t " +
                    "where t.shortName = :paramName");
            query.setParameter("paramName", typeName);
        }
        return query.getResultList();
    }

    public Collection<IntactParticipantEvidence> getByParameterUnit(String unitName, String unitMI) {
        Query query;
        if (unitMI == null && unitName == null){
            query = getEntityManager().createQuery("select i from IntactParticipantEvidence i " +
                    "join i.parameters as p " +
                    "where p.unit is null");
        }
        else if (unitMI != null){
            query = getEntityManager().createQuery("select distinct i from IntactParticipantEvidence i " +
                    "join i.parameters as p " +
                    "join p.unit as u " +
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
            query = getEntityManager().createQuery("select distinct i from IntactParticipantEvidence i " +
                    "join i.parameters as p " +
                    "join p.unit as u " +
                    "where u.shortName = :unitName");
            query.setParameter("unitName", unitName);
        }
        return query.getResultList();
    }

    public Collection<IntactParticipantEvidence> getByParameterTypeAndUnit(String typeName, String typeMI, String unitName, String unitMI) {
        Query query;
        if (typeMI != null){
            if (unitMI == null && unitName == null){
                query = getEntityManager().createQuery("select distinct i from IntactParticipantEvidence i " +
                        "join i.parameters as p " +
                        "join p.type as t " +
                        "join t.dbXrefs as x " +
                        "join x.database as d " +
                        "join x.qualifier as q " +
                        "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                        "and d.shortName = :psimi " +
                        "and x.id = :mi " +
                        "and p.unit is null");
                query.setParameter("identity", Xref.IDENTITY);
                query.setParameter("secondaryAc", Xref.SECONDARY);
                query.setParameter("psimi", CvTerm.PSI_MI);
                query.setParameter("mi", typeMI);
            }
            else if (unitMI != null){
                query = getEntityManager().createQuery("select distinct i from IntactParticipantEvidence i " +
                        "join i.parameters as p " +
                        "join p.type as t " +
                        "join t.dbXrefs as x " +
                        "join x.database as d " +
                        "join x.qualifier as q " +
                        "join p.unit as u " +
                        "join u.dbXrefs as x2 " +
                        "join x2.database as d2 " +
                        "join x2.qualifier as q2 " +
                        "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                        "and d.shortName = :psimi " +
                        "and x.id = :mi " +
                        "and (q2.shortName = :identity or q2.shortName = :secondaryAc) "+
                        "and d2.shortName = :psimi "+
                        "and x2.id = :mi2 ");
                query.setParameter("identity", Xref.IDENTITY);
                query.setParameter("secondaryAc", Xref.SECONDARY);
                query.setParameter("psimi", CvTerm.PSI_MI);
                query.setParameter("mi", typeMI);
                query.setParameter("mi2", unitMI);
            }
            else{
                query = getEntityManager().createQuery("select distinct i from IntactParticipantEvidence i " +
                        "join i.parameters as p " +
                        "join p.type as t " +
                        "join t.dbXrefs as x " +
                        "join x.database as d " +
                        "join x.qualifier as q " +
                        "join p.unit as u " +
                        "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                        "and d.shortName = :psimi " +
                        "and x.id = :mi " +
                        "and u.shortName = :unitName");
                query.setParameter("identity", Xref.IDENTITY);
                query.setParameter("secondaryAc", Xref.SECONDARY);
                query.setParameter("psimi", CvTerm.PSI_MI);
                query.setParameter("mi", typeMI);
                query.setParameter("unitName", unitName);
            }
        }
        else{
            if (unitMI == null && unitName == null){
                query = getEntityManager().createQuery("select distinct i from IntactParticipantEvidence i " +
                        "join i.parameters as p " +
                        "join p.type as t " +
                        "where t.shortName = :confName " +
                        "and p.unit is null");
                query.setParameter("typeName", typeName);
            }
            else if (unitMI != null){
                query = getEntityManager().createQuery("select distinct i from IntactParticipantEvidence i " +
                        "join i.parameters as p " +
                        "join p.type as t " +
                        "join p.unit as u " +
                        "join u.dbXrefs as x " +
                        "join x.database as d " +
                        "join x.qualifier as q " +
                        "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                        "and d.shortName = :psimi " +
                        "and x.id = :mi " +
                        "and t.shortName = :typeName");
                query.setParameter("identity", Xref.IDENTITY);
                query.setParameter("secondaryAc", Xref.SECONDARY);
                query.setParameter("psimi", CvTerm.PSI_MI);
                query.setParameter("mi", unitMI);
                query.setParameter("typeName", typeName);
            }
            else{
                query = getEntityManager().createQuery("select distinct i from IntactParticipantEvidence i " +
                        "join i.parameters as p " +
                        "join p.type as t " +
                        "join p.unit as u " +
                        "where u.shortName = :unitName " +
                        "and t.shortName = :typeName");
                query.setParameter("unitName", unitName);
                query.setParameter("typeName", typeName);
            }
        }
        return query.getResultList();
    }

    public Collection<IntactParticipantEvidence> getByCausalRelationType(String typeName, String typeMI) {
        Query query;
        if (typeMI != null){
            query = getEntityManager().createQuery("select distinct e from IntactParticipantEvidence e " +
                    "join e.causalRelationships as c " +
                    "join c.relationType as t " +
                    "join t.dbXrefs as x " +
                    "join x.database as d " +
                    "join x.qualifier as q " +
                    "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                    "and d.shortName = :psimi " +
                    "and x.id = :mi");
            query.setParameter("identity", Xref.IDENTITY);
            query.setParameter("secondaryAc", Xref.SECONDARY);
            query.setParameter("psimi", CvTerm.PSI_MI);
            query.setParameter("mi", typeMI);
        }
        else{
            query = getEntityManager().createQuery("select distinct e from IntactParticipantEvidence e " +
                    "join e.causalRelationships as c " +
                    "join c.relationType as t " +
                    "where t.shortName = :unitName");
            query.setParameter("unitName", typeName);
        }
        return query.getResultList();
    }

    public Collection<IntactParticipantEvidence> getByCausalRelationshipTargetAc(String parentAc, boolean isExperimental) {
        Query query = getEntityManager().createQuery("select distinct e from IntactParticipantEvidence e  " +
                "join e.causalRelationships as c " +
                (isExperimental ? "join c.experimentalTarget as t " : "join c.modelledTarget as t ")+
                "where t.ac = :ac ");
        query.setParameter("ac",parentAc);
        return query.getResultList();
    }

    public Collection<IntactParticipantEvidence> getByCausalRelationship(String name, String mi, String targetAc, boolean isExperimental) {
        Query query;
        if (mi != null){
            query = getEntityManager().createQuery("select distinct f from IntactParticipantEvidence f "  +
                    "join f.causalRelationships as c " +
                    "join c.dbXrefs as xref " +
                    "join xref.database as d " +
                    "join xref.qualifier as q " +
                    (isExperimental ? "join c.experimentalTarget as t " : "join c.modelledTarget as t ")+
                    "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                    "and d.shortName = :psimi " +
                    "and xref.id = :mi"+(targetAc != null ? " and t.ac = :tarAc" : ""));
            query.setParameter("identity", Xref.IDENTITY);
            query.setParameter("secondaryAc", Xref.SECONDARY);
            query.setParameter("psimi", CvTerm.PSI_MI);
            query.setParameter("mi", mi);
            query.setParameter("tarAc", targetAc);
        }
        else{
            query = getEntityManager().createQuery("select distinct f from "+getEntityClass().getSimpleName()+" f "  +
                    "join f.causalRelationships as c " +
                    (isExperimental ? "join c.experimentalTarget as t " : "join c.modelledTarget as t ")+
                    "where c.shortName = :effectName"+(targetAc != null ? " and t.ac = :tarAc" : ""));
            query.setParameter("effectName", name);
            query.setParameter("tarAc", targetAc);
        }
        return query.getResultList();
    }

    @Override
    public Collection<Confidence> getConfidencesForParticipant(String ac) {
        Query query = getEntityManager().createQuery("select x from IntactParticipantEvidence i " +
                "join i.confidences as x " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return query.getResultList();
    }

    @Override
    public Collection<Parameter> getParametersForParticipant(String ac) {
        Query query = getEntityManager().createQuery("select x from IntactParticipantEvidence i " +
                "join i.parameters as x " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return query.getResultList();
    }

    @Override
    public int countConfidencesForParticipant(String ac) {
        Query query = getEntityManager().createQuery("select size(i.confidences) from IntactParticipantEvidence i " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return (Integer)query.getSingleResult();
    }

    @Override
    public int countParametersForParticipant(String ac) {
        Query query = getEntityManager().createQuery("select size(i.parameters) from IntactParticipantEvidence i " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return (Integer)query.getSingleResult();
    }

    @Override
    public int countExperimentalPreparationsForParticipant(String ac) {
        Query query = getEntityManager().createQuery("select size(i.experimentalPreparations) from IntactParticipantEvidence i " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return (Integer)query.getSingleResult();
    }

    @Override
    public int countIdentificationMethodsForParticipant(String ac) {
        Query query = getEntityManager().createQuery("select size(i.dbIdentificationMethods) from IntactParticipantEvidence i " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return (Integer)query.getSingleResult();
    }

    @Override
    public int countParticipantsByExpressedInOrganism(String organismAc) {
        Query query = getEntityManager().createQuery("select count (distinct i.ac) from IntactParticipantEvidence i " +
                "join i.expressedInOrganism o " +
                "where o.ac = :ac");
        query.setParameter("ac", organismAc);
        Long results = (Long)query.getSingleResult();
        return results != null ? results.intValue() : 0;
    }
}
