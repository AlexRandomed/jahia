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

 package org.jahia.data.beans;

import org.apache.log4j.Logger;
import org.jahia.content.ContentObject;
import org.jahia.content.JahiaObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageDefinition;


import java.util.*;

/**
 * <p>Title: Page bean used in views</p>
 * <p>Description: This bean is a facade object that is designed to be used
 * with different view systems such as JSP (or other technologies) templates.
 * It is a little redundant with the JahiaPage object, but the accessors here
 * are fully JavaBean compliant and this way we can keep the "real" objects
 * for compatibility reasons.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber, Xavier Lawrence
 * @version $Id$
 * @deprecated since 6.5
 */

public class PageBean extends ContentBean {

    private static final transient Logger logger = Logger.getLogger(PageBean.class);

    public static final String TYPE = "ContentPage";

    private PageBean parent;
    private Map<String, ActionURIBean> actionURIs;
    private boolean completelyLocked = false;

    private final ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();

    private JahiaPage jahiaPage;

    static {
        registerType(ContentPage.class.getName(), PageBean.class.getName());
    }

    public PageBean() {
    }

    public PageBean(final JahiaPage jahiaPage, final ProcessingContext processingContext) {
        this.jahiaPage = jahiaPage;
        this.processingContext = processingContext;
    }

    public static AbstractJahiaObjectBean getChildInstance(
            final JahiaObject jahiaObject,
            final ProcessingContext processingContext) {
        final ContentPage contentPage = (ContentPage) jahiaObject;
        try {
            return new PageBean(contentPage.getPage(processingContext.
                    getEntryLoadRequest(), processingContext.getOperationMode(),
                    processingContext.getUser()), processingContext);
        } catch (JahiaException je) {
            logger.error(
                    "Error while converting content container to jahia container",
                    je);
            return null;
        }
    }

    public String getBeanType() {
        return TYPE;
    }

    public PageBean getParentPage() {
        if (parent != null) {
            // parent has already been resolved previously
            return parent;
        }
        final int parentID = jahiaPage.getParentID();
        try {
            final ContentPage parentContentPage = ContentPage.getPage(parentID);
            if (parentContentPage == null) {
                return null;
            }
            final JahiaPage parentJahiaPage = parentContentPage.getPage(processingContext.
                    getEntryLoadRequest(), processingContext.getOperationMode(),
                    processingContext.getUser());
            if (parentJahiaPage == null) {
                return null;
            }
            parent = new PageBean(parentJahiaPage, processingContext);
            return parent;
        } catch (JahiaException je) {
            logger.error("Error while trying to retrieve parent page " +
                    parentID + " for page " + getID() + " : ", je);
            return null;
        }
    }

    public int getID() {
        return jahiaPage.getID();
    }

    public int getPageID() {
        return jahiaPage.getID();
    }

    public String getTitle() {
        return jahiaPage.getTitle();
    }

    public String getHighLightDiffTitle() {
        throw new UnsupportedOperationException();
    }

    public int getDefinitionID() {
        return jahiaPage.getPageTemplateID();
    }

    public int getAclID() {
        return jahiaPage.getAclID();
    }

    public JahiaBaseACL getACL() {
        return jahiaPage.getACL();
    }

    public int getCounter() {
        return jahiaPage.getCounter();
    }

    public String getCreator() {
        String creator = "";
        try {
            creator = jahiaPage.getContentPage().getMetadataValue(
                    CoreMetadataConstant.CREATOR, processingContext, "");
        } catch (JahiaException e) {
            // do nothing
        }
        return creator;
    }

    public String getDateOfCreation() {
        return jahiaPage.getDoc();
    }

    public int getSiteID() {
        return jahiaPage.getJahiaID();
    }

    public int getTemplateID() {
        return jahiaPage.getPageTemplateID();
    }

    public JahiaPage getJahiaPage() {
        return jahiaPage;
    }

    public int getPageType() {
        return jahiaPage.getPageType();
    }

    /**
     * @deprecated Use getPageType() instead
     */
    public int getType() {
        return jahiaPage.getPageType();
    }

    public String getUrl() {
        try {
            return jahiaPage.getURL(processingContext);
        } catch (JahiaException je) {
            logger.error("Error while generating URL for page " + getID() + ":",
                    je);
            return null;
        }
    }

    public Map<String, Integer> getLanguageStates() {
        return jahiaPage.getLanguagesStates(false);
    }

    public Map<String, Integer> getLanguageStatesWithContent() {
        return jahiaPage.getLanguagesStates(true);
    }

    public List<PageBean> getPath() {
        try {
            final Iterator<ContentPage> pathEnum = jahiaPage.getContentPagePath(processingContext.
                    getOperationMode(), processingContext.getUser());
            final List<PageBean> pathList = new ArrayList<PageBean>();
            while (pathEnum.hasNext()) {
                final ContentPage curJahiaPage = (ContentPage) pathEnum.next();
                final PageBean curPageBean = new PageBean(curJahiaPage.getPage(processingContext), processingContext);
                pathList.add(curPageBean);
            }
            return pathList;
        } catch (JahiaException je) {
            logger.error("Error while retrieving page path for page " + getID() +
                    ":", je);
            return null;
        }
    }

    public int getLinkID() {
        return jahiaPage.getPageLinkID();
    }

    public JahiaPageDefinition getTemplate() {
        return jahiaPage.getPageTemplate();
    }

    public int getParentID() {
        return jahiaPage.getParentID();
    }

    public String getProperty(final String propertyName) {
        try {
            return jahiaPage.getProperty(propertyName);
        } catch (JahiaException je) {
            logger.error("Error while retrieving property " + propertyName +
                    " for page " + getID() + ":", je);
            return null;
        }
    }

    public String getRemoteURL() {
        return jahiaPage.getRemoteURL();
    }

    public boolean isInCurrentPagePath() {
        try {
            final Iterator<ContentPage> thePath = processingContext.getPage().getContentPagePath(processingContext.
                    getOperationMode(), processingContext.getUser());
            while (thePath.hasNext()) {
                final ContentPage curContentPage = (ContentPage) thePath.next();
                boolean foundTarget = (curContentPage.getID() == processingContext.getPage().getID());
                if (curContentPage.getID() == getID()) {
                    return true;
                }
                if (foundTarget) {
                    break;
                }
            }
        } catch (JahiaException je) {
            logger.error("Error while loading current page path " + processingContext.getPageID() + ":", je);
            return false;
        }
        return false;
    }

    public boolean isCurrentPage() {
        return getID() == processingContext.getPageID();
    }

    public boolean isHomePage() {
        return processingContext.getSite().getHomePageID() == getID();
    }

    public int getLevel() {
        return getPath().size();
    }

    public boolean isPicker() {
        return false;
    }

    public ContentObject getContentObject() {
        return jahiaPage.getContentPage();
    }

    public String getJCRPath() throws JahiaException {
        return this.jahiaPage.getContentPage().getJCRPath(this.processingContext);
    }

    public String getUrlKey() throws JahiaException {
        return jahiaPage.getURLKey();
    }
}
