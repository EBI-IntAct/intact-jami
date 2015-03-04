package uk.ac.ebi.intact.jami.dao.impl;

import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.jami.context.SynchronizerContext;
import uk.ac.ebi.intact.jami.dao.InteractionDao;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractionEvidence;
import uk.ac.ebi.intact.jami.synchronizer.IntactDbSynchronizer;

import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of complexDao
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/14</pre>
 */
public class InteractionDaoImpl extends AbstractIntactBaseDao<InteractionEvidence, IntactInteractionEvidence> implements InteractionDao{

    public InteractionDaoImpl(EntityManager entityManager, SynchronizerContext context) {
        super(IntactInteractionEvidence.class, entityManager, context);
    }

    @Override
    public IntactDbSynchronizer<InteractionEvidence, IntactInteractionEvidence> getDbSynchronizer() {
        return getSynchronizerContext().getInteractionSynchronizer();
    }

    public IntactInteractionEvidence getByAc(String ac) {
        return getEntityManager().find(getEntityClass(), ac);
    }

    public IntactInteractionEvidence getByShortName(String value) {
        Query query = getEntityManager().createQuery("select f from IntactInteractionEvidence f " +
                "where f.shortName = :name");
        query.setParameter("name",value);
        List<IntactInteractionEvidence> results = query.getResultList();
        if (results.size() == 1){
            return results.iterator().next();
        }
        else if (results.isEmpty()){
            return null;
        }
        else{
            throw new NonUniqueResultException("We found "+results.size()+" interactors matching shortlabel "+value);
        }
    }

    public Collection<IntactInteractionEvidence> getByShortNameLike(String value) {
        Query query = getEntityManager().createQuery("select f from IntactInteractionEvidence f " +
                "where upper(f.shortName) like :name");
        query.setParameter("name","%"+value.toUpperCase()+"%");
        return query.getResultList();
    }

