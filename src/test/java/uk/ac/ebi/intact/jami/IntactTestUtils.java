package uk.ac.ebi.intact.jami;

import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.model.impl.*;
import psidev.psi.mi.jami.utils.*;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.model.lifecycle.AbstractLifeCycleEvent;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleEventType;
import uk.ac.ebi.intact.jami.model.user.Preference;
import uk.ac.ebi.intact.jami.model.user.Role;
import uk.ac.ebi.intact.jami.model.user.User;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

/**
 * Utility class for testing
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/04/14</pre>
 */

public class IntactTestUtils {

    public static <T extends AbstractIntactAlias> T createIntactAlias(Class<T> aliasClass, String typeName, String typeMI, String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        T alias = aliasClass.getConstructor(String.class).newInstance(name);
        if (typeName != null){
            alias.setType(IntactUtils.createMIAliasType(typeName, typeMI));
        }
        return alias;
    }

    public static <T extends AbstractIntactAlias> T createAliasNoType(Class<T> aliasClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createAliasNoType(aliasClass, "test synonym 2");
    }

    public static <T extends AbstractIntactAlias> T createAliasNoType(Class<T> aliasClass, String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createIntactAlias(aliasClass, null, null, name);
    }

    public static <T extends AbstractIntactAlias> T createAliasSynonym(Class<T> aliasClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createAliasSynonym(aliasClass, "test synonym");
    }

    public static <T extends AbstractIntactAlias> T createAliasSynonym(Class<T> aliasClass, String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createIntactAlias(aliasClass, Alias.SYNONYM, Alias.SYNONYM_MI, name);
    }

    public static Alias createAliasNoType() {
        return new DefaultAlias("test synonym 2");
    }

    public static Alias createAliasNoType(String name) {
        return new DefaultAlias(name);
    }

    public static Alias createAliasSynonym() {
        return AliasUtils.createAlias(Alias.SYNONYM, Alias.SYNONYM_MI,"test synonym");
    }

    public static Alias createAliasSynonym(String name) {
        return AliasUtils.createAlias(Alias.SYNONYM, Alias.SYNONYM_MI, name);
    }

