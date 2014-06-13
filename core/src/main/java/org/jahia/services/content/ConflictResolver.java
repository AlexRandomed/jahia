/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
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
 */
package org.jahia.services.content;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.slf4j.Logger;

import javax.jcr.*;
import java.util.*;

/**
 * User: toto
 * Date: Feb 1, 2010
 * Time: 5:58:37 PM
 */
public class ConflictResolver {


    private static List<String> ignore = Arrays.asList(Constants.JCR_UUID, Constants.JCR_PRIMARYTYPE,
            Constants.JCR_CREATED, Constants.JCR_CREATEDBY, Constants.JCR_BASEVERSION,
            Constants.JCR_ISCHECKEDOUT, Constants.JCR_VERSIONHISTORY, Constants.JCR_PREDECESSORS,
            Constants.JCR_ACTIVITY, Constants.CHECKIN_DATE, "j:locktoken", "j:lockTypes", "jcr:lockOwner",
            "jcr:lockIsDeep", "j:deletedChildren", "j:processId");
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ConflictResolver.class);

    private JCRNodeWrapper sourceNode;
    private JCRNodeWrapper targetNode;

    private Set<JCRNodeWrapper> toCheckpoint;

    private List<Diff> differences;
    private List<Diff> unresolvedDifferences;

    public ConflictResolver(JCRNodeWrapper sourceNode, JCRNodeWrapper targetNode) throws RepositoryException {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
    }

    public void setToCheckpoint(Set<JCRNodeWrapper> toCheckpoint) {
        this.toCheckpoint = toCheckpoint;
    }

    public List<Diff> getUnresolvedDifferences() {
        return unresolvedDifferences;
    }

    public void applyDifferences() throws RepositoryException {
        computeDifferences();

        unresolvedDifferences = new ArrayList<Diff>();
        for (Diff diff : differences) {
            if (!diff.apply()) {
                unresolvedDifferences.add(diff);
            }
        }
        targetNode.getSession().save();
    }

    private void computeDifferences() throws RepositoryException {
        differences = compare(sourceNode, targetNode, "");
    }

    private List<Diff> compare(JCRNodeWrapper sourceNode, JCRNodeWrapper targetNode, String basePath) throws RepositoryException {
        List<Diff> diffs = new ArrayList<Diff>();

        final ListOrderedMap targetUuids = getChildEntries(targetNode, targetNode.getSession());
        final ListOrderedMap sourceUuids = getChildEntries(sourceNode, sourceNode.getSession());

        if (!targetUuids.values().equals(sourceUuids.values())) {
            for (Iterator iterator = sourceUuids.keySet().iterator(); iterator.hasNext(); ) {
                String key = (String) iterator.next();
                if (targetUuids.containsKey(key) && !targetUuids.get(key).equals(sourceUuids.get(key))) {
                    diffs.add(new ChildRenamedDiff(key, addPath(basePath, (String) targetUuids.get(key)), addPath(basePath, (String) sourceUuids.get(key))));
                }
            }
        }

        // Child nodes
        if (!targetUuids.keyList().equals(sourceUuids.keyList())) {
            List<String> added = new ArrayList<String>(sourceUuids.keySet());
            added.removeAll(targetUuids.keySet());
            List<String> removed = new ArrayList<String>(targetUuids.keySet());
            removed.removeAll(sourceUuids.keySet());

            // Ordering
            if (targetNode.getPrimaryNodeType().hasOrderableChildNodes()) {
                Map<String, String> newOrdering = getOrdering(sourceUuids, Collections.<String>emptyList());
                List<String> oldUuidsList = new ArrayList<String>(targetUuids.keySet());
                oldUuidsList.removeAll(removed);
                List<String> newUuidsList = new ArrayList<String>(sourceUuids.keySet());
                newUuidsList.removeAll(added);
                if (!oldUuidsList.equals(newUuidsList)) {
                    for (int i=1; i < oldUuidsList.size(); i++) {
                        String x = oldUuidsList.get(i);
                        int j = i;
                        while ( j > 0 && sourceUuids.indexOf(oldUuidsList.get(j-1)) > sourceUuids.indexOf(x)) {
                            oldUuidsList.set(j, oldUuidsList.get(j-1));
                            j--;
                        }
                        if (j != i) {
                            String orderBeforeUuid = (j+1 == oldUuidsList.size()) ? null : oldUuidsList.get(j + 1);

                            diffs.add(new ChildNodeReorderedDiff(x, orderBeforeUuid,
                                    addPath(basePath, (String) sourceUuids.get(x)), (String) sourceUuids.get(orderBeforeUuid), newOrdering));
                            logger.debug("reorder " + sourceUuids.get(x) + " before " + sourceUuids.get(orderBeforeUuid));

                            oldUuidsList.set(j,x);
                        }
                    }
                }
            }

            // Removed nodes
            for (String s : removed) {
                try {
                    this.sourceNode.getSession().getNodeByUUID(s);
                    // Item has been moved
                } catch (ItemNotFoundException e) {
                    diffs.add(new ChildRemovedDiff(s, addPath(basePath, (String) targetUuids.get(s)), s));
                }
            }

            // Added nodes
            for (String uuid : added) {
                diffs.add(new ChildAddedDiff(uuid, addPath(basePath,
                        (String) sourceUuids.get(uuid)), uuid.equals(sourceUuids
                        .lastKey()) ? null : (String) sourceUuids.get(sourceUuids
                        .get(sourceUuids.indexOf(uuid) + 1))));
            }
        }

        PropertyIterator pi1 = targetNode.getProperties();
        while (pi1.hasNext()) {
            JCRPropertyWrapper prop1 = (JCRPropertyWrapper) pi1.next();

            String propName = prop1.getName();
            if (ignore.contains(propName)) {
                continue;
            }
            if (!sourceNode.hasProperty(propName)) {
                if (prop1.isMultiple()) {
                    Value[] values = prop1.getRealValues();
                    for (Value value : values) {
                        diffs.add(new PropertyRemovedDiff(addPath(basePath, propName), value));
                    }
                } else {
                    diffs.add(new PropertyChangedDiff(
                            addPath(basePath, propName), null));
                }
            } else {
                JCRPropertyWrapper prop2 = sourceNode.getProperty(propName);

                if (prop1.isMultiple() != prop2.isMultiple()) {
                    throw new RepositoryException();
                } else {
                    if (prop1.isMultiple()) {
                        List<? extends Value> vs1 = Arrays.asList(prop1.getRealValues());
                        List<? extends Value> vs2 = Arrays.asList(prop2.getRealValues());

                        Map<String, Value> added = new HashMap<String, Value>();
                        for (Value value : vs2) {
                            added.put(value.getString(), value);
                        }
                        for (Value value : vs1) {
                            added.remove(value.getString());
                        }
                        for (Value value : added.values()) {
                            diffs.add(new PropertyAddedDiff(
                                    addPath(basePath, propName), value));
                        }

                        Map<String, Value> removed = new HashMap<String, Value>();
                        for (Value value : vs1) {
                            removed.put(value.getString(), value);
                        }
                        for (Value value : vs2) {
                            removed.remove(value.getString());
                        }
                        for (Value value : removed.values()) {
                            diffs.add(new PropertyRemovedDiff(
                                    addPath(basePath, propName), value));
                        }
                    } else {
                        if (!equalsValue(prop1.getRealValue(), prop2.getRealValue())) {
                            diffs.add(new PropertyChangedDiff(
                                    addPath(basePath, propName), prop2.getRealValue()));
                        }
                    }
                }
            }
        }
        PropertyIterator pi2 = sourceNode.getProperties();

        while (pi2.hasNext()) {
            JCRPropertyWrapper prop2 = (JCRPropertyWrapper) pi2.next();

            String propName = prop2.getName();

            if (ignore.contains(propName)) {
                continue;
            }
            if (!targetNode.hasProperty(propName)) {
                if (prop2.isMultiple()) {
                    Value[] values = prop2.getRealValues();
                    for (Value value : values) {
                        diffs.add(new PropertyAddedDiff(addPath(basePath, prop2.getName()), value));
                    }
                } else {
                    diffs.add(new PropertyChangedDiff(addPath(basePath, prop2.getName()), prop2.getRealValue()));
                }
            }

        }

        for (Diff diff : new ArrayList<Diff>(diffs)) {
            if (diff instanceof PropertyAddedDiff && ((PropertyAddedDiff) diff).propertyPath.endsWith(Constants.JCR_MIXINTYPES)) {
                diffs.remove(diff);
                diffs.add(0, diff);
            } else if (diff instanceof PropertyRemovedDiff && ((PropertyRemovedDiff) diff).propertyPath.endsWith(Constants.JCR_MIXINTYPES)) {
                diffs.remove(diff);
                diffs.add(diff);
            }
        }

        NodeIterator ni = targetNode.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper sub = (JCRNodeWrapper) ni.next();
            if (sourceNode.hasNode(sub.getName()) && !sub.isVersioned() && !sourceNode.getNode(sub.getName()).isVersioned()) {
                diffs.addAll(compare(sourceNode.getNode(sub.getName()), sub, addPath(basePath, sub.getName())));
            }
        }

        return diffs;
    }

    private Map<String, String> getOrdering(ListOrderedMap uuids1, List<String> removed) {
        Map<String, String> previousMap = new LinkedHashMap<String, String>();
        ListIterator it = uuids1.keyList().listIterator(uuids1.size());
        String previous = "";
        while (it.hasPrevious()) {
            String uuid = (String) it.previous();
            if (!removed.contains(uuid)) {
                previousMap.put(uuid, previous);
                previous = uuid;
            }
        }
        return previousMap;
    }

    private ListOrderedMap getChildEntries(JCRNodeWrapper node, JCRSessionWrapper session) throws RepositoryException {
        NodeIterator ni1 = node.getNodes();
        ListOrderedMap childEntries = new ListOrderedMap();
        while (ni1.hasNext()) {
            Node child = (Node) ni1.next();
            try {
                if (!child.isNodeType("jmix:nolive") && !(session.getWorkspace().getName().equals("live") && child.hasProperty("j:originWS") && child.getProperty("j:originWS").getString().equals("live"))) {
                    session.getNodeByUUID(child.getIdentifier());
                    childEntries.put(child.getIdentifier(), child.getName());
                }
            } catch (ItemNotFoundException e) {
                // item does not exist in this workspace
            }
        }
        return childEntries;
    }

    public boolean equalsValue(Value o1, Value o2) {
        try {
            if (o1.getType() != o2.getType()) {
                return false;
            }
            if (o1.getType() == PropertyType.BINARY) {
                return o1.equals(o2);
            } else {
                return o1.getString().equals(o2.getString());
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String addPath(String basePath, String name) {
        return basePath.equals("") ? name : basePath + "/" + name;
    }

    private JCRNodeWrapper getParentTarget(JCRNodeWrapper target, String path) throws RepositoryException {
        if (path.contains("/")) {
            return target.getNode(StringUtils.substringBeforeLast(path, "/"));
        } else {
            return target;
        }
    }

    private String getTargetName(String path) {
        if (path.contains("/")) {
            return StringUtils.substringAfterLast(path, "/");
        } else {
            return path;
        }

    }

    interface Diff {
        boolean apply() throws RepositoryException;
    }

    class ChildRenamedDiff implements Diff {
        private String uuid;
        private String oldName;
        private String newName;

        ChildRenamedDiff(String uuid, String oldName, String newName) {
            this.uuid = uuid;
            this.oldName = oldName;
            this.newName = newName;
        }

        public boolean apply() throws RepositoryException {
            return !(targetNode.hasNode(oldName) && !targetNode.getNode(oldName).isVersioned()) ||
                    targetNode.getNode(oldName).rename(newName);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChildRenamedDiff that = (ChildRenamedDiff) o;

            if (!newName.equals(that.newName)) return false;
            if (!oldName.equals(that.oldName)) return false;
            if (!uuid.equals(that.uuid)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = uuid.hashCode();
            result = 31 * result + oldName.hashCode();
            result = 31 * result + newName.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "ChildRenamedDiff{" +
                    "uuid='" + uuid + '\'' +
                    ", oldName='" + oldName + '\'' +
                    ", newName='" + newName + '\'' +
                    '}';
        }
    }

    class ChildAddedDiff implements Diff {
        private String uuid;
        private String newName;
        private String nextSibling;

        ChildAddedDiff(String uuid, String newName, String nextSibling) {
            this.uuid = uuid;
            this.newName = newName;
            this.nextSibling = nextSibling;
        }

        public boolean apply() throws RepositoryException {
            if (sourceNode.getNode(newName).isVersioned() || targetNode.hasNode(newName)) {
                if (targetNode.hasNode(newName) && (nextSibling == null || targetNode.hasNode(nextSibling)) && targetNode.getPrimaryNodeType().hasOrderableChildNodes()) {
                    if (!newName.contains("/") && (nextSibling == null || !nextSibling.contains("/"))) {
                        // todo reorder non-versionable sub nodes
                        targetNode.orderBefore(newName, nextSibling);
                    }
                    targetNode.getSession().save();
                }
                return true;
            }

            JCRNodeWrapper targetNode = getParentTarget(ConflictResolver.this.targetNode, newName);
            JCRNodeWrapper sourceNode = getParentTarget(ConflictResolver.this.sourceNode, newName);
            String newNameParsed = getTargetName(newName);

            targetNode.getSession().save();
            JCRPublicationService.getInstance().doClone(sourceNode.getNode(newNameParsed), sourceNode.getSession(), targetNode.getSession(), toCheckpoint);
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChildAddedDiff)) return false;

            ChildAddedDiff that = (ChildAddedDiff) o;

            if (newName != null ? !newName.equals(that.newName) : that.newName != null) return false;
            if (nextSibling != null ? !nextSibling.equals(that.nextSibling) : that.nextSibling != null) return false;
            if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = uuid != null ? uuid.hashCode() : 0;
            result = 31 * result + (newName != null ? newName.hashCode() : 0);
            result = 31 * result + (nextSibling != null ? nextSibling.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ChildAddedDiff{" +
                    "uuid='" + uuid + '\'' +
                    ", newName='" + newName + '\'' +
                    ", nextSibling='" + nextSibling + '\'' +
                    '}';
        }
    }

    class ChildRemovedDiff implements Diff {
        private String uuid;
        private String oldName;
        private String identifier;

        ChildRemovedDiff(String uuid, String oldName, String identifier) {
            this.uuid = uuid;
            this.oldName = oldName;
            this.identifier = identifier;
        }

        public boolean apply() throws RepositoryException {
            if (targetNode.hasNode(oldName)) {
                final JCRNodeWrapper node = targetNode.getNode(oldName);
                if (node.getIdentifier().equals(identifier)) {
                    JCRPublicationService.getInstance().addRemovedLabel(node, node.getSession().getWorkspace().getName() + "_removed_at_" + JCRVersionService.DATE_FORMAT.print(System.currentTimeMillis()));
                    node.remove();
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChildRemovedDiff that = (ChildRemovedDiff) o;

            if (!oldName.equals(that.oldName)) return false;
            if (!uuid.equals(that.uuid)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = uuid.hashCode();
            result = 31 * result + oldName.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "ChildRemovedDiff{" +
                    "uuid='" + uuid + '\'' +
                    ", oldName='" + oldName + '\'' +
                    '}';
        }
    }

    class ChildNodeReorderedDiff implements Diff {
        private String name;
        private String orderBeforeName;
        private String uuid;
        private String orderBeforeUuid;
        private Map<String, String> ordering;

        ChildNodeReorderedDiff(String uuid, String orderBeforeUuid, String name, String orderBeforeName, Map<String, String> ordering) {
            this.name = name;
            this.orderBeforeName = orderBeforeName;
            this.uuid = uuid;
            this.orderBeforeUuid = orderBeforeUuid;
            this.ordering = ordering;
        }

        public boolean apply() throws RepositoryException {
            if (targetNode.hasNode(name)) {
                JCRNodeWrapper realTargetNode = targetNode;
                String realName = name;
                if (name.contains("/")) {
                    realTargetNode = targetNode.getNode(StringUtils
                            .substringBeforeLast(name, "/"));
                    realName = StringUtils
                            .substringAfterLast(name, "/");
                }
                if (realTargetNode.getPrimaryNodeType()
                        .hasOrderableChildNodes()) {

                    while (orderBeforeName != null
                            && !realTargetNode.hasNode(orderBeforeName)) {
                        orderBeforeUuid = ordering.get(orderBeforeUuid);
                        try {
                            if (orderBeforeUuid.equals("")) {
                                orderBeforeName = null;
                            } else {
                                orderBeforeName = realTargetNode.getSession()
                                        .getNodeByUUID(orderBeforeUuid)
                                        .getName();
                            }
                        } catch (ItemNotFoundException e) {
                            // ignored exception
                        }
                    }
                    realTargetNode.orderBefore(realName, orderBeforeName);
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChildNodeReorderedDiff that = (ChildNodeReorderedDiff) o;

            if (orderBeforeUuid != null ? !orderBeforeUuid.equals(that.orderBeforeUuid) : that.orderBeforeUuid != null)
                return false;
            if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = uuid != null ? uuid.hashCode() : 0;
            result = 31 * result + (orderBeforeUuid != null ? orderBeforeUuid.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ChildNodeReorderedDiff{" +
                    "name='" + name + '\'' +
                    ", orderBeforeName='" + orderBeforeName + '\'' +
                    ", uuid='" + uuid + '\'' +
                    ", orderBeforeUuid='" + orderBeforeUuid + '\'' +
                    ", ordering=" + ordering +
                    '}';
        }
    }

    class PropertyAddedDiff implements Diff {
        private String propertyPath;
        private Value newValue;

        PropertyAddedDiff(String propertyPath, Value newValue) {
            this.propertyPath = propertyPath;
            this.newValue = newValue;
        }

        public boolean apply() throws RepositoryException {
            JCRNodeWrapper targetNode = getParentTarget(ConflictResolver.this.targetNode, propertyPath);
            String propertyName = getTargetName(propertyPath);

            if (!targetNode.isCheckedOut()) {
                targetNode.checkout();
            }
            if (propertyName.equals(Constants.JCR_MIXINTYPES)) {
                targetNode.addMixin(newValue.getString());
            } else if (targetNode.hasProperty(propertyName)) {
                List<Value> values = new ArrayList<Value>(Arrays.asList(targetNode.getProperty(propertyName).getRealValues()));
                values.add(newValue);
                targetNode.setProperty(propertyName, values.toArray(new Value[values.size()]));
            } else {
                targetNode.setProperty(propertyName, new Value[]{newValue});
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PropertyAddedDiff that = (PropertyAddedDiff) o;

            if (!equalsValue(newValue, that.newValue)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = propertyPath != null ? propertyPath.hashCode() : 0;
            result = 31 * result + (newValue != null ? newValue.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "PropertyAddedDiff{" +
                    "propertyPath='" + propertyPath + '\'' +
                    ", newValue=" + newValue +
                    '}';
        }
    }

    class PropertyRemovedDiff implements Diff {
        private String propertyPath;
        private Value oldValue;

        PropertyRemovedDiff(String propertyPath, Value oldValue) {
            this.propertyPath = propertyPath;
            this.oldValue = oldValue;
        }

        public boolean apply() throws RepositoryException {
            JCRNodeWrapper targetNode = getParentTarget(ConflictResolver.this.targetNode, propertyPath);
            String propertyName = getTargetName(propertyPath);

            if (!targetNode.isCheckedOut()) {
                targetNode.checkout();
            }

            if (propertyName.equals(Constants.JCR_MIXINTYPES)) {
                targetNode.removeMixin(oldValue.getString());
            } else {
                List<? extends Value> oldValues = Arrays.asList(targetNode.getProperty(propertyName).getRealValues());
                List<Value> newValues = new ArrayList<Value>();
                for (Value value : oldValues) {
                    if (!equalsValue(value, oldValue)) {
                        newValues.add(value);
                    }
                }
                if (newValues.isEmpty()) {
                    targetNode.getProperty(propertyName).remove();
                } else {
                    targetNode.setProperty(propertyName, newValues.toArray(new Value[newValues.size()]));
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PropertyRemovedDiff that = (PropertyRemovedDiff) o;

            if (!equalsValue(oldValue, that.oldValue)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = propertyPath != null ? propertyPath.hashCode() : 0;
            result = 31 * result + (oldValue != null ? oldValue.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "PropertyRemovedDiff{" +
                    "propertyPath='" + propertyPath + '\'' +
                    ", oldValue=" + oldValue +
                    '}';
        }
    }

    class PropertyChangedDiff implements Diff {
        private String propertyPath;
        private Value newValue;

        PropertyChangedDiff(String propertyPath, Value newValue) {
            this.propertyPath = propertyPath;
            this.newValue = newValue;
        }

        public boolean apply() throws RepositoryException {
            JCRNodeWrapper targetNode = getParentTarget(ConflictResolver.this.targetNode, propertyPath);
            String propertyName = getTargetName(propertyPath);

            if (!targetNode.isCheckedOut()) {
                targetNode.checkout();
            }

            if (newValue == null) {
                if (targetNode.hasProperty(propertyName)) {
                    targetNode.getProperty(propertyName).remove();
                }
            } else {
                targetNode.getRealNode().setProperty(propertyName, newValue);
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PropertyChangedDiff that = (PropertyChangedDiff) o;

            if (newValue != null ? !newValue.equals(that.newValue) : that.newValue != null) return false;
            if (propertyPath != null ? !propertyPath.equals(that.propertyPath) : that.propertyPath != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = propertyPath != null ? propertyPath.hashCode() : 0;
            result = 31 * result + (newValue != null ? newValue.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "PropertyChangedDiff{" +
                    "propertyPath='" + propertyPath + '\'' +
                    ", newValue=" + newValue +
                    '}';
        }
    }


}
