package uk.ac.ebi.intact.jami.model.extension;

import org.hibernate.annotations.Where;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.utils.ChecksumUtils;
import psidev.psi.mi.jami.utils.XrefUtils;
import psidev.psi.mi.jami.utils.collection.AbstractListHavingProperties;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.Iterator;

/**
 * Intact implementation of bioactive entity
 *
 * Smile, standard inchi and standard inchi key are stored as annotations for backward compatibility with IntAct core
 * but when intact-core is removed, it may be good to add these checksum as columns in the interactor table
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16/01/14</pre>
 */
@Entity
@DiscriminatorValue( "bioactive_entity" )
@Where(clause = "category = 'bioactive_entity'")
public class IntactBioactiveEntity extends IntactMolecule implements BioactiveEntity{

    private transient Xref chebi;
    private transient Checksum smile;
    private transient Checksum standardInchi;
    private transient Checksum standardInchiKey;

    protected IntactBioactiveEntity(){
        super();
    }

    public IntactBioactiveEntity(String name, CvTerm type) {
        super(name, type);
    }

    public IntactBioactiveEntity(String name, String fullName, CvTerm type) {
        super(name, fullName, type);
    }

    public IntactBioactiveEntity(String name, CvTerm type, Organism organism) {
        super(name, type, organism);
    }

    public IntactBioactiveEntity(String name, String fullName, CvTerm type, Organism organism) {
        super(name, fullName, type, organism);
    }

    public IntactBioactiveEntity(String name, CvTerm type, Xref uniqueId) {
        super(name, type, uniqueId);
    }

    public IntactBioactiveEntity(String name, String fullName, CvTerm type, Xref uniqueId) {
        super(name, fullName, type, uniqueId);
    }

    public IntactBioactiveEntity(String name, CvTerm type, Organism organism, Xref uniqueId) {
        super(name, type, organism, uniqueId);
    }

    public IntactBioactiveEntity(String name, String fullName, CvTerm type, Organism organism, Xref uniqueId) {
        super(name, fullName, type, organism, uniqueId);

    }

    public IntactBioactiveEntity(String name, String fullName, CvTerm type, String uniqueChebi) {
        super(name, fullName, type);
        if (uniqueChebi != null){
            setChebi(uniqueChebi);
        }
    }

    public IntactBioactiveEntity(String name, CvTerm type, Organism organism, String uniqueChebi) {
        super(name, type, organism);
        if (uniqueChebi != null){
            setChebi(uniqueChebi);
        }
    }

    public IntactBioactiveEntity(String name, String fullName, CvTerm type, Organism organism, String uniqueChebi) {
        super(name, fullName, type, organism);
        if (uniqueChebi != null){
            setChebi(uniqueChebi);
        }
    }

    public IntactBioactiveEntity(String name) {
        super(name);
    }

    public IntactBioactiveEntity(String name, String fullName) {
        super(name, fullName);
    }

    public IntactBioactiveEntity(String name, Organism organism) {
        super(name, organism);
    }

    public IntactBioactiveEntity(String name, String fullName, Organism organism) {
        super(name, fullName, organism);
    }

    public IntactBioactiveEntity(String name, Xref uniqueId) {
        super(name, uniqueId);
    }

    public IntactBioactiveEntity(String name, String fullName, Xref uniqueId) {
        super(name, fullName, uniqueId);
    }

    public IntactBioactiveEntity(String name, Organism organism, Xref uniqueId) {
        super(name, organism, uniqueId);
    }

    public IntactBioactiveEntity(String name, String fullName, Organism organism, Xref uniqueId) {
        super(name, fullName, organism, uniqueId);

    }

    public IntactBioactiveEntity(String name, String fullName, String uniqueChebi) {
        super(name, fullName);
        if (uniqueChebi != null){
            setChebi(uniqueChebi);
        }
    }

    public IntactBioactiveEntity(String name, Organism organism, String uniqueChebi) {
        super(name, organism);
        if (uniqueChebi != null){
            setChebi(uniqueChebi);
        }
    }

