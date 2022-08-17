package uk.ac.ebi.intact.jami.model.extension;

import psidev.psi.mi.jami.model.Alias;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.model.impl.DefaultCvTerm;
import psidev.psi.mi.jami.model.impl.DefaultXref;
import psidev.psi.mi.jami.utils.CvTermUtils;
import psidev.psi.mi.jami.utils.XrefUtils;
import psidev.psi.mi.jami.utils.collection.AbstractCollectionWrapper;
import psidev.psi.mi.jami.utils.collection.AbstractListHavingProperties;
import psidev.psi.mi.jami.utils.comparator.cv.UnambiguousCvTermComparator;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.context.IntactContext;
import uk.ac.ebi.intact.jami.model.AbstractIntactPrimaryObject;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Abstract class for intact cv terms
 * <p>
 * Note: we don't want to mix source with other cv terms as IntAct institution need to be separate entities
 * NOTE: The cv term ac is automatically added as an identifier in getIdentifiers but is not persisted in getDbXrefs.
 * The getIdentifiers.remove will thrown an UnsupportedOperationException if someone tries to remove the AC identifier from the list of identifiers
 * NOTE: getAnnotations is not persistent. For HQL queries, the method getDbAnnotations should be used because is annotated with hibernate annotations.
 * However, getDbAnnotations should not be used directly to add/remove annotations because it could mess up with the state of the object. Only the synchronizers
 * can use it this way before persistence. The access type of DbAnnotations is private as it does not have to be used by the synchronizers neither.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/01/14</pre>
 */
@MappedSuperclass
public abstract class AbstractIntactCvTerm extends AbstractIntactPrimaryObject implements CvTerm {
    private String shortName;
    private String fullName;
    private transient CvTermXrefList xrefs;
    private transient CvTermIdentifierList identifiers;
    private transient Xref miIdentifier;
    private transient Xref modIdentifier;
    private transient Xref parIdentifier;

    private transient PersistentXrefList persistentXrefs;
    private Collection<Alias> synonyms;

    private transient Xref acRef;

    protected AbstractIntactCvTerm() {
        //super call sets creation time data
        super();
    }

    public AbstractIntactCvTerm(String shortName) {
        super();
        if (shortName == null) {
            throw new IllegalArgumentException("The short name is required and cannot be null");
        }
        this.shortName = shortName;
    }

    public AbstractIntactCvTerm(String shortName, String miIdentifier) {
        this(shortName);
        if (miIdentifier != null) {
            setMIIdentifier(miIdentifier);
        }
    }

    public AbstractIntactCvTerm(String shortName, String fullName, String miIdentifier) {
        this(shortName, miIdentifier);
        this.fullName = fullName;
    }

    public AbstractIntactCvTerm(String shortName, Xref ontologyId) {
        this(shortName);
        if (ontologyId != null) {
            getIdentifiers().add(ontologyId);
        }
    }

    public AbstractIntactCvTerm(String shortName, String fullName, Xref ontologyId) {
        this(shortName, ontologyId);
        this.fullName = fullName;
    }

