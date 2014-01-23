/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.nodetypes;

import com.google.common.collect.Sets;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xerces.impl.dv.dtd.ENTITYDatatypeValidator;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.nodetype.*;
import java.io.*;
import java.util.*;

/**
 * Jahia implementation of the {@link NodeTypeManager}.
 * User: toto
 * Date: 4 janv. 2008
 * Time: 15:08:56
 */
public class NodeTypeRegistry implements NodeTypeManager {
    public static final String SYSTEM = "system";
    private static Logger logger = LoggerFactory.getLogger(NodeTypeRegistry.class);

    private List<ExtendedNodeType> nodeTypesList = new ArrayList<ExtendedNodeType>();
    private Map<Name, ExtendedNodeType> nodetypes = new HashMap<Name, ExtendedNodeType>();

    private BidiMap namespaces = new DualHashBidiMap();

    @SuppressWarnings("unchecked")
    private Map<String,List<Resource>> files = new ListOrderedMap();

    private Map<ExtendedNodeType,Set<ExtendedNodeType>> mixinExtensions = new HashMap<ExtendedNodeType,Set<ExtendedNodeType>>();
    private Map<String,Set<ExtendedItemDefinition>> typedItems = new HashMap<String,Set<ExtendedItemDefinition>>();

    private boolean propertiesLoaded = false;
    private final Properties deploymentProperties = new Properties();

    private static NodeTypeRegistry instance;

    private static NodeTypeRegistry providerNodeTypeRegistry;

    private static boolean hasEncounteredIssuesWithDefinitions = false;

    public static NodeTypeRegistry getInstance() {
        if (instance == null) {
            instance = new NodeTypeRegistry();
            try {
                instance.initPropertiesFile();
                instance.initSystemDefinitions();
            } catch (IOException e) {
                logger.error("Cannot load definition deployment properties");
            }
        }
        return instance;
    }

