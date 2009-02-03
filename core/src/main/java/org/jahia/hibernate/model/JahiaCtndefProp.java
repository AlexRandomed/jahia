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

/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import java.io.Serializable;

/**
 * @hibernate.class table="jahia_ctndef_prop"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaCtndefProp implements Serializable {

    /**
     * identifier field
     */
    private org.jahia.hibernate.model.JahiaCtndefPropPK comp_id;

    /**
     * nullable persistent field
     */
    private String valueJahiaCtndefProp;

    /**
     * full constructor
     */
    public JahiaCtndefProp(org.jahia.hibernate.model.JahiaCtndefPropPK comp_id, String valueJahiaCtndefProp) {
        this.comp_id = comp_id;
        this.valueJahiaCtndefProp = valueJahiaCtndefProp;
    }

    /**
     * default constructor
     */
    public JahiaCtndefProp() {
    }

    /**
     * minimal constructor
     */
    public JahiaCtndefProp(org.jahia.hibernate.model.JahiaCtndefPropPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.id generator-class="assigned"
     */
    public org.jahia.hibernate.model.JahiaCtndefPropPK getComp_id() {
        return this.comp_id;
    }

    public void setComp_id(org.jahia.hibernate.model.JahiaCtndefPropPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.property column="value_jahia_ctndef_prop"
     * length="255"
     */
    public String getValueJahiaCtndefProp() {
        return this.valueJahiaCtndefProp;
    }

    public void setValueJahiaCtndefProp(String valueJahiaCtndefProp) {
        this.valueJahiaCtndefProp = valueJahiaCtndefProp;
    }

    public String toString() {
        return new StringBuffer(getClass().getName())
                .append("comp_id="+getComp_id())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaCtndefProp castOther = (JahiaCtndefProp) obj;
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
