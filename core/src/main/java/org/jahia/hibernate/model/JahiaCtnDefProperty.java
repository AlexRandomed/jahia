/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

 package org.jahia.hibernate.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;

/**
 * @hibernate.class table="jahia_ctn_def_properties"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaCtnDefProperty implements Serializable {

    private static final long serialVersionUID = -1374545799847770866L;

// ------------------------------ FIELDS ------------------------------

    /**
     * identifier field
     */
    private Integer idJahiaCtnDefProperties;

    /**
     * nullable persistent field
     */
    private Integer pageDefinitionId;

    /**
     * persistent field
     */
    private org.jahia.hibernate.model.JahiaCtnDef jahiaCtnDef;

    /**
     * persistent field
     */
    private Set<JahiaCtnStruct> jahiaCtnStructs;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * default constructor
     */
    public JahiaCtnDefProperty() {
        jahiaCtnStructs = new HashSet<JahiaCtnStruct>(53);
    }

    /**
     * minimal constructor
     */
    public JahiaCtnDefProperty(Integer idJahiaCtnDefProperties, org.jahia.hibernate.model.JahiaCtnDef jahiaCtnDef,
                               Set<JahiaCtnStruct> jahiaCtnStructs) {
        this.idJahiaCtnDefProperties = idJahiaCtnDefProperties;
        this.jahiaCtnDef = jahiaCtnDef;
        this.jahiaCtnStructs = jahiaCtnStructs;
    }

    /**
     * full constructor
     */
    public JahiaCtnDefProperty(Integer idJahiaCtnDefProperties, Integer pagedefidJahiaCtnDefProp,
                               org.jahia.hibernate.model.JahiaCtnDef jahiaCtnDef,
                               Set<JahiaCtnStruct> jahiaCtnStructs) {
        this.idJahiaCtnDefProperties = idJahiaCtnDefProperties;
        this.pageDefinitionId = pagedefidJahiaCtnDefProp;
        this.jahiaCtnDef = jahiaCtnDef;
        this.jahiaCtnStructs = jahiaCtnStructs;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * @hibernate.id generator-class="assigned" unsaved-value="null"
     * type="java.lang.Integer"
     * column="id_jahia_ctn_def_properties"
     *
     */
    public Integer getIdJahiaCtnDefProperties() {
        return this.idJahiaCtnDefProperties;
    }

    public void setIdJahiaCtnDefProperties(Integer idJahiaCtnDefProperties) {
        this.idJahiaCtnDefProperties = idJahiaCtnDefProperties;
    }

    /**
     * @hibernate.many-to-one not-null="true" update="true"
     * insert="true" cascade="all" column="ctndefid_jahia_ctn_def_prop"
     */
    public org.jahia.hibernate.model.JahiaCtnDef getJahiaCtnDef() {
        return this.jahiaCtnDef;
    }

    public void setJahiaCtnDef(org.jahia.hibernate.model.JahiaCtnDef jahiaCtnDef) {
        this.jahiaCtnDef = jahiaCtnDef;
    }

    /**
     * @hibernate.set lazy="false"
     * inverse="true"
     * cascade="all" order-by="rank_jahia_ctn_struct"
     * @hibernate.collection-key column="ctnsubdefid_jahia_ctn_struct"
     * @hibernate.collection-one-to-many class="org.jahia.hibernate.model.JahiaCtnStruct"
     * @hibernate.collection-cache usage="nonstrict-read-write"
     */
    public Set<JahiaCtnStruct> getJahiaCtnStructs() {
        return this.jahiaCtnStructs;
    }

    public void setJahiaCtnStructs(Set<JahiaCtnStruct> jahiaCtnStructs) {
        this.jahiaCtnStructs = jahiaCtnStructs;
    }

    /**
     * @hibernate.property column="pagedefid_jahia_ctn_def_prop"
     * length="11"
     */
    public Integer getPageDefinitionId() {
        return this.pageDefinitionId;
    }

    public void setPageDefinitionId(Integer pageDefinitionId) {
        this.pageDefinitionId = pageDefinitionId;
    }

// ------------------------ CANONICAL METHODS ------------------------

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaCtnDefProperty castOther = (JahiaCtnDefProperty) obj;
            return new EqualsBuilder()
                .append(this.getIdJahiaCtnDefProperties(), castOther.getIdJahiaCtnDefProperties())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getIdJahiaCtnDefProperties())
                .toHashCode();
    }

    public String toString() {
        return new StringBuffer(getClass().getName())
                .append("idJahiaCtnDefProperties="+getIdJahiaCtnDefProperties())
                .toString();
    }
}