    public Collection<IntactInteractionEvidence> getByXref(String primaryId) {
        Query query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                "join f.dbXrefs as x " +
                "where x.id = :primaryId");
        query.setParameter("primaryId",primaryId);
        return query.getResultList();
    }

    @Override
    public Collection<IntactInteractionEvidence> getByInteractorsPrimaryId(String... primaryIds) {
        if (primaryIds.length > 5) {
            return getByInteractorsPrimaryIdExactComponents(primaryIds);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("select i from IntactInteractionEvidence as i ");

        for (int i=0; i<primaryIds.length; i++) {
            sb.append("join i.participants as comp").append(i).append(" ");
            sb.append("join comp").append(i).append(".interactor.dbXrefs as xref").append(i).append(" ");
        }

        sb.append("where ");

        for (int i=0; i<primaryIds.length; i++) {
            if (i>0) {
                sb.append("and ");
            }
            sb.append("xref").append(i).append(".id = :protPrimaryId").append(i).append(" ");
            sb.append("and ( xref").append(i).append(".qualifier.shortName = :identity").append(" or xref").append(i).
                    append(".qualifier.shortName = :secondary )").append(" ");
        }

        if (primaryIds.length > 0) {
            sb.append("and ");
        }
        sb.append("size(i.participants) = "+primaryIds.length);

        Query query = getEntityManager().createQuery(sb.toString());
        query.setParameter("identity", Xref.IDENTITY);
        query.setParameter("secondary", Xref.SECONDARY);
        for (int i=0; i<primaryIds.length; i++) {
            query.setParameter("protPrimaryId"+i, primaryIds[i]);
        }

        return query.getResultList();
    }

    protected List<IntactInteractionEvidence> getByInteractorsPrimaryIdExactComponents(String... primaryIds) {
        List<IntactInteractionEvidence> results = new ArrayList<IntactInteractionEvidence>();

        // get first all the interactions of the same size (efficient only for interactions with several components)
        Query query = getEntityManager().createQuery("select distinct i from IntactInteractionEvidence i where size(i.components) = :compSize");
        query.setParameter("compSize", primaryIds.length);

        List<IntactInteractionEvidence> interactionsOfTheSameSize = query.getResultList();

        // the following crappy algorithm checks that the interactors contained in the interactions
        // have the provided list of primaryIds
        for (IntactInteractionEvidence interaction : interactionsOfTheSameSize) {
            String[] primIdsToFind = new String[primaryIds.length];
            System.arraycopy(primaryIds, 0, primIdsToFind, 0, primaryIds.length);

            for (ParticipantEvidence component : interaction.getParticipants()) {
                for (Xref idXref : component.getInteractor().getIdentifiers()) {
                    for (int i=0; i<primIdsToFind.length; i++) {
                        if (idXref.getId().equals(primIdsToFind[i])) {
                            primIdsToFind[i] = "";
                        }
                    }
                }
            }

            boolean found = true;

            for (String id : primIdsToFind) {
                if (id.length() > 0) {
                    found = false;
                }
            }

            if (found) {
                results.add(interaction);
            }
        }

        return results;
    }

    public Collection<IntactInteractionEvidence> getByXrefLike(String primaryId) {
        Query query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                "join f.dbXrefs as x " +
                "where upper(x.id) like :primaryId");
        query.setParameter("primaryId","%"+primaryId.toUpperCase()+"%");
        return query.getResultList();
    }

    public Collection<IntactInteractionEvidence> getByXref(String dbName, String dbMI, String primaryId) {
        Query query;
        if (dbMI != null){
            query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                    "join f.dbXrefs as x " +
                    "join x.database as dat " +
                    "join dat.dbXrefs as xref " +
                    "join xref.database as d " +
                    "join xref.qualifier as q " +
                    "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                    "and d.shortName = :psimi " +
                    "and xref.id = :mi " +
                    "and x.id = :primary");
            query.setParameter("identity", Xref.IDENTITY);
            query.setParameter("secondaryAc", Xref.SECONDARY);
            query.setParameter("psimi", CvTerm.PSI_MI);
            query.setParameter("mi", dbMI);
            query.setParameter("primary", primaryId);
        }
        else{
            query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                    "join f.dbXrefs as x " +
                    "join x.database as d " +
                    "where d.shortName = :dbName " +
                    "and x.id = :primary");
            query.setParameter("dbName", dbName);
            query.setParameter("primary", primaryId);
        }
        return query.getResultList();
    }

    public Collection<IntactInteractionEvidence> getByXrefLike(String dbName, String dbMI, String primaryId) {
        Query query;
        if (dbMI != null){
            query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                    "join f.dbXrefs as x " +
                    "join x.database as dat " +
                    "join dat.dbXrefs as xref " +
                    "join xref.database as d " +
                    "join xref.qualifier as q " +
                    "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                    "and d.shortName = :psimi " +
                    "and xref.id = :mi " +
                    "and upper(x.id) like :primary");
            query.setParameter("identity", Xref.IDENTITY);
            query.setParameter("secondaryAc", Xref.SECONDARY);
            query.setParameter("psimi", CvTerm.PSI_MI);
            query.setParameter("mi", dbMI);
            query.setParameter("primary", "%"+primaryId.toUpperCase()+"%");
        }
        else{
            query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                    "join f.dbXrefs as x " +
                    "join x.database as d " +
                    "where d.shortName = :dbName " +
                    "and upper(x.id) like :primary");
            query.setParameter("dbName", dbName);
            query.setParameter("primary", "%"+primaryId.toUpperCase()+"%");
        }
        return query.getResultList();
    }

    public Collection<IntactInteractionEvidence> getByXref(String dbName, String dbMI, String primaryId, String qualifierName, String qualifierMI) {
        Query query;
        if (dbMI != null){
            if (qualifierName == null && qualifierMI == null){
                query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                        "join f.dbXrefs as x " +
                        "join x.database as dat " +
                        "join dat.dbXrefs as xref " +
                        "join xref.database as d " +
                        "join xref.qualifier as q " +
                        "where x.qualifier is null " +
                        "and (q.shortName = :identity or q.shortName = :secondaryAc) " +
                        "and d.shortName = :psimi " +
                        "and xref.id = :mi " +
                        "and x.id = :primary");
                query.setParameter("identity", Xref.IDENTITY);
                query.setParameter("secondaryAc", Xref.SECONDARY);
                query.setParameter("psimi", CvTerm.PSI_MI);
                query.setParameter("mi", dbMI);
                query.setParameter("primary", primaryId);
            }
            else if (qualifierMI != null){
                query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                        "join f.dbXrefs as x " +
                        "join x.database as dat " +
                        "join dat.dbXrefs as xref " +
                        "join x.qualifier as qual " +
                        "join qual.dbXrefs as xref2 " +
                        "join xref.database as d " +
                        "join xref.qualifier as q " +
                        "join xref2.database as d2 " +
                        "join xref2.qualifier as q2 " +
                        "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                        "and d.shortName = :psimi " +
                        "and xref.id = :mi "+
                        "and (q2.shortName = :identity or q2.shortName = :secondaryAc) " +
                        "and d2.shortName = :psimi " +
                        "and xref2.id = :mi2 " +
                        "and x.id = :primary");
                query.setParameter("identity", Xref.IDENTITY);
                query.setParameter("secondaryAc", Xref.SECONDARY);
                query.setParameter("psimi", CvTerm.PSI_MI);
                query.setParameter("mi", dbMI);
                query.setParameter("mi2", qualifierMI);
                query.setParameter("primary", primaryId);
            }
            else{
                query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                        "join f.dbXrefs as x " +
                        "join x.database as dat " +
                        "join x.qualifier as qual " +
                        "join dat.dbXrefs as xref " +
                        "join xref.database as d " +
                        "join xref.qualifier as q " +
                        "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                        "and d.shortName = :psimi " +
                        "and xref.id = :mi " +
                        "and qual.shortName = :qName " +
                        "and x.id = :primary");
                query.setParameter("identity", Xref.IDENTITY);
                query.setParameter("secondaryAc", Xref.SECONDARY);
                query.setParameter("psimi", CvTerm.PSI_MI);
                query.setParameter("mi", dbMI);
                query.setParameter("qName", qualifierName);
                query.setParameter("primary", primaryId);
            }
        }
        else{
            if (qualifierName == null && qualifierMI == null){
                query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                        "join f.dbXrefs as x " +
                        "join x.database as d " +
                        "where d.shortName = :dbName " +
                        "and x.qualifier is null " +
                        "and x.id = :primary");
                query.setParameter("dbName", dbName);
                query.setParameter("primary", primaryId);
            }
            else if (qualifierMI != null){
                query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                        "join f.dbXrefs as x " +
                        "join x.database as dat " +
                        "join x.qualifier as qual " +
                        "join qual.dbXrefs as xref " +
                        "join xref.database as d " +
                        "join xref.qualifier as q " +
                        "where dat.shortName = :dbName " +
                        "and (q.shortName = :identity or q.shortName = :secondaryAc) " +
                        "and d.shortName = :psimi " +
                        "and xref.id = :mi " +
                        "and x.id = :primary");
                query.setParameter("dbName", dbName);
                query.setParameter("identity", Xref.IDENTITY);
                query.setParameter("secondaryAc", Xref.SECONDARY);
                query.setParameter("psimi", CvTerm.PSI_MI);
                query.setParameter("mi", qualifierMI);
                query.setParameter("primary", primaryId);
            }
            else{
                query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                        "join f.dbXrefs as x " +
                        "join x.database as d " +
                        "join x.qualifier as q " +
                        "where d.shortName = :dbName " +
                        "and q.shortName = :qName " +
                        "and x.id = :primary");
                query.setParameter("dbName", dbName);
                query.setParameter("qName", qualifierName);
                query.setParameter("primary", primaryId);
            }
        }
        return query.getResultList();
    }

    public Collection<IntactInteractionEvidence> getByXrefLike(String dbName, String dbMI, String primaryId, String qualifierName, String qualifierMI) {
        Query query;
        if (dbMI != null){
            if (qualifierName == null && qualifierMI == null){
                query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                        "join f.dbXrefs as x " +
                        "join x.database as dat " +
                        "join dat.dbXrefs as xref " +
                        "join xref.database as d " +
                        "join xref.qualifier as q " +
                        "where x.qualifier is null " +
                        "and (q.shortName = :identity or q.shortName = :secondaryAc) " +
                        "and d.shortName = :psimi " +
                        "and xref.id = :mi " +
                        "and upper(x.id) like :primary");
                query.setParameter("identity", Xref.IDENTITY);
                query.setParameter("secondaryAc", Xref.SECONDARY);
                query.setParameter("psimi", CvTerm.PSI_MI);
                query.setParameter("mi", dbMI);
                query.setParameter("primary", "%"+primaryId.toUpperCase()+"%");
            }
            else if (qualifierMI != null){
                query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                        "join f.dbXrefs as x " +
                        "join x.database as dat " +
                        "join dat.dbXrefs as xref " +
                        "join x.qualifier as qual " +
                        "join qual.dbXrefs as xref2 " +
                        "join xref.database as d " +
                        "join xref.qualifier as q " +
                        "join xref2.database as d2 " +
                        "join xref2.qualifier as q2 " +
                        "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                        "and d.shortName = :psimi " +
                        "and xref.id = :mi "+
                        "and (q2.shortName = :identity or q2.shortName = :secondaryAc) " +
                        "and d2.shortName = :psimi " +
                        "and xref2.id = :mi2 " +
                        "and upper(x.id) like :primary");
                query.setParameter("identity", Xref.IDENTITY);
                query.setParameter("secondaryAc", Xref.SECONDARY);
                query.setParameter("psimi", CvTerm.PSI_MI);
                query.setParameter("mi", dbMI);
                query.setParameter("mi2", qualifierMI);
                query.setParameter("primary", "%"+primaryId.toUpperCase()+"%");
            }
            else{
                query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                        "join f.dbXrefs as x " +
                        "join x.database as dat " +
                        "join x.qualifier as qual " +
                        "join dat.dbXrefs as xref " +
                        "join xref.database as d " +
                        "join xref.qualifier as q " +
                        "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                        "and d.shortName = :psimi " +
                        "and xref.id = :mi " +
                        "and qual.shortName = :qName " +
                        "and upper(x.id) like :primary");
                query.setParameter("identity", Xref.IDENTITY);
                query.setParameter("secondaryAc", Xref.SECONDARY);
                query.setParameter("psimi", CvTerm.PSI_MI);
                query.setParameter("mi", dbMI);
                query.setParameter("qName", qualifierName);
                query.setParameter("primary", "%"+primaryId.toUpperCase()+"%");
            }
        }
        else{
            if (qualifierName == null && qualifierMI == null){
                query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f " +
                        "join f.dbXrefs as x " +
                        "join x.database as d " +
                        "where d.shortName = :dbName " +
                        "and x.qualifier is null " +
                        "and upper(x.id) like :primary");
                query.setParameter("dbName", dbName);
                query.setParameter("primary", "%"+primaryId.toUpperCase()+"%");
            }
            else if (qualifierMI != null){
                query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                        "join f.dbXrefs as x " +
                        "join x.database as dat " +
                        "join x.qualifier as qual " +
                        "join qual.dbXrefs as xref " +
                        "join xref.database as d " +
                        "join xref.qualifier as q " +
                        "where dat.shortName = :dbName " +
                        "and (q.shortName = :identity or q.shortName = :secondaryAc) " +
                        "and d.shortName = :psimi " +
                        "and xref.id = :mi " +
                        "and upper(x.id) like :primary");
                query.setParameter("dbName", dbName);
                query.setParameter("identity", Xref.IDENTITY);
                query.setParameter("secondaryAc", Xref.SECONDARY);
                query.setParameter("psimi", CvTerm.PSI_MI);
                query.setParameter("mi", qualifierMI);
                query.setParameter("primary", "%"+primaryId.toUpperCase()+"%");
            }
            else{
                query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                        "join f.dbXrefs as x " +
                        "join x.database as d " +
                        "join x.qualifier as q " +
                        "where d.shortName = :dbName " +
                        "and q.shortName = :qName " +
                        "and upper(x.id) like :primary");
                query.setParameter("dbName", dbName);
                query.setParameter("qName", qualifierName);
                query.setParameter("primary", "%"+primaryId.toUpperCase()+"%");
            }
        }
        return query.getResultList();
    }

    public Collection<IntactInteractionEvidence> getByAnnotationTopic(String topicName, String topicMI) {
        Query query;
        if (topicMI != null){
            query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                    "join f.dbAnnotations as a " +
                    "join a.topic as t " +
                    "join t.dbXrefs as xref " +
                    "join xref.database as d " +
                    "join xref.qualifier as q " +
                    "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                    "and d.shortName = :psimi " +
                    "and xref.id = :mi");
            query.setParameter("identity", Xref.IDENTITY);
            query.setParameter("secondaryAc", Xref.SECONDARY);
            query.setParameter("psimi", CvTerm.PSI_MI);
            query.setParameter("mi", topicMI);
        }
        else{
            query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                    "join f.dbAnnotations as a " +
                    "join a.topic as t " +
                    "where t.shortName = :topicName");
            query.setParameter("topicName", topicName);
        }
        return query.getResultList();
    }

    public Collection<IntactInteractionEvidence> getByAnnotationTopicAndValue(String topicName, String topicMI, String value) {
        Query query;
        if (topicMI != null){
            query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                    "join f.dbAnnotations as a " +
                    "join a.topic as t " +
                    "join t.dbXrefs as xref " +
                    "join xref.database as d " +
                    "join xref.qualifier as q " +
                    "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                    "and d.shortName = :psimi " +
                    "and xref.id = :mi"+(value != null ? " and a.value = :annotValue" : ""));
            query.setParameter("identity", Xref.IDENTITY);
            query.setParameter("secondaryAc", Xref.SECONDARY);
            query.setParameter("psimi", CvTerm.PSI_MI);
            query.setParameter("mi", topicMI);
            if (value != null){
                query.setParameter("annotValue", value);
            }
        }
        else{
            query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                    "join f.dbAnnotations as a " +
                    "join a.topic as t " +
                    "where t.shortName = :topicName"+(value != null ? " and a.value = :annotValue" : ""));
            query.setParameter("topicName", topicName);
            if (value != null){
                query.setParameter("annotValue", value);
            }
        }
        return query.getResultList();
    }

    public Collection<IntactInteractionEvidence> getByInteractionType(String typeName, String typeMI, int first, int max) {
        Query query;
        if (typeName == null && typeMI == null){
            query = getEntityManager().createQuery("select f from IntactInteractionEvidence f "  +
                    "where f.interactionType is null order by f.ac");
        }
        else if (typeMI != null){
            query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                    "join f.interactionType as t " +
                    "join t.dbXrefs as xref " +
                    "join xref.database as d " +
                    "join xref.qualifier as q " +
                    "where (q.shortName = :identity or q.shortName = :secondaryAc) " +
                    "and d.shortName = :psimi " +
                    "and xref.id = :mi order by f.ac");
            query.setParameter("identity", Xref.IDENTITY);
            query.setParameter("secondaryAc", Xref.SECONDARY);
            query.setParameter("psimi", CvTerm.PSI_MI);
            query.setParameter("mi", typeMI);
        }
        else{
            query = getEntityManager().createQuery("select f from IntactInteractionEvidence f "  +
                    "join f.interactionType as t " +
                    "where t.shortName = :typeName");
            query.setParameter("typeName", typeName);
        }
        return query.getResultList();
    }

    public Collection<IntactInteractionEvidence> getByExperimentAc(String ac) {
        Query query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                "join f.dbExperiments as e " +
                "where e.ac = :expAc");
        query.setParameter("expAc",ac);
        return query.getResultList();
    }

    public Collection<IntactInteractionEvidence> getByConfidence(String typeName, String typeMI, String value) {
        Query query;
        if (typeName == null && typeMI == null){
            query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                    "where f.confidences is empty order by f.ac");
        }
        else if (typeMI != null){
            query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
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
            query = getEntityManager().createQuery("select distinct f from IntactInteractionEvidence f "  +
                    "join f.confidences as c " +
                    "join c.type as t " +
                    "where t.shortName = :name "  + (value != null ? " and c.value = :confValue ":" ")+
                    "order by f.ac");
            query.setParameter("name", typeName);
            if (value != null){
                query.setParameter("confValue", value);
            }
        }
        return query.getResultList();
    }

    public Collection<IntactInteractionEvidence> getByParameterType(String typeName, String typeMI) {
        Query query;
        if (typeMI != null){
            query = getEntityManager().createQuery("select distinct i from IntactInteractionEvidence i " +
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
            query = getEntityManager().createQuery("select distinct i from IntactInteractionEvidence i " +
                    "join i.parameters as p " +
                    "join p.type as t " +
                    "where t.shortName = :paramName");
            query.setParameter("paramName", typeName);
        }
        return query.getResultList();
    }

    public Collection<IntactInteractionEvidence> getByParameterUnit(String unitName, String unitMI) {
        Query query;
        if (unitMI == null && unitName == null){
            query = getEntityManager().createQuery("select distinct i from IntactInteractionEvidence i " +
                    "join i.parameters as p " +
                    "where p.unit is null");
        }
        else if (unitMI != null){
            query = getEntityManager().createQuery("select distinct i from IntactInteractionEvidence i " +
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
            query = getEntityManager().createQuery("select distinct i from IntactInteractionEvidence i " +
                    "join i.parameters as p " +
                    "join p.unit as u " +
                    "where u.shortName = :unitName");
            query.setParameter("unitName", unitName);
        }
        return query.getResultList();
    }

    public Collection<IntactInteractionEvidence> getByParameterTypeAndUnit(String typeName, String typeMI, String unitName, String unitMI) {
        Query query;
        if (typeMI != null){
            if (unitMI == null && unitName == null){
                query = getEntityManager().createQuery("select distinct i from IntactInteractionEvidence i " +
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
                query = getEntityManager().createQuery("select distinct i from IntactInteractionEvidence i " +
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
                query = getEntityManager().createQuery("select distinct i from IntactInteractionEvidence i " +
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
                query = getEntityManager().createQuery("select distinct i from IntactInteractionEvidence i " +
                        "join i.parameters as p " +
                        "join p.type as t " +
                        "where t.shortName = :confName " +
                        "and p.unit is null");
                query.setParameter("typeName", typeName);
            }
            else if (unitMI != null){
                query = getEntityManager().createQuery("select distinct i from IntactInteractionEvidence i " +
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
                query = getEntityManager().createQuery("select distinct i from IntactInteractionEvidence i " +
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

    @Override
    public int countParticipantsForInteraction(String ac) {
        Query query = getEntityManager().createQuery("select size(i.participants) from IntactInteractionEvidence i " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return (Integer)query.getSingleResult();
    }

    @Override
    public Collection<Xref> getXrefsForInteraction(String ac) {
        Query query = getEntityManager().createQuery("select x from IntactInteractionEvidence i " +
                "join i.dbXrefs as x " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return query.getResultList();
    }

    @Override
    public Collection<Annotation> getAnnotationsForInteraction(String ac) {
        Query query = getEntityManager().createQuery("select a from IntactInteractionEvidence i " +
                "join i.dbAnnotations as a " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return query.getResultList();
    }

    @Override
    public Collection<Confidence> getConfidencesForInteraction(String ac) {
        Query query = getEntityManager().createQuery("select x from IntactInteractionEvidence i " +
                "join i.confidences as x " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return query.getResultList();
    }

    @Override
    public Collection<Parameter> getParametersForInteraction(String ac) {
        Query query = getEntityManager().createQuery("select x from IntactInteractionEvidence i " +
                "join i.parameters as x " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return query.getResultList();
    }

    @Override
    public int countConfidencesForInteraction(String ac) {
        Query query = getEntityManager().createQuery("select size(i.confidences) from IntactInteractionEvidence i " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return (Integer)query.getSingleResult();
    }

    @Override
    public int countParametersForInteraction(String ac) {
        Query query = getEntityManager().createQuery("select size(i.parameters) from IntactInteractionEvidence i " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return (Integer)query.getSingleResult();
    }

    @Override
    public int countXrefsForInteraction(String ac) {
        Query query = getEntityManager().createQuery("select size(i.dbXrefs) from IntactInteractionEvidence i " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return (Integer)query.getSingleResult();
    }

    @Override
    public int countAnnotationsForInteraction(String ac) {
        Query query = getEntityManager().createQuery("select size(i.dbAnnotations) from IntactInteractionEvidence i " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return (Integer)query.getSingleResult();
    }

    @Override
    public int countVariableParameterValuesSetsForInteraction(String ac) {
        Query query = getEntityManager().createQuery("select size(i.variableParameterValues) from IntactInteractionEvidence i " +
                "where i.ac = :ac");
        query.setParameter("ac", ac);
        return (Integer)query.getSingleResult();
    }

    @Override
    public int countInteractionsInvolvingInteractor(String ac) {
        Query query = getEntityManager().createQuery("select count(distinct i.ac) from IntactInteractionEvidence i " +
                "join i.participants as p join p.interactor as inter " +
                "where inter.ac = :ac");
        query.setParameter("ac", ac);
        Long results = (Long)query.getSingleResult();
        return results != null ? results.intValue() : 0;
    }
}
