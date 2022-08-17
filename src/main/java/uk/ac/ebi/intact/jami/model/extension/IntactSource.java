package uk.ac.ebi.intact.jami.model.extension;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Target;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.utils.XrefUtils;
import psidev.psi.mi.jami.utils.collection.AbstractCollectionWrapper;
import psidev.psi.mi.jami.utils.collection.AbstractListHavingProperties;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;

/**
 * Intact implementation of a source. It replaces Institution from intact core
 * <p>
 * NOTE: dbUrl and dbPostalAddress are private methods as they are deprecated and only there for backward compatibility with intact-core.
 * Only getUrl and getPostalAddress should be used. These methods are not persistent, if a URL/postal address is attached to an institution, it
 * should always be stored in the annotations of the institution.
 * NOTE: getAnnotations is not persistent. For HQL queries, the method getDbAnnotations should be used because is annotated with hibernate annotations.
 * However, getDbAnnotations should not be used directly to add/remove annotations because it could mess up with the state of the object. Only the synchronizers
 * can use it this way before persistence. The access type of DbAnnotations is private as it does not have to be used by the synchronizers neither.
 * NOTE: getIdentifiers and getXrefs are not persistent methods annotated with hibernate annotations. All the xrefs present in identifiers
 * and xrefs are persisted in the same table for backward compatibility with intact-core. So the persistent xrefs are available with the getDbXrefs method.
 * For HQL queries, the method getDbXrefs should be used because is annotated with hibernate annotations.
 * However, getDbXrefs should not be used directly to add/remove xrefs because it could mess up with the state of the object. Only the synchronizers
 * can use it this way before persistence.
 * NOTE: The source ac is automatically added as an identifier in getIdentifiers but is not persisted in getDbXrefs.
 * The getIdentifiers.remove will thrown an UnsupportedOperationException if someone tries to remove the AC identifier from the list of identifiers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/01/14</pre>
 */
@Entity
@Table(name = "ia_institution")
@Cacheable
public class IntactSource extends AbstractIntactCvTerm implements Source {
    private transient Annotation url;
    private transient Annotation postalAddress;
    private transient Publication bibRef;

    private transient SourceAnnotationList annotations;
    private transient SourcePersistentAnnotationList persistentAnnotations;

    protected IntactSource() {
        super();
    }

    public IntactSource(String shortName) {
        super(shortName);
    }

    public IntactSource(String shortName, Xref ontologyId) {
        super(shortName, ontologyId);
    }

    public IntactSource(String shortName, String fullName, Xref ontologyId) {
        super(shortName, fullName, ontologyId);
    }

    public IntactSource(String shortName, String url, String address, Publication bibRef) {
        super(shortName);
        setUrl(url);
        setPostalAddress(address);
        this.bibRef = bibRef;
    }

    public IntactSource(String shortName, Xref ontologyId, String url, String address, Publication bibRef) {
        super(shortName, ontologyId);
        setUrl(url);
        setPostalAddress(address);
        this.bibRef = bibRef;
    }

    public IntactSource(String shortName, String fullName, Xref ontologyId, String url, String address, Publication bibRef) {
        super(shortName, fullName, ontologyId);
        setUrl(url);
        setPostalAddress(address);
        this.bibRef = bibRef;
    }

    public IntactSource(String shortName, String miId) {
        super(shortName, miId);
    }

    public IntactSource(String shortName, String fullName, String miId) {
        super(shortName, fullName, miId);
    }

    public IntactSource(String shortName, String miId, String url, String address, Publication bibRef) {
        super(shortName, miId);
        setUrl(url);
        setPostalAddress(address);
        this.bibRef = bibRef;
    }

    public IntactSource(String shortName, String fullName, String miId, String url, String address, Publication bibRef) {
        super(shortName, fullName, miId);
        setUrl(url);
        setPostalAddress(address);
        this.bibRef = bibRef;
    }

    @Column(name = "shortlabel", nullable = false, unique = true)
    @Size(min = 1, max = IntactUtils.MAX_SHORT_LABEL_LEN)
    @NotNull
    public String getShortName() {
        return super.getShortName();
    }

