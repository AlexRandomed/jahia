/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator used to filter translation and mount nodes
 * wrapping nodes on demand with a lazy size
 */
public class ChildNodesIterator implements JCRNodeIteratorWrapper {

    private static final Logger logger = LoggerFactory.getLogger(ChildNodesIterator.class);

    private NodeIterator ni;
    private List<String> mountNames;
    private List<JCRNodeWrapper> mountNodes;
    private JCRNodeWrapperImpl parent;
    private JCRSessionWrapper session;
    private JCRStoreProvider provider;
    private JCRNodeWrapper nextNode;
    private long position;
    private List<JCRNodeWrapper> cache = new ArrayList<>();

    public ChildNodesIterator(NodeIterator ni, List<String> mountNames, List<JCRNodeWrapper> mountNodes,
                              JCRNodeWrapperImpl parent, JCRSessionWrapper session, JCRStoreProvider provider) {
        this.ni = ni;
        this.mountNames = mountNames;
        this.mountNodes = mountNodes;
        this.parent = parent;
        this.session = session;
        this.provider = provider;
        this.position = 0;
        prefetchNext();
    }

    private JCRNodeWrapper loadNextInCache() {
        do {
            try {
                if (mountNodes != null && !mountNodes.isEmpty()) {
                    JCRNodeWrapper wrapper = mountNodes.remove(0);
                    cache.add(wrapper);
                    return wrapper;
                }
                Node n = null;
                while (n == null && ni.hasNext()) {
                    n = ni.nextNode();
                    if ((session.getLocale() != null && n.getName().startsWith("j:translation_")) ||
                            (mountNames != null && mountNames.contains(n.getName()))) {
                        n = null;
                    }
                }
                if (n != null) {
                    try {
                        JCRNodeWrapper wrapper = provider.getNodeWrapper(n, parent.buildSubnodePath(n.getName()), parent, session);
                        cache.add(wrapper);
                        return wrapper;
                    } catch (PathNotFoundException e) {
                        // continue
                    }
                } else {
                    return null;
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                // continue
            }
        } while (true);
    }

    private void prefetchNext() {
        while (position >= cache.size() && loadNextInCache() != null) {}
        if (position < cache.size()) {
            nextNode = cache.get((int) position);
            position++;
        } else {
            nextNode = null;
        }
    }

    @Override
    public void skip(long skipNum) {
        for (int i = 0; i< skipNum; i++) {
            if (nextNode == null) {
                throw new NoSuchElementException();
            }
            prefetchNext();
        }
    }

    @Override
    public long getSize() {
        while (loadNextInCache() != null) {}
        return cache.size();
    }

    @Override
    public long getPosition() {
        return hasNext() ? (position - 1) : position;
    }

    @Override
    public Node nextNode() {
        return (Node) next();
    }

    @Override
    public boolean hasNext() {
        return (nextNode != null);
    }

    @Override
    public Object next() {
        return wrappedNext();
    }

    private JCRNodeWrapper wrappedNext() {
        final JCRNodeWrapper res = nextNode;
        if (res == null) {
            throw new NoSuchElementException();
        }
        prefetchNext();
        return res;
    }

    @Override
    public void remove() {
        if (mountNodes != null && !mountNodes.isEmpty()) {
            mountNodes.remove(0);
        } else {
            ni.remove();
        }
    }

    @Override
    public Iterator<JCRNodeWrapper> iterator() {
        return new Iterator<JCRNodeWrapper>() {
            @Override
            public boolean hasNext() {
                return ChildNodesIterator.this.hasNext();
            }

            @Override
            public JCRNodeWrapper next() {
                return ChildNodesIterator.this.wrappedNext();
            }

            @Override
            public void remove() {
                ChildNodesIterator.this.remove();
            }
        };
    }
}
