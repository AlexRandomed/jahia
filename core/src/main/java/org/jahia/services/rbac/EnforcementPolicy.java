/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.services.usermanager.GuestGroup;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.jcr.JCRUser;

/**
 * Defines the role/permission checking policy.
 * 
 * @author Sergiy Shyrkov
 */
public class EnforcementPolicy {

    public static class DenyAllEnforcementPolicy extends EnforcementPolicy {

        @Override
        public boolean isDenyAll() {
            return true;
        }

    }

    public static class EnforcementPolicyResult {
        private boolean applied = true;
        private boolean granted;

        public EnforcementPolicyResult(boolean granted) {
            super();
            this.granted = granted;
        }

        public boolean getResult() {
            return granted;
        }

        public boolean isApplied() {
            return applied;
        }

    }

    public static class GrantAllEnforcementPolicy extends EnforcementPolicy {

        @Override
        public boolean isGrantAll() {
            return true;
        }

    }

    private static final EnforcementPolicyResult NON_MATCHED_POLICY_RESULT = new EnforcementPolicyResult(false) {

        @Override
        public boolean getResult() {
            throw new UnsupportedOperationException("The policy is not applied on the principal."
                    + " The value for 'granted' is undefined.");
        }

        @Override
        public boolean isApplied() {
            return false;
        }
    };

    private boolean denyAll = false;

    private boolean denyAllToGuest = true;

    private boolean grantAll = true;

    private boolean grantAllToRoot = true;

    private boolean permitNonExistingPermissions = false;

    private boolean permitNonExistingRoles = false;

    /**
     * Initializes an instance of this class.
     */
    public EnforcementPolicy() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param denyAll
     * @param grantAll
     * @param grantAllToRoot
     * @param denyAllToGuest
     */
    public EnforcementPolicy(boolean denyAll, boolean grantAll, boolean grantAllToRoot, boolean denyAllToGuest) {
        this();
        this.denyAll = denyAll;
        this.grantAll = grantAll;
        this.grantAllToRoot = grantAllToRoot;
        this.denyAllToGuest = denyAllToGuest;
    }

    public EnforcementPolicyResult enforce(JahiaPrincipal principal) {
        EnforcementPolicyResult result = NON_MATCHED_POLICY_RESULT;

        if (isDenyAll()) {
            // we deny all to any user
            result = new EnforcementPolicyResult(false);
        } else if (isGrantAll()) {
            // we grant all to any user
            result = new EnforcementPolicyResult(true);
        } else if (isGrantAllToRoot() && isRoot(principal)) {
            // we grant all to root user
            result = new EnforcementPolicyResult(true);
        } else if (isDenyAllToGuest() && isGuest(principal)) {
            // we deny all to guest
            result = new EnforcementPolicyResult(false);
        }

        return result;
    }

    /**
     * Returns {@code true} if any role/permission is silently denied to any
     * principal.
     * 
     * @return {@code true} if any role/permission is silently denied to any
     *         principal
     */
    public boolean isDenyAll() {
        return denyAll;
    }

    /**
     * Returns {@code true} if the guest user/group is silently denied all
     * roles/permissions, i.e. the role/permission check always evaluates to $
     * {@code false}.
     * 
     * @return {@code true} if the guest user/group is silently denied all
     *         roles/permissions, i.e. the role/permission check always
     *         evaluates to ${@code false}
     */
    public boolean isDenyAllToGuest() {
        return denyAllToGuest;
    }

    /**
     * Returns {@code true} if any role/permission is silently granted to any
     * principal.
     * 
     * @return {@code true} if any role/permission is silently granted to any
     *         principal
     */
    public boolean isGrantAll() {
        return grantAll;
    }

    /**
     * Returns {@code true} if the system root user is silently granted all
     * roles/permissions, i.e. the role/permission check always evaluates to $
     * {@code true}.
     * 
     * @return {@code true} if the system root user is silently granted all
     *         roles/permissions, i.e. the role/permission check always
     *         evaluates to ${@code true}
     */
    public boolean isGrantAllToRoot() {
        return grantAllToRoot;
    }

