/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.jami.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 *  Class to retrieve generated complex ac
 *
 */
@Entity
@Table( name = "ia_complex_ac" )
public class ComplexAcValue {

    private String ac;

    public ComplexAcValue() {
    }

    ///////////////////////////////////////
    //access methods for attributes

    @Id
    @GeneratedValue(generator = "complex-ac-generator", strategy = GenerationType.SEQUENCE)
    @GenericGenerator(name = "complex-ac-generator", strategy = "uk.ac.ebi.intact.jami.model.ComplexAcGenerator")
    @Column(name = "ac", length = 30)
    public String getAc() {
        return ac;
    }

    /**
     * This method should not be used by applications, as the AC is auto-generated. If
     *
     * @param ac complex accession
     */
    public void setAc(String ac ) {
        this.ac = ac;
    }

    public static String getNextComplexAcValue(EntityManager em) {
        ComplexAcValue acValue = new ComplexAcValue();
        em.persist(acValue);
        return acValue.getAc();
    }

    ///////////////////////////////////////
    // instance methods
    @Override
    public String toString() {
        return this.ac;
    }

}

