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

package org.jahia.services.content;

import org.jahia.api.Constants;

import javax.jcr.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Invocation handler to decorate the {@link javax.jcr.NodeIterator} instance of the query result in order to wrap each {@link javax.jcr.Node} into
 * {@link org.jahia.services.content.JCRNodeWrapper}.
 *
 * @author Sergiy Shyrkov
 */
public class NodeIteratorWrapper implements JCRNodeIteratorWrapper {
    private NodeIterator ni;
    private JCRNodeWrapper nextNode = null;
    private JCRSessionWrapper session;
    private JCRStoreProvider provider;

    public NodeIteratorWrapper(NodeIterator ni, final JCRSessionWrapper session, final JCRStoreProvider provider) {
        this.ni = ni;
        this.session = session;
        this.provider = provider;
        prefetchNext();
    }

    private void prefetchNext() {
        do {
            try {
                Node n = null;
                while (ni.hasNext() && (n == null)) {
                    n = ni.nextNode();
                    if (session.getVersionDate() != null || session.getVersionLabel() != null) {
                        try {
                            n = session.getNode(n.getPath());
                        } catch (PathNotFoundException e) {
                            n = null;
                        }
                    }
                }
                if (n != null) {
                    if (session.getLocale() != null && n.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                        try {
                            n = n.getParent();
                        } catch (ItemNotFoundException e) {
                            // keep same node
                        }
                    }
                    try {
                        nextNode = provider.getNodeWrapper(n, session);
                        break;
                    } catch (PathNotFoundException e) {
                        // continue
                    }
                } else {
                    nextNode = null;
                    break;
                }
            } catch (RepositoryException e) {
                e.printStackTrace();
                // continue
            }
        } while (true);
    }

    public void skip(long skipNum) {
        for (int i = 0; i< skipNum; i++) {
            prefetchNext();
        }
        if (nextNode == null) {
            throw new NoSuchElementException();
        }
    }

    public long getSize() {
        return ni.getSize();
    }

    public long getPosition() {
        return ni.getPosition() - (nextNode != null ? 1 : 0);
    }

    public Node nextNode() {
        return (Node) next();
    }

    public boolean hasNext() {
        return (nextNode != null);
    }

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

    public void remove() {
        ni.remove();
    }

    @Override
    public Iterator<JCRNodeWrapper> iterator() {
        return new Iterator<JCRNodeWrapper>() {
            @Override
            public boolean hasNext() {
                return NodeIteratorWrapper.this.hasNext();
            }

            @Override
            public JCRNodeWrapper next() {
                return NodeIteratorWrapper.this.wrappedNext();
            }

            @Override
            public void remove() {
                NodeIteratorWrapper.this.remove();
            }
        };
    }
}