    /**
     * Test if the current principal is the guest user.
     * 
     * @return {@code true} if this principal is the guest user
     */
    protected boolean isGuest(JahiaPrincipal principal) {
        return (principal instanceof GuestGroup) || (principal instanceof JahiaUser)
                && JahiaUserManagerService.isGuest((JahiaUser) principal);
    }

    /**
     * If the requested permission is not found in the system the
     * {@link JahiaPrincipal#isPermitted(Permission)} will always evaluate to
     * <code>true</code> for this permission.
     * 
     * @return <code>true</code> if the
     *         {@link JahiaPrincipal#isPermitted(Permission)} will always
     *         evaluate to <code>true</code> for non-existing permissions
     */
    public boolean isPermitNonExistingPermissions() {
        return permitNonExistingPermissions;
    }

    /**
     * Returns <code>true</code> if the {@link JahiaPrincipal#hasRole(Role)} is
     * forced to always evaluate to <code>true</code> for non-existing roles.
     * 
     * @return <code>true</code> if the {@link JahiaPrincipal#hasRole(Role)} is
     *         forced to always evaluate to <code>true</code> for non-existing
     *         roles
     */
    public boolean isPermitNonExistingRoles() {
        return permitNonExistingRoles;
    }

    /**
     * Test if the current principal is the root system user.
     * 
     * @return {@code true} if this principal is the root system user
     */
    protected boolean isRoot(JahiaPrincipal principal) {
        return (principal instanceof JCRUser) && ((JCRUser) principal).isRoot();
    }

    /**
     * Sets the value for the <code>denyAll</code> property.
     * 
     * @param denyAll the denyAll to set
     * @see ${@link #isDenyAll()}
     */
    public void setDenyAll(boolean denyAll) {
        this.denyAll = denyAll;
    }

    /**
     * Sets the value for the <code>denyAllToGuest</code> property
     * 
     * @param denyAllToGuest the denyAllToGuest to set
     * @see ${@link #isDenyAllToGuest()}
     */
    public void setDenyAllToGuest(boolean denyAllToGuest) {
        this.denyAllToGuest = denyAllToGuest;
    }

    /**
     * Sets the value for the <code>grantAll</code> property.
     * 
     * @param grantAll the grantAll to set
     * @see ${@link #isGrantAll()}
     */
    public void setGrantAll(boolean grantAll) {
        this.grantAll = grantAll;
    }

    /**
     * Sets the value for the <code>grantAllToRoot</code> property.
     * 
     * @param grantAllToRoot the grantAllToRoot to set
     * @see ${@link #isGrantAllToRoot()}
     */
    public void setGrantAllToRoot(boolean grantAllToRoot) {
        this.grantAllToRoot = grantAllToRoot;
    }

    /**
     * Set this to true to force {@link JahiaPrincipal#isPermitted(Permission)}
     * always evaluate to <code>true</code> for non-existing permissions.
     * 
     * @param permitNonExistingPermission <code>true</code> if the
     *            {@link JahiaPrincipal#isPermitted(Permission)} will always
     *            evaluate to <code>true</code> for non-existing permissions
     */
    public void setPermitNonExistingPermission(boolean permitNonExistingPermission) {
        this.permitNonExistingPermissions = permitNonExistingPermission;
    }

    /**
     * Set this to true to force {@link JahiaPrincipal#hasRole(Role)} always
     * evaluate to <code>true</code> for non-existing roles.
     * 
     * @param permitNonExistingRoles <code>true</code> if the
     *            {@link JahiaPrincipal#hasRole(Role)} will always evaluate to
     *            <code>true</code> for non-existing roles
     */
    public void setPermitNonExistingRoles(boolean permitNonExistingRoles) {
        this.permitNonExistingRoles = permitNonExistingRoles;
    }

}