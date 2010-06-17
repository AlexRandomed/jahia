/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.rbac;

import org.jahia.services.usermanager.JahiaPrincipal;

import java.util.Set;

/**
 * Role identifier.
 * 
 * @author Sergiy Shyrkov
 */
public class RoleIdentity extends BaseIdentity implements Role, JahiaPrincipal {

    /**
     * Initializes an instance of this class.
     * 
     * @param name the name of this role
     */
    public RoleIdentity(String name) {
        super(name);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param name the name of this role
     * @param site the site key
     */
    public RoleIdentity(String name, String site) {
        super(name, site);
    }

    /**
     * Returns a set of all roles this principal has also considering
     * membership. An empty set is returned if this principal has no roles
     * assigned.
     *
     * @return a set of all roles this principal has also considering
     *         membership; an empty set is returned if this principal has no
     *         roles assigned
     * @since 6.5
     */
    public Set<Role> getRoles() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns {@code true} if this principal has the specified role, {@code
     * false} otherwise.
     *
     * @param role the application-specific role
     * @return {@code true} if this principal has the specified role, {@code
     *         false} otherwise
     * @since 6.5
     */
    public boolean hasRole(Role role) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns {@code true} if this principal is permitted to perform a server
     * action or access a resource summarized by the specified permission string
     * ("<group>/<name>").
     *
     * @param permission a permission that is being checked
     * @return {@code true} if this principal is permitted to perform a server
     *         action or access a resource summarized by the specified
     *         permission string
     * @since 6.5
     */
    public boolean isPermitted(Permission permission) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}