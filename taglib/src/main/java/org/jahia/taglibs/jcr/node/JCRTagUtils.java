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

package org.jahia.taglibs.jcr.node;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * JCR content related utilities.
 * User: jahia
 * Date: 28 mai 2009
 * Time: 15:46:07
 */
public class JCRTagUtils {
    private static final transient Logger logger = Logger.getLogger(JCRTagUtils.class);

    /**
     * Get the node or property display name depending on the locale
     *
     * @param item the item to get the label for
     * @param locale current locale
     * @return the node or property display name depending on the locale
     */
    public static String label(Object nodeObject, Locale locale) {
        return JCRContentUtils.getDisplayLabel(nodeObject, locale);
    }

    /**
     * Get the label value depending on the locale
     *
     * @param nodeObject
     * @param locale as a string
     * @return
     */
    public static String label(Object nodeObject, String locale) {
        return label(nodeObject, LanguageCodeConverters.languageCodeToLocale(locale));
    }

    /**
     * Returns <code>true</code> if the current node has the specified type or at least one of the specified node types.
     * 
     * @param node current node to check the type
     * @param type the node type name to match or a comma-separated list of node
     *            types (at least one should be matched)
     * @return <code>true</code> if the current node has the specified type or at least one of the specified node types
     */
    public static boolean isNodeType(JCRNodeWrapper node, String type) {
        if (node == null) {
            throw new IllegalArgumentException("The specified node is null");
        }
        
        boolean hasType = false;
        try {
            if (type.contains(",")) {
                for (String typeToCheck : StringUtils.split(type, ',')) {
                    if (node.isNodeType(typeToCheck.trim())) {
                        hasType = true;
                        break;
                    }
                }
            } else {
                hasType = node.isNodeType(type);
            }
        } catch (RepositoryException e) {
            logger.error(e, e);
        }
        
        return hasType;
    }

    public static NodeIterator getNodes(JCRNodeWrapper node, String type) {
        return JCRContentUtils.getNodes(node, type);
    }