    public static <T extends AbstractIntactAnnotation> T createIntactAnnotation(Class<T> annotationClass, String topicName, String topicMI, String description) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        return annotationClass.getConstructor(CvTerm.class, String.class).
                newInstance(IntactUtils.createMITopic(topicName, topicMI), description);
    }

    public static <T extends AbstractIntactAnnotation> T createAnnotationNoDescription(Class<T> annotationClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createAnnotationNoDescription(annotationClass, Annotation.CAUTION, Annotation.CAUTION_MI);
    }

    public static <T extends AbstractIntactAnnotation> T createAnnotationNoDescription(Class<T> annotationClass, String topicName, String topicMI) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createIntactAnnotation(annotationClass, topicName, topicMI, null);
    }

    public static <T extends AbstractIntactAnnotation> T createAnnotationComment(Class<T> annotationClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createIntactAnnotation(annotationClass, Annotation.COMMENT, Annotation.COMMENT_MI, "test comment");
    }

    public static <T extends AbstractIntactAnnotation> T createAnnotationComment(Class<T> annotationClass, String description) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createIntactAnnotation(annotationClass, Annotation.COMMENT, Annotation.COMMENT_MI, description);
    }

    public static Annotation createAnnotationNoDescription() {
        return AnnotationUtils.createCaution(null);
    }

    public static Annotation createAnnotationNoDescription(String topicName, String topicMI) {
        return AnnotationUtils.createAnnotation(topicName, topicMI, null);
    }

    public static Annotation createAnnotationComment() {
        return AnnotationUtils.createComment("test comment");
    }

    public static Annotation createAnnotationComment(String description) {
        return AnnotationUtils.createComment(description);
    }

    public static <T extends AbstractIntactConfidence> T createIntactConfidence(Class<T> confidenceClass, String typeName, String typeMI, String value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        return confidenceClass.getConstructor(CvTerm.class, String.class).
                newInstance(IntactUtils.createMIConfidenceType(typeName, typeMI), value);
    }

    public static <T extends AbstractIntactConfidence> T createConfidenceAuthorScore(Class<T> annotationClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createIntactConfidence(annotationClass, "author-score", "MI:xxx1", "high");
    }

    public static <T extends AbstractIntactConfidence> T createConfidenceAuthorScore(Class<T> annotationClass, String value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createIntactConfidence(annotationClass, "author-score", "MI:xxx1", value);
    }

    public static Confidence createConfidenceAuthorScore() {
        return createConfidenceAuthorScore("high");
    }

    public static Confidence createConfidenceAuthorScore(String value) {
        return ConfidenceUtils.createConfidence("author-score", "MI:xxx1", value);
    }

    public static <T extends AbstractIntactXref> T createIntactXref(Class<T> xrefClass, String dbName, String dbMI,
                                                                    String dbId, String qualifierName, String qualifierId)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        T xref = xrefClass.getConstructor(CvTerm.class, String.class).newInstance(IntactUtils.createMIDatabase(dbName, dbMI),dbId);
        if (qualifierName != null){
            xref.setQualifier(IntactUtils.createMIQualifier(qualifierName, qualifierId));
        }
        return xref;
    }

    public static <T extends AbstractIntactXref> T createPubmedXrefNoQualifier(Class<T> xrefClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createPubmedXrefNoQualifier(xrefClass, "12345");
    }

    public static <T extends AbstractIntactXref> T createPubmedXrefNoQualifier(Class<T> xrefClass, String id) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createIntactXref(xrefClass, Xref.PUBMED, Xref.PUBMED_MI, id, null, null);
    }

    public static <T extends AbstractIntactXref> T createXrefSeeAlso(Class<T> xrefClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createXrefSeeAlso(xrefClass, "IM-1-1");
    }

    public static <T extends AbstractIntactXref> T createXrefSeeAlso(Class<T> xrefClass, String id) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createIntactXref(xrefClass, Xref.IMEX, Xref.IMEX_MI, id, Xref.SEE_ALSO, Xref.SEE_ALSO_MI);
    }

    public static Xref createPubmedXrefNoQualifier() {
        return XrefUtils.createXref(Xref.PUBMED, Xref.PUBMED_MI, "12345");
    }

    public static Xref createPubmedXrefNoQualifier(String id) {
        return XrefUtils.createXref(Xref.PUBMED, Xref.PUBMED_MI, id);
    }

    public static Xref createXrefSeeAlso() {
        return XrefUtils.createXrefWithQualifier(Xref.IMEX, Xref.IMEX_MI, "IM-1-1", Xref.SEE_ALSO, Xref.SEE_ALSO_MI);
    }

    public static Xref createXrefSeeAlso(String id) {
        return XrefUtils.createXrefWithQualifier(Xref.IMEX, Xref.IMEX_MI, id, Xref.SEE_ALSO, Xref.SEE_ALSO_MI);
    }

    public static <T extends AbstractIntactParameter> T createIntactParameter(Class<T> parameterClass, String typeName, String typeMI,
                                                                              int factor, String unitName, String unitMI)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        T param = parameterClass.getConstructor(CvTerm.class, ParameterValue.class).newInstance(
                IntactUtils.createMIParameterType(typeName, typeMI),new ParameterValue(new BigDecimal(factor)));
        if (unitName != null){
            param.setUnit(IntactUtils.createMIUnit(unitName, unitMI));
        }
        return param;
    }

    public static <T extends AbstractIntactParameter> T createKdParameterNoUnit(Class<T> parameterClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createParameterNoUnit(parameterClass, "kd", "MI:xxx1", 3);
    }

    public static <T extends AbstractIntactParameter> T createParameterNoUnit(Class<T> parameterClass, String typeName, String typeId, int factor) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createIntactParameter(parameterClass, typeName, typeId, factor, null, null);
    }

    public static <T extends AbstractIntactParameter> T createKdParameter(Class<T> parameterClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createKdParameter(parameterClass, 5, "molar", "MI:xxx3");
    }

    public static <T extends AbstractIntactParameter> T createKdParameter(Class<T> parameterClass, int factor, String unitName, String unitMI) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createIntactParameter(parameterClass, "kd", "MI:xxx1", factor, unitName, unitMI);
    }

    public static Parameter createKdParameterNoUnit() {
        return new DefaultParameter(CvTermUtils.createMICvTerm("kd", "MI:xxx1"), new ParameterValue(new BigDecimal(3)));
    }

    public static ModelledParameter createParameterNoUnit(String typeName, String typeId, int factor) {
        return new DefaultModelledParameter(CvTermUtils.createMICvTerm(typeName, typeId), new ParameterValue(new BigDecimal(factor)));
    }

    public static Parameter createKdParameter() {
        return new DefaultParameter(CvTermUtils.createMICvTerm("kd", "MI:xxx1"), new ParameterValue(new BigDecimal(5)),
                CvTermUtils.createMICvTerm("molar", "MI:xxx3"));
    }

    public static Parameter createKdParameter(int factor, String unitName, String unitMI) {
        return new DefaultParameter(CvTermUtils.createMICvTerm("kd", "MI:xxx1"), new ParameterValue(new BigDecimal(factor)),
                CvTermUtils.createMICvTerm(unitName, unitMI));
    }

    public static Preference createPreference() {
        return new Preference("key","value");
    }

    public static Role createCuratorRole() {
        return new Role("CURATOR");
    }

    public static User createCuratorUser(){
        User user = new User("default", "firstName", "lastName", "name@ebi.ac.uk");
        user.getPreferences().add(new Preference("key", "value"));
        user.getRoles().add(new Role("CURATOR"));
        return user;
    }

    public static IntactCvTerm createCvTermWithXrefs(){
        return IntactUtils.createMIAliasType(Alias.GENE_NAME+ " ", Alias.GENE_NAME_MI);
    }

    public static IntactCvTerm createCvTermWithParent(){
        IntactCvTerm annotationTopic = IntactUtils.createMITopic("teST", null);
        IntactCvTerm annotationTopicParent = IntactUtils.createMITopic(Annotation.CAUTION, Annotation.CAUTION_MI);
        annotationTopic.addParent(annotationTopicParent);
        return annotationTopic;
    }

    public static IntactCvTerm createCvTermWithAnnotationsAndAliases() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        IntactCvTerm cvDatabase = IntactUtils.createMIDatabase("teST2", null);
        cvDatabase.getAnnotations().add(createAnnotationComment(CvTermAnnotation.class));
        cvDatabase.getSynonyms().add(createAliasSynonym(CvTermAlias.class));
        cvDatabase.setObjClass(IntactUtils.DATABASE_OBJCLASS);
        return cvDatabase;
    }

    public static IntactCvTerm createCvWithDefinition(){
        IntactCvTerm cvConfidenceType = IntactUtils.createMIConfidenceType("test3", null);
        cvConfidenceType.setFullName("Test Confidence");
        cvConfidenceType.setDefinition("Test Definition");
        cvConfidenceType.setObjClass(null);
        return cvConfidenceType;
    }

    public static <T extends AbstractIntactRange> T createIntactRange(Class<T> rangeClass, int start, int end, ResultingSequence seq) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        T range = rangeClass.getConstructor(Position.class, Position.class).newInstance(new IntactPosition(start), new IntactPosition(end));
        if (seq != null){
            range.setResultingSequence(seq);
        }
        return range;
    }

    public static <T extends AbstractIntactRange> T createCertainRangeNoResultingSequence(Class<T> rangeClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createIntactRange(rangeClass, 1, 2, null);
    }

    public static ExperimentalRange createExperimentalRangeWithResultingSequence() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createIntactRange(ExperimentalRange.class, 1,2, new ExperimentalResultingSequence("AAGA", "ACGA"));
    }

    public static ModelledRange createModelledRangeWithResultingSequence() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createIntactRange(ModelledRange.class, 1,2, new ModelledResultingSequence("AAGA", "ACGA"));
    }

    public static Range createCertainRangeNoResultingSequence() {
        return RangeUtils.createCertainRange(1,2);
    }

    public static Range createCertainRangeWithResultingSequence() {
        Range range = createCertainRangeNoResultingSequence();
        range.setResultingSequence(new DefaultResultingSequence("AAGA", "ACGA"));
        return range;
    }

    public static <T extends AbstractIntactFeature> T createIntactFeature(Class<T> featureClass, String type, String typeMI) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        T feature = featureClass.getConstructor(String.class, String.class).newInstance("test feature", "full test feature");
        T feature2 = featureClass.getConstructor(String.class, String.class).newInstance("test feature 2", "full test feature 2");
        if (type != null){
            feature.setType(IntactUtils.createMIFeatureType(type, typeMI));
            feature2.setType(IntactUtils.createMIFeatureType(type, typeMI));
        }
        feature.getLinkedFeatures().add(feature2);
        feature.setRole(IntactUtils.createMITopic("interaction effect", null));
        return feature;
    }

    public static IntactModelledFeature createFullModelledFeatureWithRanges(String type, String typeMI) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        IntactModelledFeature feature = new IntactModelledFeature("test feature", "full test feature");
        IntactModelledFeature feature2 = new IntactModelledFeature("test feature 2", "full test feature 2");
        if (type != null){
            feature.setType(IntactUtils.createMIFeatureType(type, typeMI));
            feature2.setType(IntactUtils.createMIFeatureType(type, typeMI));
        }
        feature.getLinkedFeatures().add(feature2);
        feature.getRanges().add(createCertainRangeNoResultingSequence(ModelledRange.class));
        feature.getAliases().add(createAliasSynonym(ModelledFeatureAlias.class));
        feature.getXrefs().add(createPubmedXrefNoQualifier(ModelledFeatureXref.class));
        feature.getAnnotations().add(createAnnotationComment(ModelledFeatureAnnotation.class));
        feature.setRole(IntactUtils.createMITopic("interaction effect", null));
        return feature;
    }

    public static IntactFeatureEvidence createFullFeatureEvidenceWithRanges(String type, String typeMI) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        IntactFeatureEvidence feature = new IntactFeatureEvidence("test feature", "full test feature");
        IntactFeatureEvidence feature2 = new IntactFeatureEvidence("test feature 2", "full test feature 2");
        if (type != null){
            feature.setType(IntactUtils.createMIFeatureType(type, typeMI));
            feature2.setType(IntactUtils.createMIFeatureType(type, typeMI));
        }
        feature.getLinkedFeatures().add(feature2);
        feature.getRanges().add(createCertainRangeNoResultingSequence(ExperimentalRange.class));
        feature.getAliases().add(createAliasSynonym(FeatureEvidenceAlias.class));
        feature.getXrefs().add(createPubmedXrefNoQualifier(FeatureEvidenceXref.class));
        feature.getAnnotations().add(createAnnotationComment(FeatureEvidenceAnnotation.class));
        feature.setRole(IntactUtils.createMITopic("interaction effect", null));
        feature.getDetectionMethods().add(IntactUtils.createMIFeatureDetectionMethod("feature detection", null));
        feature.getDetectionMethods().add(IntactUtils.createMIFeatureDetectionMethod("feature detection 2", null));
        return feature;
    }

    public static Feature createBasicFeature(String type, String typeMI) {
        Feature feature = new DefaultFeature("test feature", "full test feature");
        Feature feature2 = new DefaultFeature("test feature 2", "full test feature 2");
        if (type != null){
            feature.setType(CvTermUtils.createMICvTerm(type, typeMI));
            feature2.setType(CvTermUtils.createMICvTerm(type, typeMI));
        }
        feature.getLinkedFeatures().add(feature2);
        feature.setRole(CvTermUtils.createMICvTerm("interaction effect",null));
        return feature;
    }

    public static ModelledFeature createBasicModelledFeature(String type, String typeMI) {
        ModelledFeature feature = new DefaultModelledFeature("test feature", "full test feature");
        ModelledFeature feature2 = new DefaultModelledFeature("test feature 2", "full test feature 2");
        if (type != null){
            feature.setType(CvTermUtils.createMICvTerm(type, typeMI));
            feature2.setType(CvTermUtils.createMICvTerm(type, typeMI));
        }
        feature.getLinkedFeatures().add(feature2);
        feature.setRole(CvTermUtils.createMICvTerm("interaction effect",null));
        return feature;
    }

    public static FeatureEvidence createBasicFeatureEvidence(String type, String typeMI) {
        FeatureEvidence feature = new DefaultFeatureEvidence("test feature", "full test feature");
        FeatureEvidence feature2 = new DefaultFeatureEvidence("test feature 2", "full test feature 2");
        if (type != null){
            feature.setType(CvTermUtils.createMICvTerm(type, typeMI));
            feature2.setType(CvTermUtils.createMICvTerm(type, typeMI));
        }
        feature.getLinkedFeatures().add(feature2);
        feature.setRole(CvTermUtils.createMICvTerm("interaction effect",null));
        feature.getDetectionMethods().add(IntactUtils.createMIFeatureDetectionMethod("feature detection", null));
        feature.getDetectionMethods().add(IntactUtils.createMIFeatureDetectionMethod("feature detection 2", null));
        return feature;
    }

    public static ModelledFeature createFullModelledFeature(String type, String typeMI) {
        ModelledFeature feature = new DefaultModelledFeature("test feature", "full test feature");
        ModelledFeature feature2 = new DefaultModelledFeature("test feature 2", "full test feature 2");
        if (type != null){
            feature.setType(CvTermUtils.createMICvTerm(type, typeMI));
            feature2.setType(CvTermUtils.createMICvTerm(type, typeMI));
        }
        feature.getLinkedFeatures().add(feature2);
        feature.getRanges().add(createCertainRangeNoResultingSequence());
        feature.getAliases().add(createAliasSynonym());
        feature.getXrefs().add(createPubmedXrefNoQualifier());
        feature.getAnnotations().add(createAnnotationComment());
        feature.setRole(CvTermUtils.createMICvTerm("interaction effect",null));
        return feature;
    }

    public static FeatureEvidence createFullFeatureEvidence(String type, String typeMI) {
        FeatureEvidence feature = new DefaultFeatureEvidence("test feature", "full test feature");
        FeatureEvidence feature2 = new DefaultFeatureEvidence("test feature 2", "full test feature 2");
        if (type != null){
            feature.setType(CvTermUtils.createMICvTerm(type, typeMI));
            feature2.setType(CvTermUtils.createMICvTerm(type, typeMI));
        }
        feature.getLinkedFeatures().add(feature2);
        feature.getRanges().add(createCertainRangeNoResultingSequence());
        feature.getAliases().add(createAliasSynonym());
        feature.getXrefs().add(createPubmedXrefNoQualifier());
        feature.getAnnotations().add(createAnnotationComment());
        feature.setRole(CvTermUtils.createMICvTerm("interaction effect",null));
        feature.getDetectionMethods().add(IntactUtils.createMIFeatureDetectionMethod("feature detection", null));
        feature.getDetectionMethods().add(IntactUtils.createMIFeatureDetectionMethod("feature detection 2", null));
        return feature;
    }

    public static IntactOrganism createIntactOrganismWithCellAndTissue() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        IntactOrganism organism = new IntactOrganism(9606);
        organism.setCommonName("human");
        organism.setScientificName("Homo Sapiens");
        organism.getAliases().add(createAliasSynonym(OrganismAlias.class));

        organism.setCellType(IntactUtils.createCellType("293t"));
        organism.setTissue(IntactUtils.createTissue("test tissue"));

        return organism;
    }

    public static IntactOrganism createIntactOrganism() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        IntactOrganism organism = new IntactOrganism(9606);
        organism.setCommonName("human");
        organism.setScientificName("Homo Sapiens");
        organism.getAliases().add(createAliasSynonym(OrganismAlias.class));

        return organism;
    }

    public static Organism createOrganism() {
        Organism organism = new DefaultOrganism(9606);
        organism.setCommonName("human");
        organism.setScientificName("Homo Sapiens");
        organism.getAliases().add(createAliasSynonym());

        organism.setCellType(new DefaultCvTerm("293t"));
        organism.setTissue(new DefaultCvTerm("test tissue"));

        return organism;
    }

    public static IntactSource createIntactSource() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        IntactSource source = new IntactSource("IntAct","MI:0469");
        source.setFullName("Molecular Interaction Database");
        source.getSynonyms().add(createAliasSynonym(SourceAlias.class));
        source.getXrefs().add(createXrefSeeAlso(SourceXref.class));
        source.setUrl("http://www.ebi.ac.uk/intact/");
        source.setPostalAddress("postalAddress");
        source.setPublication(new IntactPublication("12345"));
        return source;
    }

    public static Source createSource() {
        Source source = new DefaultSource("IntAct","MI:0469");
        source.setFullName("Molecular Interaction Database");
        source.getSynonyms().add(createAliasSynonym());
        source.getXrefs().add(createXrefSeeAlso());
        source.setUrl("http://www.ebi.ac.uk/intact/");
        source.setPostalAddress("postalAddress");
        source.setPublication(new DefaultPublication("12345"));
        return source;
    }

    public static <T extends AbstractLifeCycleEvent> T createIntactLifeCycleEvent(Class<T> lifecycleEventClass, LifeCycleEventType evtName, User user, String note)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        T evt = lifecycleEventClass.getConstructor(LifeCycleEventType.class, User.class, String.class).newInstance(evtName,
                user, note);
        return evt;
    }

    public static <T extends AbstractLifeCycleEvent> T createIntactNewLifeCycleEvent(Class<T> lifecycleEventClass)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return createIntactLifeCycleEvent(lifecycleEventClass, LifeCycleEventType.CREATED, createCuratorUser(), "new event");
    }

    public static <T extends IntactInteractor> T createIntactInteractor(Class<T> interactorClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        T interactor = interactorClass.getConstructor(String.class).newInstance("test interactor");
        interactor.setFullName("Full interactor name");
        interactor.getAliases().add(createAliasSynonym(InteractorAlias.class));
        interactor.getXrefs().add(IntactTestUtils.createPubmedXrefNoQualifier(InteractorXref.class, "123456"));
        interactor.getAnnotations().add(createAnnotationComment(InteractorAnnotation.class));
        interactor.setOrganism(createIntactOrganism());
        interactor.getChecksums().add(ChecksumUtils.createRogid("xxxx1"));

        return interactor;
    }

    public static <T extends Interactor> T createInteractor(Class<T> interactorClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        T interactor = interactorClass.getConstructor(String.class).newInstance("test interactor");
        interactor.setFullName("Full interactor name");
        interactor.getAliases().add(createAliasSynonym(InteractorAlias.class));
        interactor.getXrefs().add(IntactTestUtils.createPubmedXrefNoQualifier(InteractorXref.class, "123456"));
        interactor.getAnnotations().add(createAnnotationComment(InteractorAnnotation.class));
        interactor.setOrganism(createIntactOrganism());
        interactor.getChecksums().add(ChecksumUtils.createRogid("xxxx1"));

        return interactor;
    }

    public static IntactInteractor createDefaultIntactInteractor() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        return createIntactInteractor(IntactInteractor.class);
    }

    public static Interactor createDefaultInteractor() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return createInteractor(DefaultInteractor.class);
    }

    public static IntactPolymer createIntactPolymer() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        IntactPolymer interactor = createIntactInteractor(IntactPolymer.class);
        interactor.setSequence("AAAMGGCA");
        interactor.getChecksums().add(new DefaultChecksum(IntactUtils.createMITopic("crc64",null), "xxxx3"));

        return interactor;
    }

    public static Polymer createDefaultPolymer() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Polymer interactor = createInteractor(DefaultPolymer.class);
        interactor.setSequence("AAAMGGCA");
        interactor.getChecksums().add(new DefaultChecksum(IntactUtils.createMITopic("crc64",null), "xxxx3"));

        return interactor;
    }

    public static IntactInteractorPool createIntactInteractorPool() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        IntactInteractorPool interactor = createIntactInteractor(IntactInteractorPool.class);
        Interactor interactor1 = createDefaultIntactInteractor();
        interactor1.setShortName("interactor 1");
        Interactor interactor2 = createIntactPolymer();
        interactor2.setShortName("interactor 2");
        interactor.add(interactor1);
        interactor.add(interactor2);

        return interactor;
    }

    public static InteractorPool createInteractorPool() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        InteractorPool interactor = createInteractor(DefaultInteractorPool.class);

        Interactor interactor1 = createDefaultInteractor();
        interactor1.setShortName("interactor 1");
        Interactor interactor2 = createDefaultPolymer();
        interactor2.setShortName("interactor 2");
        interactor.add(interactor1);
        interactor.add(interactor2);

        return interactor;
    }

    public static ExperimentalCausalRelationship createExperimentalCausalRelationship() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ExperimentalCausalRelationship causal = new ExperimentalCausalRelationship(IntactUtils.createMITopic("increases", null), createIntactParticipantEvidence());

        return causal;
    }

    public static CausalRelationship createCausalRelationship() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        CausalRelationship causal = new DefaultCausalRelationship(new DefaultCvTerm("increases"), createParticipantEvidence());

        return causal;
    }


    public static IntactParticipantEvidence createIntactParticipantEvidence() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        IntactParticipantEvidence interactor = new IntactParticipantEvidence(createDefaultIntactInteractor());
        interactor.setStoichiometry(new IntactStoichiometry(2));
        interactor.getAliases().add(createAliasSynonym(ModelledParticipantAlias.class));
        interactor.getXrefs().add(IntactTestUtils.createPubmedXrefNoQualifier(ModelledParticipantXref.class, "123456"));
        interactor.getAnnotations().add(createAnnotationComment(ModelledParticipantAnnotation.class));
        interactor.getFeatures().add(createBasicFeatureEvidence("type", "MI:xxxx"));
        interactor.getCausalRelationships().add(new ExperimentalCausalRelationship(IntactUtils.createMITopic("increases", null), interactor));
        interactor.getFeatures().iterator().next().getLinkedFeatures().clear();

        return interactor;
    }

    public static ParticipantEvidence createParticipantEvidence() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        ParticipantEvidence interactor = new DefaultParticipantEvidence(createDefaultInteractor());
        interactor.setStoichiometry(new IntactStoichiometry(2));
        interactor.getAliases().add(createAliasSynonym(ModelledParticipantAlias.class));
        interactor.getXrefs().add(IntactTestUtils.createPubmedXrefNoQualifier(ModelledParticipantXref.class, "123456"));
        interactor.getAnnotations().add(createAnnotationComment(ModelledParticipantAnnotation.class));
        interactor.getFeatures().add(createBasicFeatureEvidence("type", "MI:xxxx"));
        interactor.getCausalRelationships().add(new ExperimentalCausalRelationship(IntactUtils.createMITopic("increases", null), interactor));
        interactor.getFeatures().iterator().next().getLinkedFeatures().clear();

        return interactor;
    }


    public static IntactModelledParticipant createIntactModelledParticipant() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        IntactModelledParticipant interactor = new IntactModelledParticipant(createDefaultIntactInteractor());
        interactor.setStoichiometry(new IntactStoichiometry(2));
        interactor.getAliases().add(createAliasSynonym(ModelledParticipantAlias.class));
        interactor.getXrefs().add(IntactTestUtils.createPubmedXrefNoQualifier(ModelledParticipantXref.class, "123456"));
        interactor.getAnnotations().add(createAnnotationComment(ModelledParticipantAnnotation.class));
        interactor.getFeatures().add(createBasicModelledFeature("type", "MI:xxxx"));
        interactor.getFeatures().iterator().next().getLinkedFeatures().clear();
        interactor.getCausalRelationships().add(new ModelledCausalRelationship(IntactUtils.createMITopic("increases", null), interactor));

        return interactor;
    }

    public static ModelledParticipant createModelledParticipant() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        ModelledParticipant interactor = new DefaultModelledParticipant(createDefaultInteractor());
        interactor.setStoichiometry(new IntactStoichiometry(2));
        interactor.getAliases().add(createAliasSynonym(ModelledParticipantAlias.class));
        interactor.getXrefs().add(IntactTestUtils.createPubmedXrefNoQualifier(ModelledParticipantXref.class, "123456"));
        interactor.getAnnotations().add(createAnnotationComment(ModelledParticipantAnnotation.class));
        interactor.getFeatures().add(createBasicModelledFeature("type", "MI:xxxx"));
        interactor.getFeatures().iterator().next().getLinkedFeatures().clear();
        interactor.getCausalRelationships().add(new ModelledCausalRelationship(IntactUtils.createMITopic("increases", null), interactor));

        return interactor;
    }

    public static IntactModelledParticipant createBasicIntactModelledParticipant() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        IntactModelledParticipant interactor = new IntactModelledParticipant(createDefaultIntactInteractor());
        interactor.setStoichiometry(new IntactStoichiometry(2));
        interactor.getFeatures().add(createBasicModelledFeature("type", "MI:xxxx"));
        interactor.getFeatures().iterator().next().getLinkedFeatures().clear();

        return interactor;
    }

    public static ModelledParticipant createBasicModelledParticipant() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        ModelledParticipant interactor = new DefaultModelledParticipant(createDefaultInteractor());
        interactor.setStoichiometry(new IntactStoichiometry(2));
        interactor.getFeatures().add(createBasicModelledFeature("type", "MI:xxxx"));
        interactor.getFeatures().iterator().next().getLinkedFeatures().clear();

        return interactor;
    }
}