    @Transient
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The short name cannot be null");
        }
        this.shortName = name.trim();
    }

    @Override
    public void setAc(String ac) {
        super.setAc(ac);
        // only if identifiers are initialised
        if (this.acRef != null && !this.acRef.getId().equals(ac)) {
            // we don't want to create a persistent xref
            Xref newRef = new DefaultXref(this.acRef.getDatabase(), ac, this.acRef.getQualifier());
            this.identifiers.removeOnly(acRef);
            this.acRef = newRef;
            this.identifiers.addOnly(acRef);
        }
    }

    @Column(length = IntactUtils.MAX_FULL_NAME_LEN)
    @Size(max = IntactUtils.MAX_FULL_NAME_LEN)
    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String name) {
        this.fullName = name;
    }

    @Transient
    public abstract Collection<Annotation> getAnnotations();

    @Transient
    protected abstract Collection<Annotation> getDbAnnotations();

    protected abstract void setDbAnnotations(Collection<Annotation> annotations);

    protected abstract boolean processAddedAnnotations(Annotation annot);

    protected abstract void resetFieldsLinkedToAnnotations();


    @Transient
    public Collection<Alias> getSynonyms() {
        if (synonyms == null) {
            this.synonyms = new ArrayList<Alias>();
        }
        return this.synonyms;
    }

    protected void setSynonyms(Collection<Alias> aliases) {
        this.synonyms = aliases;
    }

    @Transient
    public Collection<Xref> getIdentifiers() {
        if (identifiers == null) {
            initialiseXrefs();
        }
        return identifiers;
    }

    @Transient
    public String getMIIdentifier() {
        if (this.identifiers == null) {
            initialiseXrefs();
        }
        return this.miIdentifier != null ? this.miIdentifier.getId() : null;
    }

    public void setMIIdentifier(String mi) {
        Collection<Xref> cvTermIdentifiers = getIdentifiers();

        // add new mi if not null
        if (mi != null) {
            CvTerm psiMiDatabase = IntactUtils.createPsiMiDatabase();
            CvTerm identityQualifier = IntactUtils.createIdentityQualifier(psiMiDatabase);
            // first remove old psi mi if not null
            if (this.miIdentifier != null && !mi.equals(this.miIdentifier.getId())) {
                if (this.miIdentifier instanceof AbstractIntactXref) {
                    ((AbstractIntactXref) this.miIdentifier).setId(mi);
                } else {
                    cvTermIdentifiers.remove(this.miIdentifier);
                    this.miIdentifier = new CvTermXref(psiMiDatabase, mi, identityQualifier);
                    cvTermIdentifiers.add(this.miIdentifier);
                }
            } else if (this.miIdentifier == null) {
                this.miIdentifier = new CvTermXref(psiMiDatabase, mi, identityQualifier);
                cvTermIdentifiers.add(this.miIdentifier);
            }
        }
        // remove all mi if the collection is not empty
        else if (!getIdentifiers().isEmpty()) {
            XrefUtils.removeAllXrefsWithDatabase(getIdentifiers(), CvTerm.PSI_MI_MI, CvTerm.PSI_MI);
            this.miIdentifier = null;
        }
    }

    @Transient
    public String getMODIdentifier() {
        if (this.identifiers == null) {
            initialiseXrefs();
        }
        return this.modIdentifier != null ? this.modIdentifier.getId() : null;
    }

    public void setMODIdentifier(String mod) {
        Collection<Xref> cvTermIdentifiers = getIdentifiers();

        // add new mod if not null
        if (mod != null) {

            CvTerm psiModDatabase = IntactUtils.createMIDatabase(CvTerm.PSI_MOD, CvTerm.PSI_MOD_MI);
            CvTerm identityQualifier = IntactUtils.createIdentityQualifier();
            // first remove old psi mod if not null
            if (this.modIdentifier != null && !mod.equals(this.modIdentifier.getId())) {
                if (this.modIdentifier instanceof AbstractIntactXref) {
                    ((AbstractIntactXref) this.modIdentifier).setId(mod);
                } else {
                    cvTermIdentifiers.remove(this.modIdentifier);
                    this.modIdentifier = new CvTermXref(psiModDatabase, mod, identityQualifier);
                    cvTermIdentifiers.add(this.modIdentifier);
                }
            } else if (this.modIdentifier == null) {
                this.modIdentifier = new CvTermXref(psiModDatabase, mod, identityQualifier);
                cvTermIdentifiers.add(this.modIdentifier);
            }
        }
        // remove all mod if the collection is not empty
        else if (!getIdentifiers().isEmpty()) {
            XrefUtils.removeAllXrefsWithDatabase(getIdentifiers(), CvTerm.PSI_MOD_MI, CvTerm.PSI_MOD);
            this.modIdentifier = null;
        }
    }

    @Transient
    public String getPARIdentifier() {
        if (this.identifiers == null) {
            initialiseXrefs();
        }
        return this.parIdentifier != null ? this.parIdentifier.getId() : null;
    }

    public void setPARIdentifier(String par) {
        Collection<Xref> cvTermIdentifiers = getIdentifiers();

        // add new mod if not null
        if (par != null) {

            CvTerm psiParDatabase = IntactUtils.createMIDatabase(CvTerm.PSI_PAR, null);
            CvTerm identityQualifier = IntactUtils.createIdentityQualifier();
            // first remove old psi mod if not null
            if (this.parIdentifier != null && !par.equals(this.parIdentifier.getId())) {
                if (this.parIdentifier instanceof AbstractIntactXref) {
                    ((AbstractIntactXref) this.parIdentifier).setId(par);
                } else {
                    cvTermIdentifiers.remove(this.parIdentifier);
                    this.parIdentifier = new CvTermXref(psiParDatabase, par, identityQualifier);
                    cvTermIdentifiers.add(this.parIdentifier);
                }
            } else if (this.parIdentifier == null) {
                this.parIdentifier = new CvTermXref(psiParDatabase, par, identityQualifier);
                cvTermIdentifiers.add(this.parIdentifier);
            }
        }
        // remove all mod if the collection is not empty
        else if (!getIdentifiers().isEmpty()) {
            XrefUtils.removeAllXrefsWithDatabase(getIdentifiers(), null, CvTerm.PSI_PAR);
            this.parIdentifier = null;
        }
    }

    @Transient
    public Collection<Xref> getXrefs() {
        if (xrefs == null) {
            initialiseXrefs();
        }
        return this.xrefs;
    }

    protected void initialiseXrefs() {
        this.identifiers = new CvTermIdentifierList();
        this.xrefs = new CvTermXrefList();
        // initialise persistent xref and content
        if (this.persistentXrefs != null) {
            for (Xref ref : this.persistentXrefs) {
                if (XrefUtils.isXrefAnIdentifier(ref) || XrefUtils.doesXrefHaveQualifier(ref, null, "intact-secondary")) {
                    this.identifiers.addOnly(ref);
                    processAddedIdentifierEvent(ref);
                } else {
                    this.xrefs.addOnly(ref);
                    processAddedXrefEvent(ref);
                }
            }
        } else {
            this.persistentXrefs = new PersistentXrefList(null);
        }
        // initialise ac
        if (getAc() != null) {
            IntactContext intactContext = ApplicationContextProvider.getBean("intactJamiContext");
            if (intactContext != null) {
                CvTerm database = IntactUtils.getCvByMITerm(Xref.INTACT_MI, IntactUtils.DATABASE_OBJCLASS);
                CvTerm qualifier = IntactUtils.getCvByMITerm(Xref.IDENTITY_MI, IntactUtils.QUALIFIER_OBJCLASS);
                if (database == null || qualifier == null) {
                    this.acRef = new DefaultXref(intactContext.getIntactConfiguration().getDefaultInstitution(), getAc(), CvTermUtils.createIdentityQualifier());
                } else {
                    this.acRef = new DefaultXref(database,
                            getAc(), qualifier);
                }
            } else {
                this.acRef = new DefaultXref(new DefaultCvTerm("unknown"), getAc(), CvTermUtils.createIdentityQualifier());
            }
            this.identifiers.addOnly(this.acRef);
        }
    }

    protected void processAddedXrefEvent(Xref ref) {
        // nothing to do
    }

    @Transient
    protected Collection<Xref> getDbXrefs() {
        if (this.persistentXrefs == null) {
            this.persistentXrefs = new PersistentXrefList(null);
        }
        return this.persistentXrefs.getWrappedList();
    }

    protected void setDbXrefs(Collection<Xref> persistentXrefs) {
        if (persistentXrefs instanceof PersistentXrefList) {
            this.persistentXrefs = (PersistentXrefList) persistentXrefs;
            resetXrefs();
        } else {
            this.persistentXrefs = new PersistentXrefList(persistentXrefs);
            resetXrefs();
        }
    }


    protected void processAddedIdentifierEvent(Xref added) {

        // the added identifier is psi-mi and it is not the current mi identifier
        if (miIdentifier != added && XrefUtils.isXrefFromDatabase(added, CvTerm.PSI_MI_MI, CvTerm.PSI_MI)) {
            // the current psi-mi identifier is not identity, we may want to set miIdentifier
            if (!XrefUtils.doesXrefHaveQualifier(miIdentifier, Xref.IDENTITY_MI, Xref.IDENTITY)) {
                // the miidentifier is not set, we can set the miidentifier
                if (miIdentifier == null) {
                    miIdentifier = added;
                } else if (XrefUtils.doesXrefHaveQualifier(added, Xref.IDENTITY_MI, Xref.IDENTITY)) {
                    miIdentifier = added;
                }
                // the added xref is secondary object and the current mi is not a secondary object, we reset miidentifier
                else if (!XrefUtils.doesXrefHaveQualifier(miIdentifier, Xref.SECONDARY_MI, Xref.SECONDARY)
                        && XrefUtils.doesXrefHaveQualifier(added, Xref.SECONDARY_MI, Xref.SECONDARY)) {
                    miIdentifier = added;
                }
            }
        }
        // the added identifier is psi-mod and it is not the current mod identifier
        else if (modIdentifier != added && XrefUtils.isXrefFromDatabase(added, CvTerm.PSI_MOD_MI, CvTerm.PSI_MOD)) {
            // the current psi-mod identifier is not identity, we may want to set modIdentifier
            if (!XrefUtils.doesXrefHaveQualifier(modIdentifier, Xref.IDENTITY_MI, Xref.IDENTITY)) {
                // the modIdentifier is not set, we can set the modIdentifier
                if (modIdentifier == null) {
                    modIdentifier = added;
                } else if (XrefUtils.doesXrefHaveQualifier(added, Xref.IDENTITY_MI, Xref.IDENTITY)) {
                    modIdentifier = added;
                }
                // the added xref is secondary object and the current mi is not a secondary object, we reset miidentifier
                else if (!XrefUtils.doesXrefHaveQualifier(modIdentifier, Xref.SECONDARY_MI, Xref.SECONDARY)
                        && XrefUtils.doesXrefHaveQualifier(added, Xref.SECONDARY_MI, Xref.SECONDARY)) {
                    modIdentifier = added;
                }
            }
        }
        // the added identifier is psi-par and it is not the current par identifier
        else if (parIdentifier != added && XrefUtils.isXrefFromDatabase(added, null, CvTerm.PSI_PAR)) {
            // the current psi-par identifier is not identity, we may want to set parIdentifier
            if (!XrefUtils.doesXrefHaveQualifier(parIdentifier, Xref.IDENTITY_MI, Xref.IDENTITY)) {
                // the parIdentifier is not set, we can set the parIdentifier
                if (parIdentifier == null) {
                    parIdentifier = added;
                } else if (XrefUtils.doesXrefHaveQualifier(added, Xref.IDENTITY_MI, Xref.IDENTITY)) {
                    parIdentifier = added;
                }
                // the added xref is secondary object and the current par is not a secondary object, we reset paridentifier
                else if (!XrefUtils.doesXrefHaveQualifier(parIdentifier, Xref.SECONDARY_MI, Xref.SECONDARY)
                        && XrefUtils.doesXrefHaveQualifier(added, Xref.SECONDARY_MI, Xref.SECONDARY)) {
                    parIdentifier = added;
                }
            }
        }
    }

    protected void processRemovedIdentifierEvent(Xref removed) {
        // the removed identifier is psi-mi
        if (miIdentifier != null && miIdentifier.equals(removed)) {
            miIdentifier = XrefUtils.collectFirstIdentifierWithDatabase(getIdentifiers(), CvTerm.PSI_MI_MI, CvTerm.PSI_MI);
        }
        // the removed identifier is psi-mod
        else if (modIdentifier != null && modIdentifier.equals(removed)) {
            modIdentifier = XrefUtils.collectFirstIdentifierWithDatabase(getIdentifiers(), CvTerm.PSI_MOD_MI, CvTerm.PSI_MOD);
        }
        // the removed identifier is psi-par
        else if (parIdentifier != null && parIdentifier.equals(removed)) {
            parIdentifier = XrefUtils.collectFirstIdentifierWithDatabase(getIdentifiers(), null, CvTerm.PSI_PAR);
        }
    }

    protected void processRemovedXrefEvent(Xref removed) {
        // nothing to do
    }

    protected void clearPropertiesLinkedToIdentifiers() {
        miIdentifier = null;
        modIdentifier = null;
        parIdentifier = null;
    }

    protected void clearPropertiesLinkedToXrefs() {
        // nothing to do
    }

    protected void resetXrefs() {
        this.identifiers = null;
        this.xrefs = null;
        this.miIdentifier = null;
        this.modIdentifier = null;
        this.parIdentifier = null;
    }

    /**
     * This method can reset all properties that are cached in this object as if it was just loaded from the database
     */
    public void resetCachedDbProperties() {
        resetXrefs();
        resetFieldsLinkedToAnnotations();
    }

    @Override
    public String toString() {
        return (miIdentifier != null ? miIdentifier.getId() : (modIdentifier != null ? modIdentifier.getId() : (parIdentifier != null ? parIdentifier.getId() : "-"))) + " (" + shortName + ")";
    }

    @Override
    public int hashCode() {
        return UnambiguousCvTermComparator.hashCode(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CvTerm)) {
            return false;
        }

        return UnambiguousCvTermComparator.areEquals(this, (CvTerm) o);
    }

    /**
     * Overrides serialization for xrefs and annotations (inner classes not serializable)
     *
     * @param oos
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream oos)
            throws IOException {
        // default serialization
        oos.defaultWriteObject();
        // write the xrefs
        oos.writeObject(getDbXrefs());
        // write the annotations
        oos.writeObject(getDbAnnotations());
    }

    /**
     * Overrides serialization for xrefs and annotations (inner classes not serializable)
     *
     * @param ois
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException {
        // default deserialization
        ois.defaultReadObject();
        // read default xrefs
        setDbXrefs((Collection<Xref>) ois.readObject());
        // read default annotations
        setDbAnnotations((Collection<Annotation>) ois.readObject());
    }

    protected class CvTermIdentifierList extends AbstractListHavingProperties<Xref> {
        public CvTermIdentifierList() {
            super();
        }

        @Override
        protected void processAddedObjectEvent(Xref added) {
            if (!added.equals(acRef)) {
                processAddedIdentifierEvent(added);
                persistentXrefs.add(added);
            }
        }

        @Override
        protected void processRemovedObjectEvent(Xref removed) {
            if (!removed.equals(acRef)) {
                processRemovedIdentifierEvent(removed);
                persistentXrefs.remove(removed);
            } else {
                super.addOnly(acRef);
                throw new UnsupportedOperationException("Cannot remove the database accession of a Cv object from its list of identifiers.");
            }
        }

        @Override
        protected void clearProperties() {
            clearPropertiesLinkedToIdentifiers();
            persistentXrefs.retainAll(getXrefs());
            if (acRef != null) {
                super.addOnly(acRef);
            }
        }
    }

    protected class CvTermXrefList extends AbstractListHavingProperties<Xref> {
        public CvTermXrefList() {
            super();
        }

        @Override
        protected void processAddedObjectEvent(Xref added) {
            processAddedXrefEvent(added);
            persistentXrefs.add(added);
        }

        @Override
        protected void processRemovedObjectEvent(Xref removed) {
            processRemovedXrefEvent(removed);
            persistentXrefs.remove(removed);
        }

        @Override
        protected void clearProperties() {
            clearPropertiesLinkedToXrefs();
            persistentXrefs.retainAll(getIdentifiers());
        }
    }

    protected class PersistentXrefList extends AbstractCollectionWrapper<Xref> {

        public PersistentXrefList(Collection<Xref> persistentBag) {
            super(persistentBag);
        }

        @Override
        protected boolean needToPreProcessElementToAdd(Xref added) {
            return false;
        }

        @Override
        protected Xref processOrWrapElementToAdd(Xref added) {
            return added;
        }

        @Override
        protected void processElementToRemove(Object o) {
            // nothing to do
        }

        @Override
        protected boolean needToPreProcessElementToRemove(Object o) {
            return false;
        }
    }

}
