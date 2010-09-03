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

package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.value.StringValue;
import org.apache.log4j.Logger;
import org.apache.tika.io.IOUtils;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.content.server.GWTFileManagerUploadServlet;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.categories.Category;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.*;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.PropertyDefinition;

import java.io.FileInputStream;
import java.util.*;

/**
 * Helper class for setting node properties based on GWT bean values.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:45:42 PM
 */
public class PropertiesHelper {
    private static Logger logger = Logger.getLogger(PropertiesHelper.class);

    private ContentDefinitionHelper contentDefinition;
    private ContentManagerHelper contentManager;
    private NavigationHelper navigation;

    public void setContentDefinition(ContentDefinitionHelper contentDefinition) {
        this.contentDefinition = contentDefinition;
    }

    public void setContentManager(ContentManagerHelper contentManager) {
        this.contentManager = contentManager;
    }

    public void setNavigation(NavigationHelper navigation) {
        this.navigation = navigation;
    }

    public Map<String, GWTJahiaNodeProperty> getProperties(String path, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        JCRNodeWrapper objectNode;
        try {
            objectNode = currentUserSession.getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        Map<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>();
        String propName = "null";
        try {
            PropertyIterator it = objectNode.getProperties();
            while (it.hasNext()) {
                Property prop = it.nextProperty();
                PropertyDefinition def = prop.getDefinition();
                // definition can be null if the file is versionned
                if (def != null) {
                    propName = def.getName();
                    // create the corresponding GWT bean
                    GWTJahiaNodeProperty nodeProp = new GWTJahiaNodeProperty();
                    nodeProp.setName(propName);
                    nodeProp.setMultiple(def.isMultiple());
                    Value[] values;
                    if (!def.isMultiple()) {
                        Value oneValue = prop.getValue();
                        values = new Value[]{oneValue};
                    } else {
                        values = prop.getValues();
                    }
                    List<GWTJahiaNodePropertyValue> gwtValues = new ArrayList<GWTJahiaNodePropertyValue>(values.length);

                    for (Value val : values) {
                        GWTJahiaNodePropertyValue convertedValue = contentDefinition.convertValue(val, def.getRequiredType()); 
                        if (convertedValue != null) {
                            gwtValues.add(convertedValue);
                        }
                    }
                    nodeProp.setValues(gwtValues);
                    props.put(nodeProp.getName(), nodeProp);
                } else {
                    logger.debug("The following property has been ignored " + prop.getName() + "," + prop.getPath());
                }
            }
            NodeIterator ni = objectNode.getNodes();
            while (ni.hasNext()) {
                Node node = ni.nextNode();
                if (node.isNodeType(Constants.NT_RESOURCE)) {
                    NodeDefinition def = node.getDefinition();
                    propName = def.getName();
                    // create the corresponding GWT bean
                    GWTJahiaNodeProperty nodeProp = new GWTJahiaNodeProperty();
                    nodeProp.setName(propName);
                    List<GWTJahiaNodePropertyValue> gwtValues = new ArrayList<GWTJahiaNodePropertyValue>();
                    gwtValues.add(new GWTJahiaNodePropertyValue(node.getProperty(Constants.JCR_MIMETYPE).getString(), GWTJahiaNodePropertyType.ASYNC_UPLOAD));
                    nodeProp.setValues(gwtValues);
                    props.put(nodeProp.getName(), nodeProp);
                } else if (node.isNodeType(Constants.JAHIANT_PAGE_LINK)) {

                    // case of link
                    NodeDefinition def = node.getDefinition();
                    propName = def.getName();
                    // create the corresponding GWT bean
                    GWTJahiaNodeProperty nodeProp = new GWTJahiaNodeProperty();
                    nodeProp.setName(propName);
                    List<GWTJahiaNodePropertyValue> gwtValues = new ArrayList<GWTJahiaNodePropertyValue>();
                    GWTJahiaNode linkNode = navigation.getGWTJahiaNode((JCRNodeWrapper) node);
                    if (node.isNodeType(Constants.JAHIANT_NODE_LINK)) {
                        linkNode.set("linkType", "internal");
                    } else if (node.isNodeType(Constants.JAHIANT_EXTERNAL_PAGE_LINK)) {
                        linkNode.set("linkType", "external");
                    }

                    // url
                    if (node.hasProperty(Constants.URL)) {
                        String linkUrl = node.getProperty(Constants.URL).getValue().getString();
                        linkNode.set(Constants.URL, linkUrl);
                    }

                    // title
                    if (node.hasProperty(Constants.JCR_TITLE)) {
                        String linkTitle = node.getProperty(Constants.JCR_TITLE).getValue().getString();
                        linkNode.set(Constants.JCR_TITLE, linkTitle);
                    }

                    // alt
                    if (node.hasProperty(Constants.ALT)) {
                        String alt = node.getProperty(Constants.ALT).getValue().getString();
                        linkNode.set(Constants.ALT, alt);
                    }

                    if (node.hasProperty(Constants.NODE)) {
                        JCRValueWrapper weekReference = (JCRValueWrapper) node.getProperty("j:node").getValue();
                        Node pageNode = weekReference.getNode();
                        linkNode.set(Constants.NODE, navigation.getGWTJahiaNode((JCRNodeWrapper) pageNode));
                        linkNode.set(Constants.ALT, pageNode.getName());
                        linkNode.set(Constants.URL, ((JCRNodeWrapper) pageNode).getUrl());
                        linkNode.set(Constants.JCR_TITLE, ((JCRNodeWrapper) pageNode).getUrl());

                    }


                    GWTJahiaNodePropertyValue proper = new GWTJahiaNodePropertyValue(linkNode, GWTJahiaNodePropertyType.PAGE_LINK);
                    gwtValues.add(proper);
                    nodeProp.setValues(gwtValues);
                    props.put(nodeProp.getName(), nodeProp);
                }
            }
        } catch (RepositoryException e) {
            logger.error("Cannot access property " + propName + " of node " + objectNode.getName(), e);
        }
        return props;
    }

    /**
     * A batch-capable save properties method.
     *
     * @param currentUserSession
     * @param nodes    the nodes to save the properties of
     * @param newProps the new properties
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          sthg bad happened
     */
    public void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps, JahiaUser user, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        for (GWTJahiaNode aNode : nodes) {
            JCRNodeWrapper objectNode;
            try {
                objectNode = currentUserSession.getNode(aNode.getPath());
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                throw new GWTJahiaServiceException(new StringBuilder(aNode.getDisplayName()).append(" could not be accessed :\n").append(e.toString()).toString());
            }
            try {
                List<String> types = aNode.getNodeTypes();
                for (ExtendedNodeType mixin : objectNode.getMixinNodeTypes()) {
                    if (!types.contains(mixin.getName())) {
                        List<ExtendedItemDefinition> items = mixin.getItems();
                        for (ExtendedItemDefinition item : items) {
                            if (item.isNode()) {
                                if (objectNode.hasNode(item.getName())) {
                                    currentUserSession.checkout(objectNode);
                                    objectNode.getNode(item.getName()).remove();
                                }
                            } else {
                                if (objectNode.hasProperty(item.getName())) {
                                    currentUserSession.checkout(objectNode);
                                    objectNode.getProperty(item.getName()).remove();
                                }
                            }
                        }
                        objectNode.removeMixin(mixin.getName());

                    }
                }
                for (String type : types) {
                    if (!objectNode.isNodeType(type)) {
                        currentUserSession.checkout(objectNode);
                        objectNode.addMixin(type);
                    }
                }
                int mixinCount = objectNode.getMixinNodeTypes().length;
                setProperties(objectNode, newProps);
                objectNode.saveSession();
                if (!aNode.getName().equals(objectNode.getName())) {
                    contentManager.rename(objectNode.getPath(), aNode.getName(), currentUserSession);
                }
                ExtendedNodeType[] mixins = objectNode.getMixinNodeTypes();
                if (mixinCount != mixins.length) {
                    // we got added mixins
                    for (ExtendedNodeType newMixin : mixins) {
                        if (!types.contains(newMixin.getName())) {
                            types.add(newMixin.getName());
                        }
                    }
                }
            } catch (RepositoryException e) {
                logger.error("error", e);
                throw new GWTJahiaServiceException("Could not save file " + objectNode.getName());
            }
        }
    }

