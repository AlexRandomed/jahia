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
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jahia.resourcebundle.DatabaseResourceBean;

import java.io.Serializable;

/**
 * @hibernate.class table="jahia_resources"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaResource implements Serializable,DatabaseResourceBean {

    /**
     * identifier field
     */
    private org.jahia.hibernate.model.JahiaResourcePK comp_id;

    /**
     * nullable persistent field
     */
    private String value;

    /**
     * full constructor
     */
    public JahiaResource(org.jahia.hibernate.model.JahiaResourcePK comp_id, String valueResource) {
        this.comp_id = comp_id;
        this.value = valueResource;
    }

    /**
     * default constructor
     */
    public JahiaResource() {
    }

    /**
     * minimal constructor
     */
    public JahiaResource(org.jahia.hibernate.model.JahiaResourcePK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.id generator-class="assigned"
     */
    public org.jahia.hibernate.model.JahiaResourcePK getComp_id() {
        return this.comp_id;
    }

    public void setComp_id(org.jahia.hibernate.model.JahiaResourcePK comp_id) {
        this.comp_id = comp_id;
    }

    public String getName() {
        return comp_id.getName();
    }

    public void setName(String name) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * @hibernate.property column="value_resource"
     * length="255"
     */
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLanguageCode() {
        return comp_id.getLanguageCode();
    }

    public void setLanguageCode(String languageCode) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("comp_id", getComp_id())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaResource castOther = (JahiaResource) obj;
            return new EqualsBuilder()
                .append(this.getComp_id(), castOther.getComp_id())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getComp_id())
                .toHashCode();
    }

}