    public IntactBioactiveEntity(String name, String fullName, Organism organism, String uniqueChebi) {
        super(name, fullName, organism);
        if (uniqueChebi != null){
            setChebi(uniqueChebi);
        }
    }

    @Override
    /**
     * Return the first chebi identifier if provided, otherwise the first identifier in the list of identifiers
     */
    @Transient
    public Xref getPreferredIdentifier() {
        // initialise identifiers if not done yet
        getIdentifiers();
        return chebi != null ? chebi : super.getPreferredIdentifier();
    }

    @Transient
    public String getChebi() {
        // initialise identifiers if not done yet
        getIdentifiers();
        return chebi != null ? chebi.getId() : null;
    }

    public void setChebi(String id) {
        Collection<Xref> bioactiveEntityIdentifiers = getIdentifiers();

        // add new chebi if not null
        if (id != null){
            CvTerm chebiDatabase = IntactUtils.createMIDatabase(Xref.CHEBI, Xref.CHEBI_MI);
            CvTerm identityQualifier = IntactUtils.createMIQualifier(Xref.IDENTITY, Xref.IDENTITY_MI);
            // first remove old chebi if not null
            if (this.chebi != null && !id.equals(this.chebi)){
                if (this.chebi instanceof AbstractIntactXref){
                    ((AbstractIntactXref) this.chebi).setId(id);
                }
                else{
                    bioactiveEntityIdentifiers.remove(this.chebi);
                    this.chebi = new InteractorXref(chebiDatabase, id, identityQualifier);
                    bioactiveEntityIdentifiers.add(this.chebi);
                }
            }
            else if (this.chebi == null){
                this.chebi = new InteractorXref(chebiDatabase, id, identityQualifier);
                bioactiveEntityIdentifiers.add(this.chebi);
            }
        }
        // remove all chebi if the collection is not empty
        else if (!bioactiveEntityIdentifiers.isEmpty()) {
            XrefUtils.removeAllXrefsWithDatabase(bioactiveEntityIdentifiers, Xref.CHEBI_MI, Xref.CHEBI);
            this.chebi = null;
        }
    }

    @Transient
    public String getSmile() {
        // initialise checksum if not done yet
        getChecksums();
        return smile != null ? smile.getValue() : null;
    }

    public void setSmile(String smile) {
        Collection<Checksum> bioactiveEntityChecksums = getChecksums();

        if (smile != null){
            CvTerm smileMethod = IntactUtils.createMITopic(Checksum.SMILE, Checksum.SMILE_MI);
            // first remove old smile
            if (this.smile != null){
                bioactiveEntityChecksums.remove(this.smile);
            }
            Annotation annotSmile = new InteractorAnnotation(smileMethod, smile);
            this.smile = new IntactChecksumWrapper(annotSmile);
            bioactiveEntityChecksums.add(this.smile);
            getDbAnnotations().add(annotSmile);
        }
        // remove all smiles if the collection is not empty
        else if (!bioactiveEntityChecksums.isEmpty()) {
            ChecksumUtils.removeAllChecksumWithMethod(bioactiveEntityChecksums, Checksum.SMILE_MI, Checksum.SMILE);
            AnnotationUtils.removeAllAnnotationsWithTopic(getDbAnnotations(), Checksum.SMILE_MI, Checksum.SMILE);
            this.smile = null;
        }
    }

    @Transient
    public String getStandardInchiKey() {
        // initialise checksum if not done yet
        getChecksums();
        return standardInchiKey != null ? standardInchiKey.getValue() : null;
    }