    public void setShortName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The short name cannot be null");
        }
        super.setShortName(name);
    }

    @Transient
    public String getUrl() {
        // initialise annotations if not done
        getAnnotations();
        return this.url != null ? this.url.getValue() : null;
    }

    public void setUrl(String url) {
        Collection<Annotation> sourceAnnotationList = getAnnotations();

        // add new url if not null
        if (url != null) {
            CvTerm urlTopic = IntactUtils.getCvTopicByMITerm(Annotation.URL, Annotation.URL_MI);

            // first remove old url if not null
            if (this.url != null) {
                this.url.setValue(url);
            } else {
                this.url = new SourceAnnotation(urlTopic, url);
                sourceAnnotationList.add(this.url);
            }
        }
        // remove all url if the collection is not empty
        else if (!sourceAnnotationList.isEmpty()) {
            AnnotationUtils.removeAllAnnotationsWithTopic(sourceAnnotationList, Annotation.URL_MI, Annotation.URL);
            this.url = null;
        }
    }

    @Transient
    public String getPostalAddress() {
        // initialise annotations if not done
        getAnnotations();
        return this.postalAddress != null ? this.postalAddress.getValue() : null;
    }

    public void setPostalAddress(String address) {
        Collection<Annotation> sourceAnnotationList = getAnnotations();

        // add new url if not null
        if (address != null) {
            CvTerm addressTopic = IntactUtils.getCvTopicByShortName(Annotation.POSTAL_ADDRESS, null);

            if (this.postalAddress != null) {
                this.postalAddress.setValue(address);
            } else {
                this.postalAddress = new SourceAnnotation(addressTopic, address);
                sourceAnnotationList.add(this.postalAddress);
            }
        }
        // remove all url if the collection is not empty
        else if (!sourceAnnotationList.isEmpty()) {
            AnnotationUtils.removeAllAnnotationsWithTopic(sourceAnnotationList, null, Annotation.POSTAL_ADDRESS);
            this.postalAddress = null;
        }
    }

    /*@ManyToOne( targetEntity = IntactPublication.class)
    @JoinColumn( name = "publication_ac", referencedColumnName = "ac")
    @Target(IntactPublication.class) */
    @Transient
    public Publication getPublication() {
        // initialise refs
        getXrefs();
        return this.bibRef;
    }

    public void setPublication(Publication ref) {
        // initialise refs
        getXrefs();
        if (this.bibRef != null) {
            getDbXrefs().removeAll(this.bibRef.getIdentifiers());
        }
        this.bibRef = ref;
        if (this.bibRef != null) {
            if (!ref.getIdentifiers().isEmpty()) {
                resetXrefs();
                for (Xref primary : this.bibRef.getIdentifiers()) {
                    getDbXrefs().add(new SourceXref(primary.getDatabase(), primary.getId(), primary.getVersion(),
                            IntactUtils.createMIQualifier(Xref.PRIMARY, Xref.PRIMARY_MI)));
                }
            }
        }
    }

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true, targetEntity = SourceAlias.class)
    @JoinColumn(name = "parent_ac", referencedColumnName = "ac")
    @Cascade(value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    @Target(SourceAlias.class)
    public Collection<Alias> getSynonyms() {
        return super.getSynonyms();
    }

    @Transient
    public Collection<Annotation> getAnnotations() {
        if (annotations == null) {
            initialiseAnnotations();
        }
        return this.annotations;
    }

    protected void initialiseAnnotations() {
        this.annotations = new SourceAnnotationList(null);

        // initialise persistent annotations and content
        if (this.persistentAnnotations != null) {
            for (Annotation annot : this.persistentAnnotations) {
                if (!processAddedAnnotations(annot)) {
                    this.annotations.addOnly(annot);
                }
            }
        } else {
            this.persistentAnnotations = new SourcePersistentAnnotationList(null);
        }
    }


    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true, targetEntity = SourceAnnotation.class)
    @JoinTable(
            name = "ia_institution2annot",
            joinColumns = @JoinColumn(name = "institution_ac"),
            inverseJoinColumns = @JoinColumn(name = "annotation_ac")
    )
    @Cascade(value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    @Target(SourceAnnotation.class)
    /**
     * WARNING: The join table is for backward compatibility with intact-core.
     * When intact-core will be removed, the join table would disappear wnd the relation would become
     * @JoinColumn(name="parent_ac", referencedColumnName="ac")
     *
     * This method give direct access to the persistent collection of annotations for this object.
     * WARNING: It should not be used to add/remove objects as it may mess up with the state of the object (only used this way by the synchronizers).
     *
     */
    @Override
    public Collection<Annotation> getDbAnnotations() {
        if (this.persistentAnnotations == null) {
            this.persistentAnnotations = new SourcePersistentAnnotationList(null);
        }
        return this.persistentAnnotations.getWrappedList();
    }

    protected void setDbAnnotations(Collection<Annotation> annotations) {
        if (annotations instanceof SourcePersistentAnnotationList) {
            this.persistentAnnotations = (SourcePersistentAnnotationList) annotations;
            resetFieldsLinkedToAnnotations();
        } else {
            this.persistentAnnotations = new SourcePersistentAnnotationList(annotations);
            resetFieldsLinkedToAnnotations();
        }
    }

    @Override
    protected boolean processAddedAnnotations(Annotation annot) {
        if (url == null && AnnotationUtils.doesAnnotationHaveTopic(annot, Annotation.URL_MI, Annotation.URL)) {
            url = annot;
        } else if (postalAddress == null && AnnotationUtils.doesAnnotationHaveTopic(annot, null, Annotation.POSTAL_ADDRESS)) {
            postalAddress = annot;
        }
        return false;
    }

    @Override
    protected void resetFieldsLinkedToAnnotations() {
        this.annotations = null;
        this.url = null;
        this.postalAddress = null;
    }


    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true, targetEntity = SourceXref.class)
    @JoinColumn(name = "parent_ac", referencedColumnName = "ac")
    @Cascade(value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    @Target(SourceXref.class)
    @LazyCollection(LazyCollectionOption.FALSE)
    /**
     * This method give direct access to the persistent collection of xrefs (identifiers and xrefs all together) for this object.
     * WARNING: It should not be used to add/remove objects as it may mess up with the state of the object (only used this way by the synchronizers).
     */
    public Collection<Xref> getDbXrefs() {
        return super.getDbXrefs();
    }

    @Override
    protected void setDbXrefs(Collection<Xref> persistentXrefs) {
        super.setDbXrefs(persistentXrefs);
        this.bibRef = null;
    }

    @Override
    protected void processAddedXrefEvent(Xref ref) {
        if (this.bibRef == null && XrefUtils.doesXrefHaveQualifier(ref, Xref.PRIMARY_MI, Xref.PRIMARY)) {
            this.bibRef = new IntactPublication(ref);
        } else if (XrefUtils.doesXrefHaveQualifier(ref, Xref.PRIMARY_MI, Xref.PRIMARY)) {
            this.bibRef.getIdentifiers().add(ref);
        }
    }

    @Override
    protected void processRemovedXrefEvent(Xref removed) {
        if (this.bibRef != null && this.bibRef.getIdentifiers().contains(removed)) {
            this.bibRef.getIdentifiers().remove(removed);
            if (this.bibRef.getIdentifiers().isEmpty()) {
                this.bibRef = null;
            }
        }
    }

    @Override
    protected void clearPropertiesLinkedToXrefs() {
        super.clearPropertiesLinkedToXrefs();
        this.bibRef = null;
    }

    @Override
    public void resetCachedDbProperties() {
        super.resetCachedDbProperties();
        this.bibRef = null;
    }

    @Transient
    public boolean areXrefsInitialized() {
        return Hibernate.isInitialized(getDbXrefs());
    }

    @Transient
    public boolean areSynonymsInitialized() {
        return Hibernate.isInitialized(getSynonyms());
    }

    @Transient
    public boolean areAnnotationsInitialized() {
        return Hibernate.isInitialized(getDbAnnotations());
    }


    private class SourceAnnotationList extends AbstractListHavingProperties<Annotation> {
        public SourceAnnotationList(Collection<Annotation> annots) {
            super();
        }

        @Override
        protected void processAddedObjectEvent(Annotation added) {
            persistentAnnotations.add(added);
            processAddedAnnotations(added);
        }

        @Override
        protected void processRemovedObjectEvent(Annotation removed) {
            persistentAnnotations.remove(removed);
            if (url != null && url.equals(removed)) {
                url = AnnotationUtils.collectFirstAnnotationWithTopic(getAnnotations(), Annotation.URL_MI, Annotation.URL);
            } else if (postalAddress != null && postalAddress.equals(removed)) {
                postalAddress = AnnotationUtils.collectFirstAnnotationWithTopic(getAnnotations(), null, Annotation.POSTAL_ADDRESS);
            }
        }

        @Override
        protected void clearProperties() {
            persistentAnnotations.clear();
            url = null;
            postalAddress = null;
        }
    }

    protected class SourcePersistentAnnotationList extends AbstractCollectionWrapper<Annotation> {

        public SourcePersistentAnnotationList(Collection<Annotation> persistentBag) {
            super(persistentBag);
        }

        @Override
        protected boolean needToPreProcessElementToAdd(Annotation added) {
            return false;
        }

        @Override
        protected Annotation processOrWrapElementToAdd(Annotation added) {
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