    public void setProperties(JCRNodeWrapper objectNode, List<GWTJahiaNodeProperty> newProps) throws RepositoryException {
        if(objectNode == null || newProps == null || newProps.isEmpty()){
            logger.debug("node or properties are null or empty");
            return;
        }
        if (!objectNode.isCheckedOut()) {
            objectNode.checkout();
        }

        for (GWTJahiaNodeProperty prop : newProps) {
            try {
                if (prop != null && !prop.getName().equals("*")) {
                    if (prop.isMultiple()) {
                        List<Value> values = new ArrayList<Value>();
                        for (GWTJahiaNodePropertyValue val : prop.getValues()) {
                            if (val.getString() != null) {
                                values.add(contentDefinition.convertValue(val));
                            }
                        }
                        Value[] finalValues = new Value[values.size()];
                        values.toArray(finalValues);
                        objectNode.setProperty(prop.getName(), finalValues);
                    } else {
                        if (prop.getValues().size() > 0) {
                            GWTJahiaNodePropertyValue propValue = prop.getValues().get(0);
                            if (propValue.getType() == GWTJahiaNodePropertyType.ASYNC_UPLOAD) {
                                GWTFileManagerUploadServlet.Item fileItem = GWTFileManagerUploadServlet.getItem(propValue.getString());
                                boolean clear = propValue.getString().equals("clear");
                                if (!clear && fileItem == null) {
                                    continue;
                                }
                                ExtendedNodeDefinition end = ((ExtendedNodeType) objectNode.getPrimaryNodeType()).getChildNodeDefinitionsAsMap().get(prop.getName());

                                if (end != null) {
                                    try {
                                        if (objectNode.hasNode(prop.getName())) {
                                            objectNode.getNode(prop.getName()).remove();
                                        }

                                        if (!clear) {
                                            String s = end.getRequiredPrimaryTypeNames()[0];
                                            Node content = objectNode.addNode(prop.getName(), s.equals("nt:base") ? "jnt:resource" : s);

                                            content.setProperty(Constants.JCR_MIMETYPE, fileItem.getContentType());
                                            FileInputStream is = fileItem.getStream();
                                            try {
                                                content.setProperty(Constants.JCR_DATA, is);
                                            } finally {
                                                IOUtils.closeQuietly(is);
                                                fileItem.dispose();
                                            }
                                            content.setProperty(Constants.JCR_LASTMODIFIED, new GregorianCalendar());
                                        }
                                    } catch (Throwable e) {
                                        logger.error(e.getMessage(), e);
                                    }
                                }
                            } else if (propValue.getType() == GWTJahiaNodePropertyType.PAGE_LINK) {
                                if (objectNode.hasNode(prop.getName())) {
                                    Node content = objectNode.getNodes(prop.getName()).nextNode();
                                    content.remove();
                                }

                                // case of link sub-node
                                GWTJahiaNode gwtJahiaNode = propValue.getLinkNode();
                                String linkUrl = gwtJahiaNode.get(Constants.URL);
                                String linkTitle = gwtJahiaNode.get(Constants.JCR_TITLE);
                                String alt = gwtJahiaNode.get(Constants.ALT);
                                String linkType = gwtJahiaNode.get("linkType");
                                GWTJahiaNode nodeReference = gwtJahiaNode.get("j:node");

                                // case of external
                                if (linkType.equalsIgnoreCase("external") && linkUrl !=null) {
                                    Node content = objectNode.addNode(prop.getName(), Constants.JAHIANT_EXTERNAL_PAGE_LINK);
                                    content.setProperty(Constants.JCR_TITLE, linkTitle);
                                    content.setProperty(Constants.URL, linkUrl);
                                    content.setProperty(Constants.ALT, alt);
                                    content.setProperty(Constants.JCR_LASTMODIFIED, new GregorianCalendar());
                                }
                                // case of internal link
                                else if (linkType.equalsIgnoreCase("internal") && nodeReference != null) {
                                    Node content = objectNode.addNode(prop.getName(), Constants.JAHIANT_NODE_LINK);
                                    content.setProperty(Constants.JCR_TITLE, linkTitle);
                                    content.setProperty(Constants.NODE, nodeReference.getUUID());
                                    content.setProperty(Constants.JCR_LASTMODIFIED, new GregorianCalendar());
                                }
                            } else {
                                if (propValue != null && propValue.getString() != null) {
                                    Value value = contentDefinition.convertValue(propValue);
                                    objectNode.setProperty(prop.getName(), value);
                                } else {
                                    if (objectNode.hasProperty(prop.getName())) {
                                        objectNode.getProperty(prop.getName()).remove();
                                    }
                                }
                            }
                        } else if (objectNode.hasProperty(prop.getName())) {
                            objectNode.getProperty(prop.getName()).remove();
                        }
                    }

                }
            } catch (PathNotFoundException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Property with the name '"
                            + prop.getName() + "' not found on the node "
                            + objectNode.getPath() + ". Skipping.", e);
                } else {
                    logger.info("Property with the name '" + prop.getName()
                            + "' not found on the node "
                            + objectNode.getPath() + ". Skipping.");
                }
            }
        }
    }

    public List<Value> getCategoryPathValues(String value) {
        if (value == null || value.length() == 0) {
            return Collections.emptyList();
        }
        List<Value> values = new LinkedList<Value>();
        String[] categories = StringUtils.split(value, ",");
        for (String categoryKey : categories) {
            try {
                values.add(new StringValue(Category.getCategoryPath(categoryKey.trim())));
            } catch (JahiaException e) {
                logger.warn("Unable to retrieve category path for category key '" + categoryKey + "'. Cause: " + e.getMessage(), e);
            }
        }
        return values;
    }
}
