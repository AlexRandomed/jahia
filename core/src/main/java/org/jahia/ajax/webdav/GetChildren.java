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

package org.jahia.ajax.webdav;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.engines.filemanager.TableEntry;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRNodeWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.Collator;
import java.util.*;

/**
 * Struts AJAX based Action that collects all the direct children WEBDAV directory.
 *
 * @author Xavier Lawrence
 */
public class GetChildren extends DAVAbstractAction {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GetChildren.class);

    protected static final String SORT_BY = "sortBy";
    protected static final String SIZE = "size";
    protected static final String MODIFIED = "modified";
    protected static final String NAME = "name";
    protected static final String SIZE_DESC = "sizeDesc";
    protected static final String MODIFIED_DESC = "modifiedDesc";
    protected static final String NAME_DESC = "nameDesc";

    private static final Comparator sizeComparator = new Comparator() {
        public int compare(final Object o1, final Object o2) {
            final JCRNodeWrapper fileAccess1 = (JCRNodeWrapper) o1;
            final JCRNodeWrapper fileAccess2 = (JCRNodeWrapper) o2;
            final boolean collection1 = fileAccess1.isCollection();
            final boolean collection2 = fileAccess2.isCollection();
            if (collection1 != collection2 || (collection1 && collection2)) {
                if (collection1 && collection2) {
                    final Collator collator = Collator.getInstance(Locale.ENGLISH);
                    return collator.compare(fileAccess1.getPath(), fileAccess2.getPath());
                }
                return collection1 ? -1 : 1;
            } else {
                final Integer size1 = new Integer(getFileSizeInBytes(fileAccess1));
                final Integer size2 = new Integer(getFileSizeInBytes(fileAccess2));
                final int result = size1.compareTo(size2);

                if (result == 0) {
                    final Collator collator = Collator.getInstance(Locale.ENGLISH);
                    return collator.compare(fileAccess1.getPath(), fileAccess2.getPath());
                } else {
                    return result;
                }
            }
        }
    };

    private static final Comparator modifiedComparator = new Comparator() {
        public int compare(final Object o1, final Object o2) {
            final JCRNodeWrapper fileAccess1 = (JCRNodeWrapper) o1;
            final JCRNodeWrapper fileAccess2 = (JCRNodeWrapper) o2;
            final boolean collection1 = fileAccess1.isCollection();
            final boolean collection2 = fileAccess2.isCollection();
            if (collection1 != collection2) {
                return collection1 ? -1 : 1;
            } else {
                    final Date date1 = fileAccess1.getLastModifiedAsDate();
                    final Date date2 = fileAccess2.getLastModifiedAsDate();
                    final int result = date1.compareTo(date2);

                    if (result == 0) {
                        final Collator collator = Collator.getInstance(Locale.ENGLISH);
                        return collator.compare(fileAccess1.getPath(), fileAccess2.getPath());
                    } else {
                        return result;
                    }

            }
        }
    };

    private static final Comparator sizeComparatorDesc = new Comparator() {
         public int compare(final Object o1, final Object o2) {
             final JCRNodeWrapper fileAccess1 = (JCRNodeWrapper) o1;
             final JCRNodeWrapper fileAccess2 = (JCRNodeWrapper) o2;
             final boolean collection1 = fileAccess1.isCollection();
             final boolean collection2 = fileAccess2.isCollection();
             if (collection1 != collection2 || (collection1 && collection2)) {
                 if (collection1 && collection2) {
                     final Collator collator = Collator.getInstance(Locale.ENGLISH);
                     return -(collator.compare(fileAccess1.getPath(), fileAccess2.getPath()));
                 }
                 return collection1 ? 1 : -1;
             } else {
                 final Integer size1 = new Integer(getFileSizeInBytes(fileAccess1));
                 final Integer size2 = new Integer(getFileSizeInBytes(fileAccess2));
                 final int result = size1.compareTo(size2);

                 if (result == 0) {
                     final Collator collator = Collator.getInstance(Locale.ENGLISH);
                     return -(collator.compare(fileAccess1.getPath(), fileAccess2.getPath()));
                 } else {
                     return -result;
                 }
             }
         }
     };

     private static final Comparator modifiedComparatorDesc = new Comparator() {
         public int compare(final Object o1, final Object o2) {
             final JCRNodeWrapper fileAccess1 = (JCRNodeWrapper) o1;
             final JCRNodeWrapper fileAccess2 = (JCRNodeWrapper) o2;
             final boolean collection1 = fileAccess1.isCollection();
             final boolean collection2 = fileAccess2.isCollection();
             if (collection1 != collection2) {
                 return -(collection1 ? -1 : 1);
             } else {
                     final Date date1 = fileAccess1.getLastModifiedAsDate();
                     final Date date2 = fileAccess2.getLastModifiedAsDate();
                     final int result = date1.compareTo(date2);

                     if (result == 0) {
                         final Collator collator = Collator.getInstance(Locale.ENGLISH);
                         return -(collator.compare(fileAccess1.getPath(), fileAccess2.getPath()));
                     } else {
                         return -result;
                     }
             }
         }
     };


    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {
        try {
//            final String folderPath = getParameter(request, response, FOLDER_PATH, "");
            final String path = getXmlNodeValue(request, KEY);
            final String sortBy = getParameter(request, SORT_BY, "name");
            final ProcessingContext jParams = retrieveProcessingContext(request, response);

            logger.debug("Getting children for directory: " + path);
            if (! "/".equals(path)) {
                jParams.getSessionState().setAttribute("previousPath", path);
            }

            final JCRNodeWrapper df = jahiaWebdavBaseService.getDAVFileAccess(
                    TableEntry.javascriptDecode(path), jParams.getUser());

            request.getSession().removeAttribute(GetFileManagerToolBar.TOOLBAR_VALUES);
            final List children = df.getChildren();
            final Collator collator = Collator.getInstance(jParams.getLocale());
            final TreeSet sortedResources;
            logger.debug("Sorting by: " + sortBy);
            if (NAME.equals(sortBy)) {
                sortedResources = new TreeSet(new Comparator() {
                public int compare(final Object o1, final Object o2) {
                    final JCRNodeWrapper fileAccess1 = (JCRNodeWrapper) o1;
                    final JCRNodeWrapper fileAccess2 = (JCRNodeWrapper) o2;
                    final boolean collection1 = fileAccess1.isCollection();
                    final boolean collection2 = fileAccess2.isCollection();
                    if (collection1 != collection2) {
                        return collection1 ? -1 : 1;
                    } else {
                        final int i = collator.compare(fileAccess1.getPath(), fileAccess2.getPath());
                        return i == 0 ? 1 : i;
                    }
                }
            });

            } else if (NAME_DESC.equals(sortBy)) {
                sortedResources = new TreeSet(new Comparator() {
                    public int compare(final Object o1, final Object o2) {
                        final JCRNodeWrapper fileAccess1 = (JCRNodeWrapper) o1;
                        final JCRNodeWrapper fileAccess2 = (JCRNodeWrapper) o2;
                        final boolean collection1 = fileAccess1.isCollection();
                        final boolean collection2 = fileAccess2.isCollection();
                        if (collection1 != collection2) {
                            return collection1 ? 1 : -1;
                        } else {
                            final int i = collator.compare(fileAccess1.getPath(), fileAccess2.getPath());
                            return -(i == 0 ? 1 : i);
                        }
                    }
                });

            } else if (SIZE.equals(sortBy)) {
                sortedResources = new TreeSet(sizeComparator);

            } else if (SIZE_DESC.equals(sortBy)) {
                sortedResources = new TreeSet(sizeComparatorDesc);

            } else if (MODIFIED.equals(sortBy)) {
                sortedResources = new TreeSet(modifiedComparator);

            } else if (MODIFIED_DESC.equals(sortBy)) {
                sortedResources = new TreeSet(modifiedComparatorDesc);

            } else {
                sortedResources = new TreeSet(new Comparator() {
                    public int compare(final Object o1, final Object o2) {
                        final JCRNodeWrapper fileAccess1 = (JCRNodeWrapper) o1;
                        final JCRNodeWrapper fileAccess2 = (JCRNodeWrapper) o2;
                        final boolean collection1 = fileAccess1.isCollection();
                        final boolean collection2 = fileAccess2.isCollection();
                        if (collection1 != collection2) {
                            return collection1 ? -1 : 1;
                        } else {
                            final int i = collator.compare(fileAccess1.getPath(), fileAccess2.getPath());
                            return i == 0 ? 1 : i;
                        }
                    }
                });
            }

            sortedResources.addAll(children);
            final Document resp = getNewDocument(request);
            final Element root = resp.createElement("GetChildrenResp");
            final Iterator ite = sortedResources.iterator();

            while (ite.hasNext()) {
                final JCRNodeWrapper child = (JCRNodeWrapper) ite.next();
                processObject(child, jParams, resp, root);
            }

            resp.appendChild(root);
            sendResponse(resp, response);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        return null;
    }
}