    public void setStandardInchiKey(String key) {
        Collection<Checksum> bioactiveEntityChecksums = getChecksums();

        if (key != null){
            CvTerm inchiKeyMethod = IntactUtils.createMITopic(Checksum.STANDARD_INCHI_KEY, Checksum.STANDARD_INCHI_KEY_MI);
            // first remove old standard inchi key
            if (this.standardInchiKey != null){
                bioactiveEntityChecksums.remove(this.standardInchiKey);
            }
            Annotation annotInchi = new InteractorAnnotation(inchiKeyMethod, key);
            this.standardInchiKey = new IntactChecksumWrapper(annotInchi);
            bioactiveEntityChecksums.add(this.standardInchiKey);
            getDbAnnotations().add(annotInchi);
        }
        // remove all standard inchi keys if the collection is not empty
        else if (!bioactiveEntityChecksums.isEmpty()) {
            ChecksumUtils.removeAllChecksumWithMethod(bioactiveEntityChecksums, Checksum.STANDARD_INCHI_KEY_MI, Checksum.STANDARD_INCHI_KEY);
            AnnotationUtils.removeAllAnnotationsWithTopic(getDbAnnotations(), Checksum.STANDARD_INCHI_KEY_MI, Checksum.STANDARD_INCHI_KEY);
            this.standardInchiKey = null;
        }
    }

    @Transient
    public String getStandardInchi() {
        // initialise checksum if not done yet
        getChecksums();
        return standardInchi != null ? standardInchi.getValue() : null;
    }

    public void setStandardInchi(String inchi) {
        Collection<Checksum> bioactiveEntityChecksums = getChecksums();

        if (inchi != null){
            CvTerm inchiMethod = IntactUtils.createMITopic(Checksum.INCHI, Checksum.INCHI_MI);;
            // first remove standard inchi
            if (this.standardInchi != null){
                bioactiveEntityChecksums.remove(this.standardInchi);
            }
            Annotation annotInchi = new InteractorAnnotation(inchiMethod, inchi);
            this.standardInchi = new IntactChecksumWrapper(annotInchi);
            bioactiveEntityChecksums.add(this.standardInchi);
            getDbAnnotations().add(annotInchi);
        }
        // remove all standard inchi if the collection is not empty
        else if (!bioactiveEntityChecksums.isEmpty()) {
            ChecksumUtils.removeAllChecksumWithMethod(bioactiveEntityChecksums, Checksum.INCHI_MI, Checksum.INCHI);
            AnnotationUtils.removeAllAnnotationsWithTopic(getDbAnnotations(), Checksum.INCHI_MI, Checksum.INCHI);
            this.standardInchi = null;
        }
    }

    private void processAddedChecksumEvent(Checksum added) {
        // the added checksum is standard inchi key and it is not the current standard inchi key
        if (standardInchiKey == null && ChecksumUtils.doesChecksumHaveMethod(added, Checksum.STANDARD_INCHI_KEY_MI, Checksum.STANDARD_INCHI_KEY)){
            // the standard inchi key is not set, we can set the standard inchi key
            standardInchiKey = added;
            getDbAnnotations().add(new InteractorAnnotation(standardInchiKey.getMethod(), standardInchiKey.getValue()));
        }
        else if (smile == null && ChecksumUtils.doesChecksumHaveMethod(added, Checksum.SMILE_MI, Checksum.SMILE)){
            // the smile is not set, we can set the smile
            smile = added;
            getDbAnnotations().add(new InteractorAnnotation(smile.getMethod(), smile.getValue()));
        }
        else if (standardInchi == null && ChecksumUtils.doesChecksumHaveMethod(added, Checksum.INCHI_MI, Checksum.INCHI)){
            // the standard inchi is not set, we can set the standard inchi
            standardInchi = added;
            getDbAnnotations().add(new InteractorAnnotation(standardInchi.getMethod(), standardInchi.getValue()));
        }
    }

