/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.usermanager.jcr;

import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerRoutingService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerRoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.security.Principal;
import java.util.*;

/**
 * Implementation of the JahiaGroup interface that uses the JCR API for storage
 *
 * @author rincevent
 * @since JAHIA 6.5
 */
public class JCRGroup extends JahiaGroup implements JCRPrincipal {
    private static final long serialVersionUID = 1825041318839222308L;
    public static final String J_HIDDEN = "j:hidden";
    public static final String J_EXTERNAL = "j:external";
    public static final String J_EXTERNAL_SOURCE = "j:externalSource";
    public static final String J_DISPLAYABLE_NAME = "j:displayableName";
    private static final String PROVIDER_NAME = "jcr";

    private transient static Logger logger = LoggerFactory.getLogger(JCRGroup.class);

    private String nodeUuid;
    private boolean external;
    private Properties properties = null;

    public JCRGroup(Node nodeWrapper, int siteID) {
        this(nodeWrapper, siteID, false);
    }

    public JCRGroup(Node nodeWrapper, int siteID, boolean isExternal) {
        super();
        this.mSiteID = siteID;
        try {
            this.nodeUuid = nodeWrapper.getIdentifier();
            this.mGroupname = nodeWrapper.getName();
            this.mGroupKey = mGroupname + ":" + siteID;
            this.hidden = nodeWrapper.getProperty(J_HIDDEN).getBoolean();
            initMembersMap(nodeWrapper);
        } catch (RepositoryException e) {
            logger.error("Error while accessing repository", e);
        }
        this.external = isExternal;
    }

    /**
     * Get grp's properties list.
     *
     * @return Return a reference on the grp's properties list, or null if no
     * property is present.
     */
    public Properties getProperties() {
        if (properties == null) {
            try {
                JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
                properties = new Properties();
                JCRGroupNode jcrGroupNode = (JCRGroupNode) getNode(session);
                PropertyIterator iterator = jcrGroupNode.getProperties();
                for (; iterator.hasNext(); ) {
                    Property property = iterator.nextProperty();
                    if (!property.isMultiple()) {
                        properties.put(property.getName(), property.getString());
                    }
                }
            } catch (RepositoryException e) {
                logger.error("Error while retrieving group properties", e);
            }
        } else {
            return properties;
        }
        return null;
    }