    public static NodeTypeRegistry getProviderNodeTypeRegistry() {
        if (providerNodeTypeRegistry == null) {
            providerNodeTypeRegistry = new NodeTypeRegistry();
            try {
                providerNodeTypeRegistry.initSystemDefinitions();
                File definitions = new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "definitions");

                if (definitions.exists()) {
                    List<File> files = new ArrayList<File>();
                    List<File> remfiles = new ArrayList<File>(Arrays.asList(definitions.listFiles()));
                    while (!remfiles.isEmpty() && !remfiles.equals(files)) {
                        files = new ArrayList<File>(remfiles);
                        remfiles.clear();
                        for (File file : files) {
                            try {
                                if (file.getName().endsWith(".cnd")) {
                                    deployDefinitionsFileToProviderNodeTypeRegistry(file);
                                }
                            } catch (ParseException e) {
                                remfiles.add(file);
                            }
                        }
                    }
                }
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        return providerNodeTypeRegistry;
    }

    public static void deployDefinitionsFileToProviderNodeTypeRegistry(File file) throws ParseException, IOException {
        final String systemId = StringUtils.substringBefore(file.getName(), ".cnd");
        JahiaCndReader r = new JahiaCndReader(new FileReader(file), file.getName(), systemId, getProviderNodeTypeRegistry());
        r.parse();
    }

    /**
     * Flush all labels for all node types and items
     */
    public void flushLabels() {
        for (ExtendedNodeType nodeType : nodetypes.values()) {
            nodeType.clearLabels();
        }
        for (Set<ExtendedItemDefinition> itemSet : typedItems.values()) {
            for (ExtendedItemDefinition item : itemSet) {
                item.clearLabels();
                item.getDeclaringNodeType().clearLabels();
            }
        }
    }

    public void initSystemDefinitions() throws IOException {
        String cnddir = SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/nodetypes";
        try {
            File f = new File(cnddir);
            SortedSet<File> cndfiles = new TreeSet<File>(Arrays.asList(f.listFiles()));
            for (File file : cndfiles) {
                addDefinitionsFile(file, SYSTEM + "-" + Patterns.DASH.split(file.getName())[1], null);
            }
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void initPropertiesFile() throws IOException  {
        File f = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/definitions.properties");
        if (f.exists()) {
            InputStream stream = new BufferedInputStream(new FileInputStream(f));
            try {
                deploymentProperties.load(stream);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
        propertiesLoaded = true;
    }

    public void saveProperties() throws IOException {
        if (propertiesLoaded) {
            synchronized (deploymentProperties) {
                File f = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/definitions.properties");
                OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
                try {
                    deploymentProperties.store(out, "");
                } finally {
                    IOUtils.closeQuietly(out);
                }
            }
        }
    }

    public Properties getDeploymentProperties() {
        return deploymentProperties;
    }


    public boolean isLatestDefinitions(String systemId, ModuleVersion version) {
        if (version != null) {
            String key = systemId + ".version";
            if (deploymentProperties.containsKey(key)) {
                ModuleVersion lastDeployed = new ModuleVersion(deploymentProperties.getProperty(key));
                if (lastDeployed.compareTo(version) > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public void addDefinitionsFile(Resource resource, String systemId, ModuleVersion version) throws IOException, ParseException {
        if (version != null) {
            if (isLatestDefinitions(systemId, version)) {
                deploymentProperties.put(systemId + ".version", version.toString());
                saveProperties();
            } else {
                return;
            }
        }

        String ext = resource.getURL().getPath().substring(resource.getURL().getPath().lastIndexOf('.'));
        if (ext.equalsIgnoreCase(".cnd")) {
            Reader resourceReader = null;
            try {
                resourceReader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8);
                JahiaCndReader r = new JahiaCndReader(resourceReader, resource.toString(), systemId, this);
                r.parse();
                if (r.hasEncounteredIssuesWithDefinitions()) {
                    hasEncounteredIssuesWithDefinitions = true;
                }
            } finally {
                IOUtils.closeQuietly(resourceReader);
            }
        } else if (ext.equalsIgnoreCase(".grp")) {
            Reader resourceReader = null;
            try {
                resourceReader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8);
                JahiaGroupingFileReader r = new JahiaGroupingFileReader(resourceReader, resource.toString(),systemId, this);
                r.parse();
            } finally {
                IOUtils.closeQuietly(resourceReader);
            }
        }

        if (!files.containsKey(systemId)) {
            files.put(systemId, new ArrayList<Resource>());
        }
        if (!files.get(systemId).contains(resource)) {
            files.get(systemId).add(resource);
        }
    }

    public void addDefinitionsFile(File file, String systemId, ModuleVersion version) throws ParseException, IOException {
        addDefinitionsFile(file == null ? null : new FileSystemResource(file), systemId, version);
    }

    /**
     * Reads the specified CND file resource and parses it to obtain a list of node type definitions.
     * 
     * @param resource
     *            a resource, representing a CND file
     * @param systemId
     *            the ID to use to specify the "origin" on the node types from this file
     * @return a list of the node types parsed from the specified resource
     * @throws ParseException
     *             in case of a parsing error
     * @throws IOException
     *             in case of an I/O error when reading the specified resource
     */
    public List<ExtendedNodeType> getDefinitionsFromFile(Resource resource, String systemId) throws ParseException, IOException {
        String ext = resource.getURL().getPath().substring(resource.getURL().getPath().lastIndexOf('.'));
        if (ext.equalsIgnoreCase(".cnd")) {
            Reader resourceReader = null;
            try {
                resourceReader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8);
                JahiaCndReader r = new JahiaCndReader(resourceReader, resource.getURL().getPath(), systemId, this);
                r.setDoRegister(false);
                r.parse();
                return r.getNodeTypesList();
            } finally {
                IOUtils.closeQuietly(resourceReader);
            }
        }
        return Collections.emptyList();
    }

    public void validateDefinitionsFile(InputStream inputStream, String filename, String systemId) throws ParseException, IOException, RepositoryException {
        if (filename.toLowerCase().endsWith(".cnd")) {
            Reader resourceReader = null;
            try {
                resourceReader = new InputStreamReader(inputStream, Charsets.UTF_8);
                JahiaCndReader r = new JahiaCndReader(resourceReader, filename, systemId, this);
                r.parse();

                if (r.hasEncounteredIssuesWithDefinitions()) {
                    throw new RepositoryException(StringUtils.join(r.getParsingErrors(), "\n"));
                }

                for (ExtendedNodeType nodeType : r.getNodeTypesList()) {
                    if (NodeTypeRegistry.getInstance().hasNodeType(nodeType.getName())) {
                        ExtendedNodeType existingNodeType = NodeTypeRegistry.getInstance().getNodeType(nodeType.getName());
                        if (!existingNodeType.getSystemId().equals(nodeType.getSystemId())) {
                            throw new NodeTypeExistsException("Node type already defined : "+nodeType.getName());
                        }
                    }
                }
            } finally {
                IOUtils.closeQuietly(resourceReader);
            }
        }
    }

    public List<String> getSystemIds() {
        return new ArrayList<String>(files.keySet());
    }

    public List<Resource> getFiles(String systemId) {
        return files.get(systemId);
    }

    public ExtendedNodeType getNodeType(String name) throws NoSuchNodeTypeException {
        ExtendedNodeType res = StringUtils.isNotEmpty(name) ? nodetypes.get(new Name(name, namespaces)) : null;
        if (res == null) {
            throw new NoSuchNodeTypeException(name);
        }
        return res;
    }

    public JahiaNodeTypeIterator getAllNodeTypes() {
        return new JahiaNodeTypeIterator(nodeTypesList.iterator(),nodeTypesList.size());
    }

    public JahiaNodeTypeIterator getAllNodeTypes(List<String> systemIds) {
        List<ExtendedNodeType> res = new ArrayList<ExtendedNodeType>();

        for (ExtendedNodeType nt : nodetypes.values()) {
            if (systemIds == null || systemIds.contains(nt.getSystemId())) {
                res.add(nt);
            }
        }
        return new JahiaNodeTypeIterator(res.iterator(), res.size());
    }

    public JahiaNodeTypeIterator getNodeTypes(String systemId) {
        List<ExtendedNodeType> l = new ArrayList<ExtendedNodeType>();
        for (ExtendedNodeType nt : nodeTypesList) {
            if (nt.getSystemId().equals(systemId)) {
                l.add(nt);
            }
        }
        return new JahiaNodeTypeIterator(l.iterator(),l.size());
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getNamespaces() {
        return namespaces;
    }
    public NodeTypeIterator getPrimaryNodeTypes() throws RepositoryException {
        return getPrimaryNodeTypes(null);
    }

    public NodeTypeIterator getPrimaryNodeTypes(List<String> systemIds) throws RepositoryException {
        List<ExtendedNodeType> res = new ArrayList<ExtendedNodeType>();
        for (Iterator<ExtendedNodeType> iterator = nodetypes.values().iterator(); iterator.hasNext();) {
            ExtendedNodeType nt = iterator.next();
            if (!nt.isMixin() && (systemIds == null || systemIds.contains(nt.getSystemId()))) {
                res.add(nt);
            }
        }
        return new JahiaNodeTypeIterator(res.iterator(), res.size());
    }

    public NodeTypeIterator getMixinNodeTypes() throws RepositoryException {
        return getMixinNodeTypes(null);
    }

    public NodeTypeIterator getMixinNodeTypes(List<String> systemIds) throws RepositoryException {
        List<ExtendedNodeType> res = new ArrayList<ExtendedNodeType>();
        for (Iterator<ExtendedNodeType> iterator = nodetypes.values().iterator(); iterator.hasNext();) {
            ExtendedNodeType nt = iterator.next();
            if (nt.isMixin() && (systemIds == null || systemIds.contains(nt.getSystemId()))) {
                res.add(nt);
            }
        }
        return new JahiaNodeTypeIterator(res.iterator(), res.size());
    }

    public void addNodeType(Name name, ExtendedNodeType nodeType) throws NodeTypeExistsException {
        if (nodetypes.containsKey(name)) {
            if (!nodetypes.get(name).getSystemId().equals(nodeType.getSystemId())) {
                throw new NodeTypeExistsException("Node type already defined : "+nodeType.getName());
            } else {
                nodeTypesList.remove(nodetypes.get(name));
            }
        }
        nodeTypesList.add(nodeType);
        nodetypes.put(name, nodeType);
    }

    public void addMixinExtension(ExtendedNodeType mixin, ExtendedNodeType baseType) {
        if (!mixinExtensions.containsKey(baseType)) {
            mixinExtensions.put(baseType, new HashSet<ExtendedNodeType>());
        }
        mixinExtensions.get(baseType).add(mixin);
    }

    public Map<ExtendedNodeType, Set<ExtendedNodeType>> getMixinExtensions() {
        return mixinExtensions;
    }

    public void addTypedItem(ExtendedItemDefinition itemDef) {
        final String type = itemDef.getItemType();
        if (!typedItems.containsKey(type)) {
            typedItems.put(type, new HashSet<ExtendedItemDefinition>());
        }
        typedItems.get(type).add(itemDef);
    }

    public Map<String, Set<ExtendedItemDefinition>> getTypedItems() {
        return typedItems;
    }

    public void unregisterNodeType(Name name) {
        ExtendedNodeType nt = nodetypes.remove(name);
        nodeTypesList.remove(nt);
    }

    public void unregisterNodeTypes(String systemId) {
        for (Name n : new HashSet<Name>(nodetypes.keySet())) {
            ExtendedNodeType nt = nodetypes.get(n);
            if (systemId.equals(nt.getSystemId())) {
                unregisterNodeType(n);
            }
        }
    }

    public class JahiaNodeTypeIterator implements NodeTypeIterator, Iterable<ExtendedNodeType>  {
        private long size;
        private long pos=0;
        private Iterator<ExtendedNodeType> iterator;

        JahiaNodeTypeIterator(Iterator<ExtendedNodeType> it, long size) {
            this.iterator = it;
            this.size = size;
        }

        public NodeType nextNodeType() {
            pos += 1;
            return iterator.next();
        }

        public void skip(long l) {
            if ((pos + l + 1) > size) {
                throw new NoSuchElementException("Tried to skip past " + l +
                        " elements, which with current pos (" + pos +
                        ") brings us past total size=" + size);
            }
            for (int i=0; i < l; i++) {
                next();
            }
        }

        public long getSize() {
            return size;
        }

        public long getPosition() {
            return pos;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Object next() {
            pos += 1;
            return iterator.next();
        }

        public void remove() {
            iterator.remove();
            size -= 1;
        }

        /**
         * Returns an iterator over a set of elements of type T.
         *
         * @return an Iterator.
         */
        @Override
        public Iterator<ExtendedNodeType> iterator() {
            return this;
        }
    }

    public boolean hasNodeType(String name) {
        return nodetypes.get(new Name(name, namespaces)) != null;
    }

    public NodeTypeTemplate createNodeTypeTemplate() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeTypeTemplate createNodeTypeTemplate(NodeTypeDefinition ntd) throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeDefinitionTemplate createNodeDefinitionTemplate() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public PropertyDefinitionTemplate createPropertyDefinitionTemplate() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeType registerNodeType(NodeTypeDefinition ntd, boolean allowUpdate) throws InvalidNodeTypeDefinitionException, NodeTypeExistsException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeTypeIterator registerNodeTypes(NodeTypeDefinition[] ntds, boolean allowUpdate) throws InvalidNodeTypeDefinitionException, NodeTypeExistsException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void unregisterNodeType(String name) throws ConstraintViolationException {
        Name n = new Name(name, namespaces);
        if (nodetypes.containsKey(n)) {
            for (ExtendedNodeType type : nodeTypesList) {
                if (!type.getName().equals(name)) {
                    for (ExtendedNodeType nt : type.getSupertypes()) {
                        if (nt.getName().equals(name)) {
                            throw new ConstraintViolationException("Cannot unregister node type " + name + " because " + type.getName() + " extends it.");
                        }
                    }
                    for (ExtendedNodeDefinition ntd : type.getChildNodeDefinitions()) {
                        if (Sets.newHashSet(ntd.getRequiredPrimaryTypeNames()).contains(name)) {
                            throw new ConstraintViolationException("Cannot unregister node type " + name + " because a child node definition of " + type.getName() + " requires it.");
                        }
                    }
                    for (ExtendedNodeDefinition ntd : type.getUnstructuredChildNodeDefinitions().values()) {
                        if (Sets.newHashSet(ntd.getRequiredPrimaryTypeNames()).contains(name)) {
                            throw new ConstraintViolationException("Cannot unregister node type " + name + " because a child node definition of " + type.getName() + " requires it.");
                        }
                    }
                }
            }
            nodeTypesList.remove(nodetypes.remove(n));
        }
    }

    public void unregisterNodeTypes(String[] names) throws ConstraintViolationException {
        for (String name : names) {
            unregisterNodeType(name);
        }
    }

    /**
     * Indicates if any issue related to the definitions has been encountered since the last startup. When this method
     * returns true, the only way to get back false as a return value is to restart Jahia.
     *
     * @return true if an issue with the def has been encountered, false otherwise.
     * @since 6.6.2.0
     */
    public final boolean hasEncounteredIssuesWithDefinitions() {
        return hasEncounteredIssuesWithDefinitions;
    }
}