    private void processRemovedChecksumEvent(Checksum removed) {
        // the removed identifier is standard inchi key
        if (standardInchiKey != null && standardInchiKey.equals(removed)){
            getDbAnnotations().remove(new InteractorAnnotation(standardInchiKey.getMethod(), standardInchiKey.getValue()));
            standardInchiKey = ChecksumUtils.collectFirstChecksumWithMethod(getChecksums(), Checksum.STANDARD_INCHI_KEY_MI, Checksum.STANDARD_INCHI_KEY);
            if (standardInchiKey != null){
                getDbAnnotations().add(new InteractorAnnotation(standardInchiKey.getMethod(), standardInchiKey.getValue()));
            }
        }
        else if (smile != null && smile.equals(removed)){
            getDbAnnotations().remove(new InteractorAnnotation(smile.getMethod(), smile.getValue()));
            smile = ChecksumUtils.collectFirstChecksumWithMethod(getChecksums(), Checksum.SMILE_MI, Checksum.SMILE);
            if (smile != null){
                getDbAnnotations().add(new InteractorAnnotation(smile.getMethod(), smile.getValue()));
            }
        }
        else if (standardInchi != null && standardInchi.equals(removed)){
            getDbAnnotations().remove(new InteractorAnnotation(standardInchi.getMethod(), standardInchi.getValue()));
            standardInchi = ChecksumUtils.collectFirstChecksumWithMethod(getChecksums(), Checksum.INCHI_MI, Checksum.INCHI);
            if (standardInchi != null){
                getDbAnnotations().add(new InteractorAnnotation(standardInchi.getMethod(), standardInchi.getValue()));
            }
        }
    }

    @Override
    protected void processAddedIdentifierEvent(Xref added) {
        // the added identifier is chebi and it is not the current chebi identifier
        if (chebi != added && XrefUtils.isXrefFromDatabase(added, Xref.CHEBI_MI, Xref.CHEBI)){
            // the current chebi identifier is not identity, we may want to set chebiIdentifier
            if (!XrefUtils.doesXrefHaveQualifier(chebi, Xref.IDENTITY_MI, Xref.IDENTITY)){
                // the chebi identifier is not set, we can set the chebi identifier
                if (chebi == null){
                    chebi = added;
                }
                else if (XrefUtils.doesXrefHaveQualifier(added, Xref.IDENTITY_MI, Xref.IDENTITY)){
                    chebi = added;
                }
                // the added xref is secondary object and the current chebi is not a secondary object, we reset chebi identifier
                else if (!XrefUtils.doesXrefHaveQualifier(chebi, Xref.SECONDARY_MI, Xref.SECONDARY)
                        && XrefUtils.doesXrefHaveQualifier(added, Xref.SECONDARY_MI, Xref.SECONDARY)){
                    chebi = added;
                }
            }
        }
    }

    @Override
    protected void processRemovedIdentifierEvent(Xref removed) {
        // the removed identifier is chebi
        if (chebi != null && chebi.equals(removed)){
            chebi = XrefUtils.collectFirstIdentifierWithDatabase(getIdentifiers(), Xref.CHEBI_MI, Xref.CHEBI);
        }
    }

    @Override
    protected void clearPropertiesLinkedToIdentifiers() {
        chebi = null;
    }

    @Override
    protected void initialiseChecksums() {
        super.initialiseChecksumsWith(new BioactiveEntityChecksumList());
        initialiseAnnotations();
    }

    @Override
    protected void initialiseAnnotations() {
        super.initialiseAnnotationsWith(new BioactiveEntityAnnotationList());
        BioactiveEntityChecksumList checksumList = (BioactiveEntityChecksumList)getChecksums();

        Checksum inchi = this.standardInchi;
        Checksum smile = this.smile;
        Checksum key = this.standardInchiKey;

        checksumList.remove(inchi);
        checksumList.remove(smile);
        checksumList.remove(key);

        for (Annotation annotation : getDbAnnotations()){
            // we have a checksum
            if (annotation.getValue() != null
                    && AnnotationUtils.doesAnnotationHaveTopic(annotation, Checksum.SMILE_MI, Checksum.SMILE)){
                this.smile = new IntactChecksumWrapper(annotation);
                checksumList.addOnly(this.smile);
            }
            else if (annotation.getValue() != null
                    && AnnotationUtils.doesAnnotationHaveTopic(annotation, Checksum.INCHI_MI, Checksum.INCHI)){
                this.standardInchi = new IntactChecksumWrapper(annotation);
                checksumList.addOnly(this.standardInchi);

            }
            else if (annotation.getValue() != null &&
                    AnnotationUtils.doesAnnotationHaveTopic(annotation, Checksum.STANDARD_INCHI_KEY_MI, Checksum.STANDARD_INCHI_KEY)){
                this.standardInchiKey = new IntactChecksumWrapper(annotation);
                checksumList.addOnly(this.standardInchiKey);
            }
            // we have a simple annotation
            else{
                ((BioactiveEntityAnnotationList)getAnnotations()).addOnly(annotation);
            }
        }
    }