    /**
     * Retrieve the requested grp property.
     *
     * @param key Property's name.
     * @return Return the property's value of the specified key, or null if the
     * property does not exist.
     */
    public String getProperty(final String key) {
        if (properties != null) {
            return (String) properties.get(key);
        }
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            return getNode(session).getProperty(key).getString();
        } catch (PathNotFoundException pnfe) {
            // This is expected in the case the property doesn't exist in the repository. We will simply return null.
            return null;
        } catch (RepositoryException e) {
            logger.error("Error while retrieving group property " + key, e);
            return null;
        }
    }

    /**
     * Remove the specified property from the properties list.
     *
     * @param key Property's name.
     */
    public boolean removeProperty(final String key) {
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node node = getNode(session);
                    Property property = node.getProperty(key);
                    if (property != null) {
                        if (properties != null) {
                            properties.remove(key);
                        }
                        property.remove();
                        session.save();
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }
            });
        } catch (RepositoryException e) {
            logger.warn("Error while removing property " + key, e);
        }
        return false;
    }

    /**
     * Add (or update if not already in the property list) a property key-value
     * pair in the grp's properties list.
     *
     * @param key   Property's name.
     * @param value Property's value.
     */
    public boolean setProperty(final String key, final String value) {
        try {
            if (J_EXTERNAL.equals(key)) {
                external = Boolean.valueOf(value);
            }
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            Node node = getNode(session);
            node.setProperty(key, value);
            session.save();
            if (properties != null) {
                properties.put(key, value);
            }
            return Boolean.TRUE;
        } catch (RepositoryException e) {
            logger.warn("Error while setting property " + key + " with value " + value, e);
        }
        return false;
    }

    /**
     * Adds the specified member to the group.
     *
     * @param principal The principal to add to this group.
     * @return Return true if the member was successfully added, false if the
     * principal was already a member.
     */
    public boolean addMember(final Principal principal) {
        if (null == principal || this.equals(principal)) {
            return false;
        }
        try {
            boolean memberAdded = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    if (isMember(principal)) {
                        return false;
                    }
                    JCRPrincipal jcrUser = null;
                    String name = principal.getName();
                    if (principal instanceof JCRUser) {
                        jcrUser = (JCRPrincipal) principal;
                    } else if (principal instanceof JCRGroup) {
                        name = name + "___" + ((JCRGroup) principal).getSiteID();
                        jcrUser = (JCRPrincipal) principal;
                    } else if (principal instanceof JahiaUser) {
                        JCRTemplate.getInstance().getProvider("/").deployExternalUser((JahiaUser) principal);
                        jcrUser = (JCRUser) JCRUserManagerProvider.getInstance().lookupExternalUser((JahiaUser) principal);
                    } else if (principal instanceof JahiaGroup) {
                        JCRTemplate.getInstance().getProvider("/").deployExternalGroup((JahiaGroup) principal);
                        jcrUser = (JCRGroup) JCRGroupManagerProvider.getInstance().lookupExternalGroup(principal.getName());
                    }
                    if (jcrUser != null) {
                        Node node = getNode(session);
                        Node members = node.getNode("j:members");
                        if (!members.hasNode(name)) {
                            Node member = members.addNode(name, Constants.JAHIANT_MEMBER);
                            member.setProperty("j:member", jcrUser.getIdentifier());
                            session.save();
                            JCRGroupManagerProvider.getInstance().updateMembershipCache(jcrUser.getIdentifier());
                        }
                        mMembers.add(principal);

                        return true;
                    }
                    return false;
                }
            });
            JCRGroupManagerProvider.getInstance().invalidateCacheRecursively(this);
            return memberAdded;
        } catch (RepositoryException e) {
            logger.error("Error while adding group member", e);
        }

        return false;
    }

    /**
     * Returns a hashcode for this principal.
     *
     * @return A hashcode for this principal.
     */
    public int hashCode() {
        return nodeUuid.hashCode();
    }

    /**
     * Returns members of this group. If members were not loaded before,
     * forces loading.
     *
     * @return members of this group
     */
    protected Set<Principal> getMembersMap() {
        if (mMembers == null) {
            initMembers();
        }
        return new HashSet<Principal>(mMembers);
    }

    protected void initMembers() {
        if (mMembers == null) {
            try {
                JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
                initMembersMap(getNode(session));
            } catch (RepositoryException e) {
                logger.error("Error while retrieving group member map", e);
            }
        }
    }

    private void initMembersMap(Node node) throws RepositoryException {
        if (mMembers == null) {
            Set<Principal> principals = new HashSet<Principal>();
            Node members = node.getNode("j:members");
            NodeIterator iterator = members.getNodes();
            while (iterator.hasNext()) {
                Node member = (Node) iterator.next();
                if (member.isNodeType(Constants.JAHIANT_MEMBER)) {
                    if (!member.hasProperty("j:member")) {
                        logger.warn("Missing member property for group " + mGroupname + "(key=" + mGroupKey + "), ignoring group member " + member.getName() + "...");
                        continue;
                    }
                    Property memberProperty = member.getProperty("j:member");
                    Node memberNode = null;
                    try {
                        memberNode = memberProperty.getNode();
                    } catch (ItemNotFoundException infe) {
                        logger.warn("Couldn't find group member " + member.getName() + "(uuid=" + memberProperty.getString() + ") for group " + mGroupname + "(key=" + mGroupKey + "), ignoring...");
                    }
                    if (memberNode != null) {
                        if (memberNode.isNodeType(Constants.JAHIANT_USER)) {
                            JahiaUser jahiaUser = JahiaUserManagerRoutingService.getInstance().lookupUser(member.getName());
                            if (jahiaUser != null) {
                                principals.add(jahiaUser);
                            } else if (logger.isDebugEnabled()) {
                                logger.debug("Member '" + member.getName() + "' cannot be found for group '" + node.getName()
                                        + "'");
                            }
                        } else if (memberNode.isNodeType(Constants.JAHIANT_GROUP)) {
                            JahiaGroup g;
                            if (memberNode.getPath().startsWith("/groups")) {
                                g = JahiaGroupManagerRoutingService.getInstance().lookupGroup(null, memberNode.getName());
                            } else {
                                g = JahiaGroupManagerRoutingService.getInstance().lookupGroup(((JCRGroupNode) memberNode).getResolveSite().getName(), memberNode.getName());
                            }
                            if (g != null) {
                                principals.add(g);
                            } else if (logger.isDebugEnabled()) {
                                logger.debug("Member '" + member.getName() + "' cannot be found for group '" + node.getName()
                                        + "'");
                            }
                        }
                    }
                }
            }
            mMembers = principals;
            preloadedGroups = true;
        }
    }

    /**
     * Removes the specified member from the group.
     *
     * @param principal The principal to remove from this group.
     * @return Return true if the principal was removed, or false if the
     * principal was not a member.
     */
    public boolean removeMember(final Principal principal) {
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node group = getNode(session);
                    Node members = group.getNode("j:members");
                    String memberUuid = null;
                    if (principal instanceof JCRPrincipal) {
                        // internal user/group
                        memberUuid = ((JCRPrincipal) principal).getIdentifier();
                    } else if (principal instanceof JahiaUser) {
                        // external user
                        JCRUser externalUser = JCRUserManagerProvider.getInstance().lookupExternalUser((JahiaUser) principal);
                        if (externalUser != null) {
                            memberUuid = externalUser.getIdentifier();
                        } else {
                            logger.warn("User node for an external user with the name '" + principal.getName()
                                    + " cannot be found. Skip removing user from group " + group.getPath());
                        }
                    } else if (principal instanceof JahiaGroup) {
                        // external user
                        JCRGroup externalGroup = JCRGroupManagerProvider.getInstance().lookupExternalGroup(((JahiaGroup) principal).getGroupname());
                        if (externalGroup != null) {
                            memberUuid = externalGroup.getIdentifier();
                        } else {
                            logger.warn("JCR node for an external group with the name '" + principal.getName()
                                    + " cannot be found. Skip removing principal from group " + group.getPath());
                        }
                    } else {
                        logger.warn("Cannot remove membership for principal " + principal + " in group "
                                + group.getPath() + ". Do not know how to handle this principal type.");
                    }
                    return memberUuid != null ? removeMember(session, members, memberUuid) : Boolean.FALSE;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while removing member", e);
        }
        return false;
    }

    private boolean removeMember(JCRSessionWrapper session, Node members, String memberIdentifier) throws RepositoryException {
        if (session.getWorkspace().getQueryManager() != null) {
            String query = "SELECT * FROM [jnt:member] as m where m.[j:member] = '" + memberIdentifier + "' AND ISCHILDNODE(m, '" + members.getPath() + "') ORDER BY localname(m)";
            Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
            QueryResult qr = q.execute();
            NodeIterator nodes = qr.getNodes();
            while (nodes.hasNext()) {
                Node memberNode = nodes.nextNode();
                memberNode.remove();
            }
            session.save();
            mMembers = null;
            initMembers();
            JCRGroupManagerProvider.getInstance().updateMembershipCache(memberIdentifier);
            JCRGroupManagerProvider.getInstance().invalidateCacheRecursively(this);
            return true;
        }
        return false;
    }

    /**
     * Returns a string representation of this group.
     *
     * @return A string representation of this group.
     */
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder(128);
        output.append("Details of group [").append(mGroupname).append("] :\n");

        output.append("  - ID : ").append(getIdentifier()).append("\n");

        try {
            output.append("  - properties :");
            if (properties != null && !properties.isEmpty()) {
                output.append("\n");
                for (Map.Entry<Object, Object> property : properties.entrySet()) {
                    output.append("       ").append(property.getKey()).append(" -> [").append(property.getValue()).append(
                            "]\n");
                }
            } else {
                output.append(" -no properties-\n");
            }

            // Add the user members useranames detail
            output.append("  - members : ");

            if (mMembers != null) {
                if (mMembers.size() > 0) {
                    for (Principal member : mMembers) {
                        output.append(member.getName()).append("/");
                    }
                } else {
                    output.append(" -no members-\n");
                }
            } else {
                output.append(" -preloading of members disabled-\n");
            }
        } catch (Exception e) {
            // Group might be already deleted
            logger.debug("Error while generating toString output for group " + mGroupname, e);
        }

        return output.toString();
    }

    /**
     * Get the name of the provider of this group.
     *
     * @return String representation of the name of the provider of this group
     */
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    public JCRNodeWrapper getNode(JCRSessionWrapper session) throws RepositoryException {
        return session.getNodeByIdentifier(getIdentifier());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JCRGroup jcrGroup = (JCRGroup) o;

        return nodeUuid.equals(jcrGroup.nodeUuid);

    }

    public String getIdentifier() {
        return nodeUuid;
    }

    /**
     * @return the external
     */
    public boolean isExternal() {
        return external;
    }

    public void addMembers(final Collection<Principal> principals) {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    for (Principal principal : principals) {
                        if (isMember(principal)) {
                            continue;
                        }
                        if (null == principal || JCRGroup.this.equals(principal)) {
                            continue;
                        }
                        JCRPrincipal jcrUser = null;
                        String name = principal.getName();
                        if (principal instanceof JCRUser) {
                            jcrUser = (JCRPrincipal) principal;
                        } else if (principal instanceof JCRGroup) {
                            name = name + "___" + ((JCRGroup) principal).getSiteID();
                            jcrUser = (JCRPrincipal) principal;
                        } else if (principal instanceof JahiaUser) {
                            JCRTemplate.getInstance().getProvider("/").deployExternalUser((JahiaUser) principal);
                            jcrUser = (JCRUser) JCRUserManagerProvider.getInstance().lookupExternalUser((JahiaUser) principal);
                        } else if (principal instanceof JahiaGroup) {
                            JCRTemplate.getInstance().getProvider("/").deployExternalGroup((JahiaGroup) principal);
                            jcrUser = (JCRGroup) JCRGroupManagerProvider.getInstance().lookupExternalGroup(principal.getName());
                        }
                        if (jcrUser != null) {
                            Node node = getNode(session);
                            Node members = node.getNode("j:members");
                            if (!members.hasNode(name)) {
                                Node member = members.addNode(name, Constants.JAHIANT_MEMBER);
                                member.setProperty("j:member", jcrUser.getIdentifier());
                                JCRGroupManagerProvider.getInstance().updateMembershipCache(jcrUser.getIdentifier());
                            }
                            mMembers.add(principal);
                        }
                    }
                    session.save();
                    return null;
                }
            });
            JCRGroupManagerProvider.getInstance().invalidateCacheRecursively(this);
        } catch (RepositoryException e) {
            logger.error("Error while adding group member", e);
        }
    }
}