    /**
     * Returns <code>true</code> if the current node has at least one child node
     * of the specified type.
     * 
     * @param node current node whose children will be queried
     * @param type the node type name to match or a comma-separated list of node
     *            types (at least one should be matched)
     * @return <code>true</code> if the current node has at least one child node
     *         of the specified type
     */
    public static boolean hasChildrenOfType(JCRNodeWrapper node, String type) {
        boolean hasChildrenOfType = false;
        String[] typesToCheck = StringUtils.split(type, ',');
        try {
            for (NodeIterator iterator = node.getNodes(); iterator.hasNext() && !hasChildrenOfType;) {
                Node child = iterator.nextNode();
                for (String matchType : typesToCheck) {
                    if (child.isNodeType(matchType)) {
                        hasChildrenOfType = true;
                        break;
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
        return hasChildrenOfType;
    }

    /**
     * Returns an iterator with the child nodes of the current node, which match
     * the specified node type name. This is an advanced version of the
     * {@link #getNodes(JCRNodeWrapper, String)} method to handle multiple node
     * types.
     * 
     * @param node current node whose children will be queried
     * @param type the node type name to match or a comma-separated list of node
     *            types (at least one should be matched)
     * @return an iterator with the child nodes of the current node, which match
     *         the specified node type name
     */
    public static NodeIterator getChildrenOfType(JCRNodeWrapper node, String type) {
        return JCRContentUtils.getChildrenOfType(node, type);
    }

    /**
     * Returns an iterator with the descendant nodes of the current node, which match
     * the specified node type name.
     * 
     * @param node current node whose descendants will be queried
     * @param type the node type name to match
     * @return an iterator with the descendant nodes of the current node, which match
     *         the specified node type name
     */
    public static NodeIterator getDescendantNodes(JCRNodeWrapper node, String type) {
        return JCRContentUtils.getDescendantNodes(node, type);
    }

    public static Map<String, String> getPropertiesAsStringFromNodeNameOfThatType(JCRNodeWrapper nodeContainingProperties,JCRNodeWrapper nodeContainingNodeNames, String type) {
        NodeIterator nodeNames = getNodes(nodeContainingNodeNames,type);
        Map<String, String> props = new LinkedHashMap<String, String>();
        while (nodeNames.hasNext()) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeNames.next();
            final String name = nodeWrapper.getName();
            try {
                JCRPropertyWrapper property = nodeContainingProperties.getProperty(name);
                String value;
                if(property.isMultiple()) {
                    value = property.getValues()[0].getString();
                } else {
                value = property.getValue().getString();
                }
                props.put(name,value);
            } catch (PathNotFoundException e) {
                logger.debug(e.getMessage(), e);
            } catch (ValueFormatException e) {
                logger.error(e.getMessage(), e);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return props;
    }

    /**
     * Returns all the parents of the current node that have the specified node type. If no matching node is found, an
     * empty list.
     * 
     * @param node
     *            the current node to start the lookup from
     * @param type
     *            the required type of the parent node(s)
     * @return the parents of the current node that have the specified node type. If no matching node is found, an
     *         empty list is returned
     */
    public static List<JCRNodeWrapper> getParentsOfType(JCRNodeWrapper node,
            String type) {
        
        List<JCRNodeWrapper> parents = new ArrayList<JCRNodeWrapper>();
        do {
            node = getParentOfType(node, type);
            if (node != null) {
                parents.add(node);
            }
        } while (node != null);

        return parents;
    }    
    
    /**
     * Returns the first parent of the current node that has the specified node type. If no matching node is found, <code>null</code> is
     * returned.
     * 
     * @param node
     *            the current node to start the lookup from
     * @param type
     *            the required type of the parent node
     * @return the first parent of the current node that has the specified node type. If no matching node is found, <code>null</code> is
     *         returned
     */
    public static JCRNodeWrapper getParentOfType(JCRNodeWrapper node,
            String type) {
        JCRNodeWrapper matchingParent = null;
        try {
            JCRNodeWrapper parent = node.getParent();
            while (parent != null) {
                if (parent.isNodeType(type)) {
                    matchingParent = parent;
                    break;
                }
                parent = parent.getParent();
            }
        } catch (ItemNotFoundException e) {
            // we reached the hierarchy top
        } catch (RepositoryException e) {
            logger.error("Error while retrieving nodes parent node. Cause: "
                    + e.getMessage(), e);
        }
        return matchingParent;
    }

    public static Map<String, JahiaGroup> getUserMembership(JCRNodeWrapper user) {
        Map<String, JahiaGroup> map = new LinkedHashMap<String, JahiaGroup>();
        final JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(
                user.getName());
        final JahiaGroupManagerService managerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        final List<String> userMembership = managerService.getUserMembership(
                jahiaUser);
        for (String groupName : userMembership) {
            final JahiaGroup group = managerService.lookupGroup(groupName);
            map.put(groupName,group);
        }
        return map;
    }

    public static boolean hasPermission(JCRNodeWrapper node,String permission) {
        String perm = permission.toLowerCase();
        if("read_live".equals(perm)) {
            return node.hasPermission(JCRNodeWrapper.READ_LIVE);
        }
        else if("read".equals(perm)) {
            return node.hasPermission(JCRNodeWrapper.READ);
        }
        else if("write_live".equals(perm)) {
            return node.hasPermission(JCRNodeWrapper.WRITE_LIVE);
        }
        else if("write".equals(perm)) {
            return node.hasPermission(JCRNodeWrapper.WRITE);
        }
        else if("modify_acls".equals(perm)) {
            return node.hasPermission(JCRNodeWrapper.MODIFY_ACL);
        }
        return false;
    }

    public static String humanReadableFileLength(JCRNodeWrapper node) {
        return FileUtils.byteCountToDisplaySize(node.getFileContent().getContentLength());
    }

    /**
     * Returns all the parents of the current node that have the specified node type. If no matching node is found, an
     * empty list.
     *
     * @param node
     *            the current node to start the lookup from
     * @param type
     *            the required type of the parent node(s)
     * @return the parents of the current node that have the specified node type. If no matching node is found, an
     *         empty list is returned
     */
    public static List<JCRNodeWrapper> getMeAndParentsOfType(JCRNodeWrapper node,String type) {

        List<JCRNodeWrapper> parents = new ArrayList<JCRNodeWrapper>();
        try {
            if(node.isNodeType(type)) {
                parents.add(node);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        do {
            node = getParentOfType(node, type);
            if (node != null) {
                parents.add(node);
            }
        } while (node != null);

        return parents;
    }

    public static boolean hasOrderableChildNodes(JCRNodeWrapper node) {
        try {
            return node.getPrimaryNodeType().hasOrderableChildNodes();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * @see org.apache.jackrabbit.util.Text#escapeIllegalJcrChars(String)
     */    
    public static String escapeIllegalJcrChars(String inputString) {
        return Text.escapeIllegalJcrChars(inputString);
    }
    
    /**
     * Returns the full user name, including first and last name. If those are
     * not available, returns the username.
     * 
     * @param userNode the user JCR node
     * @return the full user name, including first and last name. If those are
     *         not available, returns the username
     */
    public static String userFullName(JCRNodeWrapper userNode) {
        StringBuilder name = new StringBuilder();
        String value = userNode.getPropertyAsString("j:firstName");
        if (StringUtils.isNotEmpty(value)) {
            name.append(value);
        }
        value = userNode.getPropertyAsString("j:lastName");
        if (StringUtils.isNotEmpty(value)) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(value);
        }

        if (name.length() == 0) {
            name.append(userNode.getName());
        }

        return name.toString();
    }
}