    @Override
    protected void initialiseDefaultInteractorType() {
        super.setInteractorType(IntactUtils.createIntactMITerm(BioactiveEntity.BIOACTIVE_ENTITY, BioactiveEntity.BIOACTIVE_ENTITY_MI, IntactUtils.INTERACTOR_TYPE_OBJCLASS));
    }

    @Override
    protected void setDbXrefs(Collection<Xref> persistentXrefs) {
        super.setDbXrefs(persistentXrefs);
        this.chebi = null;
    }

    @Override
    protected void setDbAnnotations(Collection<Annotation> annotations) {
        super.setDbAnnotations(annotations);
        initialiseChecksumsWith(null);
        this.smile = null;
        this.standardInchi = null;
        this.standardInchiKey = null;
    }

    @Override
    public void resetCachedDbProperties() {
        super.resetCachedDbProperties();
        initialiseChecksumsWith(null);
        this.chebi = null;
        this.smile = null;
        this.standardInchi = null;
        this.standardInchiKey = null;
    }

    @Override
    protected String generateObjClass() {
        return "uk.ac.ebi.intact.model.SmallMoleculeImpl";
    }

    private void clearPropertiesLinkedToChecksums() {
        if (standardInchiKey != null){
            getDbAnnotations().remove(new InteractorAnnotation(standardInchiKey.getMethod(), standardInchiKey.getValue()));
        }
        if (smile != null){
            getDbAnnotations().remove(new InteractorAnnotation(smile.getMethod(), smile.getValue()));
        }
        if (standardInchi != null){
            getDbAnnotations().remove(new InteractorAnnotation(standardInchi.getMethod(), standardInchi.getValue()));
        }

        this.standardInchiKey = null;
        this.smile = null;
        this.standardInchi = null;
    }

    private class BioactiveEntityChecksumList extends AbstractListHavingProperties<Checksum> {
        public BioactiveEntityChecksumList(){
            super();
        }

        @Override
        protected void processAddedObjectEvent(Checksum checksum) {
            processAddedChecksumEvent(checksum);
        }

        @Override
        protected void processRemovedObjectEvent(Checksum checksum) {
            processRemovedChecksumEvent(checksum);
        }

        @Override
        protected void clearProperties() {
            clearPropertiesLinkedToChecksums();
        }

    }

    private class BioactiveEntityAnnotationList extends AbstractListHavingProperties<Annotation> {
        public BioactiveEntityAnnotationList(){
            super();
        }

        @Override
        protected void processAddedObjectEvent(Annotation annotation) {
            getDbAnnotations().add(annotation);
        }

        @Override
        protected void processRemovedObjectEvent(Annotation annotation) {
            getDbAnnotations().remove(annotation);
        }

        @Override
        protected void clearProperties() {
            Iterator<Annotation> dbAnnotationsIterator = getDbAnnotations().iterator();
            while(dbAnnotationsIterator.hasNext()){
                Annotation dbAnnot = dbAnnotationsIterator.next();

                // we have a checksum
                if (dbAnnot.getValue() != null
                        && AnnotationUtils.doesAnnotationHaveTopic(dbAnnot, Checksum.SMILE_MI, Checksum.SMILE)){
                    // nothing to do
                }
                else if (dbAnnot.getValue() != null
                        && AnnotationUtils.doesAnnotationHaveTopic(dbAnnot, Checksum.INCHI_MI, Checksum.INCHI)){
                    // nothing to do

                }
                else if (dbAnnot.getValue() != null &&
                        AnnotationUtils.doesAnnotationHaveTopic(dbAnnot, Checksum.STANDARD_INCHI_KEY_MI, Checksum.STANDARD_INCHI_KEY)){
                    // nothing to do
                }
                else{
                    dbAnnotationsIterator.remove();
                }
            }
        }

    }
}
