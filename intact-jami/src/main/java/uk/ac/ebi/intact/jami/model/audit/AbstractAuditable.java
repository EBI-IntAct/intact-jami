/**
 * Copyright 2006 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.jami.model.audit;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import uk.ac.ebi.intact.jami.context.UserContext;
import uk.ac.ebi.intact.jami.model.listener.AuditableEventListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * This is the top level class for all auditable intact model object.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 */
@MappedSuperclass
@DynamicInsert(value = true)
@DynamicUpdate(value = true)
@EntityListeners(value = {AuditableEventListener.class})
public abstract class AbstractAuditable implements Auditable, Serializable {

    ///////////////////////////////////////
    //attributes

    /**
     * The curator who has last edited the object.
     */
    public String updator;

    /**
     * The curator who has created the edited object
     */
    public String creator;

    /**
     * Creation date of an object. The type is java.sql.Date, not java.util.Data, for database compatibility.
     */
    private Date created;

    /**
     * The last update of the object. The type is java.sql.Date, not java.util.Data, for database compatibility.
     */
    private Date updated;

    private transient UserContext localContext;


    public AbstractAuditable() {
    }

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    public Date getCreated() {
        return created;
    }

    /**
     * <b>Avoid calling this method as this field is set by the DB and it can't be modifiable via OJB because 'created'
     * field is declared as read-only</b>
     */
    public void setCreated( Date created ) {
        this.created = created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    public Date getUpdated() {
        return updated;
    }

    /**
     * <b>Avoid calling this method as this field is set by the DB and it can't
     * be modifiable via OJB because 'updated' field is declared as read-only</b>
     */
    public void setUpdated( Date updated ) {
        this.updated = updated;
    }

    @Column( name="created_user", length=30)
    @NotNull
    public String getCreator() {
        return creator;
    }

    public void setCreator( String creator ) {
        this.creator = creator;
    }

    @Column( name="userstamp", length=30 )
    @NotNull
    public String getUpdator() {
        return updator;
    }

    public void setUpdator( String updator ) {
        this.updator = updator;
    }

    @Override
    @Transient
    /**
     * Property not saved, only for auditing
     */
    public UserContext getLocalUserContext() {
        return this.localContext;
    }

    @Override
    public void setLocalUserContext(UserContext context) {
        this.localContext = context;
    }
}
