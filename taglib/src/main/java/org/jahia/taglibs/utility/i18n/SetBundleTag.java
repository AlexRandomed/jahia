/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.utility.i18n;

import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.params.ProcessingContext;
import org.jahia.services.render.RenderContext;
import org.jahia.taglibs.utility.Utils;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Used to set the resource bundle to be used in the page by &lt;fmt:message/&gt; tags.
 * User: rincevent
 * Date: 26 févr. 2009
 * Time: 16:34:03
 */
@SuppressWarnings("serial")
public class SetBundleTag extends TagSupport {
    
    private static final transient Logger logger = Logger.getLogger(SetBundleTag.class);
    
    private String basename;
    private String var;
    private int scope;
    private boolean useUiLocale = false;

    public SetBundleTag() {
        super();
        init();
    }

    private void init() {
        basename = null;
        useUiLocale = false;
        scope = PageContext.PAGE_SCOPE;
    }

    @Override
    public int doStartTag() throws JspException {
        // Position localisationContext
        if (basename != null && !"".equals(basename)) {
            RenderContext context = null;
            Locale locale = null;
            try {
                context = Utils.getRenderContext(pageContext);
                locale = context != null ? (useUiLocale ? context.getUILocale() : context.getMainResourceLocale()) : pageContext.getRequest().getLocale();
            } catch (Exception e) {
                logger.debug(e.getMessage(), e);
                locale = pageContext.getRequest().getLocale();
            }
            ResourceBundle resourceBundle = null;
            try {
                resourceBundle = new JahiaResourceBundle(basename,
                        locale,
                        context != null && context.getSite() != null && context.getSite().getSession().isLive() ? context
                                .getSite().getTemplatePackageName() : null);
            } catch (RepositoryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            LocalizationContext locCtxt = new LocalizationContext(resourceBundle, locale);
            if (var != null) {
                pageContext.setAttribute(var, locCtxt, scope);
            } else {
                Config.set(pageContext, Config.FMT_LOCALIZATION_CONTEXT, locCtxt, scope);
            }
        } else {
            logger.warn("Provided 'basename' for the tag is either null or empty");
        }
        return EVAL_BODY_INCLUDE;
    }

    public void setBasename(String basename) {
        this.basename = basename;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }
    
    public void setUseUILocale(String useUILocale) {
        this.useUiLocale = Boolean.parseBoolean(useUILocale);
    }
}
