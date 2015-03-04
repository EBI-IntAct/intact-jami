package uk.ac.ebi.intact.jami.model.extension;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Target;
import psidev.psi.mi.jami.model.Alias;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Organism;
import psidev.psi.mi.jami.utils.comparator.organism.UnambiguousOrganismComparator;
import uk.ac.ebi.intact.jami.model.AbstractIntactPrimaryObject;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Intact implementation of organism
 *
 * NOTE: for backward compatibility, getTaxid is not persistent and we should use getDbTaxid for HQL queries.
 * NOTE: getCompartment is not persistent and should not be used in HQL queries
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14/01/14</pre>
 */
@Entity
@Table( name = "ia_biosource" )
@Cacheable
public class IntactOrganism extends AbstractIntactPrimaryObject implements Organism{
    private String commonName;
    private String scientificName;
    private int taxId;
    private Collection<Alias> aliases;
    private CvTerm cellType;
    private CvTerm compartment;
    private CvTerm tissue;

    protected IntactOrganism(){
        this(-3);
    }

    public IntactOrganism(int taxId){
        if (taxId == -1 || taxId == -2 || taxId == -3 || taxId == -4 || taxId == -5 || taxId > 0){
            this.taxId = taxId;
        }
        else {
            throw new IllegalArgumentException("The taxId "+taxId+" is not a valid taxid. Only NCBI taxid or -1, -2, -3, -4, -5 are valid taxids.");
        }
    }

    public IntactOrganism(int taxId, String commonName){
        this(taxId);
        this.commonName = commonName;
    }

    public IntactOrganism(int taxId, String commonName, String scientificName){
        this(taxId, commonName);
        this.scientificName = scientificName;
    }

    public IntactOrganism(int taxId, CvTerm cellType, CvTerm tissue, CvTerm compartment){
        this(taxId);
        this.cellType = cellType;
        this.tissue = tissue;
        this.compartment = compartment;
    }

    public IntactOrganism(int taxId, String commonName, CvTerm cellType, CvTerm tissue, CvTerm compartment){
        this(taxId, commonName);
        this.cellType = cellType;
        this.tissue = tissue;
        this.compartment = compartment;
    }

    public IntactOrganism(int taxId, String commonName, String scientificName, CvTerm cellType, CvTerm tissue, CvTerm compartment){
        this(taxId, commonName, scientificName);
        this.cellType = cellType;
        this.tissue = tissue;
        this.compartment = compartment;
    }

    protected void initialiseAliases(){
        this.aliases = new ArrayList<Alias>();
    }

    @Column(name = "shortlabel", nullable = false, unique = true)
    @Size( min = 1, max = IntactUtils.MAX_SHORT_LABEL_LEN )
    @NotNull
    public String getCommonName() {
        return this.commonName;
    }

    public void setCommonName(String name) {
        this.commonName = name;
    }

    @Column( name = "fullname", length = IntactUtils.MAX_FULL_NAME_LEN )
    @Size( max = IntactUtils.MAX_FULL_NAME_LEN )
    public String getScientificName() {
        return this.scientificName;
    }

    public void setScientificName(String name) {
        this.scientificName = name;
    }

    @Transient
    public int getTaxId() {
        return this.taxId;
    }

    public void setTaxId(int id) {
        if (taxId == -1 || taxId == -2 || taxId == -3 || taxId == -4 || taxId == -5 || taxId > 0){
            this.taxId = id;
        }
        else {
            throw new IllegalArgumentException("The taxId "+id+" is not a valid taxid. Only NCBI taxid or -1, -2, -3, -4, -5 are valid taxids.");
        }
    }

    @OneToMany( cascade = {CascadeType.ALL}, orphanRemoval = true, targetEntity = OrganismAlias.class)
    @JoinColumn(name="parent_ac", referencedColumnName="ac")
    @Cascade( value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE} )
    @Target(OrganismAlias.class)
    public Collection<Alias> getAliases() {
        if (aliases == null){
            initialiseAliases();
        }
        return this.aliases;
    }

    @ManyToOne(targetEntity = IntactCvTerm.class)
    @JoinColumn( name = "celltype_ac", referencedColumnName = "ac" )
    @ForeignKey(name = "FK_BIOSOURCE$CELLTYPE")
    @Target(IntactCvTerm.class)
    public CvTerm getCellType() {
        return this.cellType;
    }

    public void setCellType(CvTerm cellType) {
        this.cellType = cellType;
    }

    @Transient
    public CvTerm getCompartment() {
        return this.compartment;
    }

    public void setCompartment(CvTerm compartment) {
        this.compartment = compartment;
    }

    @ManyToOne(targetEntity = IntactCvTerm.class)
    @JoinColumn( name = "tissue_ac", referencedColumnName = "ac" )
    @ForeignKey(name = "FK_BIOSOURCE$TISSUE")
    @Target(IntactCvTerm.class)
    public CvTerm getTissue() {
        return this.tissue;
    }

    public void setTissue(CvTerm tissue) {
        this.tissue = tissue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }

        if (!(o instanceof Organism)){
            return false;
        }

        return UnambiguousOrganismComparator.areEquals(this, (Organism) o);
    }

    @Override
    public int hashCode() {
        return UnambiguousOrganismComparator.hashCode(this);
    }

    @Override
    public String toString() {
        return taxId + "(" + (commonName != null ? commonName : "-" )+")";
    }

    @Transient
    public boolean areAliasesInitialized(){
        return Hibernate.isInitialized(getAliases());
    }

    @Column( name="taxid", length = 30, nullable = false)
    @NotNull
    @Size(max = 30)
    protected String getDbTaxid(){
        return Integer.toString(this.taxId);
    }

    private void setDbTaxid(String taxid){
        if (taxid == null){
           this.taxId = -3;
        }
        else{
            try {
                this.taxId = Integer.parseInt(taxid);
            }
            catch (NumberFormatException e){
                this.taxId = -3;
            }
        }
    }

    private void setAliases(Collection<Alias> aliases) {
        this.aliases = aliases;
    }
}
