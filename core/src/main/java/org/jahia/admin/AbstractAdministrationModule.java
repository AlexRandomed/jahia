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

package org.jahia.admin;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.services.rbac.PermissionIdentity;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.i18n.JahiaResourceBundle;

/**
 * Base implementation of the Jahia administration module.
 * 
 * @author Serge Huber
 * Date: Feb 2, 2009
 * Time: 10:04:21 AM
 */
public abstract class AbstractAdministrationModule implements AdministrationModule {

    private String icon;
    private String iconSmall;
    private String label;
    private String link;
    private String name;
    private String tooltip;
    private String urlKey;
    private String permissionName;
    private int rank; 
    private String urlType;
    private String urlAction;
    private String urlParams;
    private boolean serverModule;
    private AdministrationModulesRegistry registry;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public boolean isServerModule() {
        return serverModule;
    }

    public void setServerModule(boolean serverModule) {
        this.serverModule = serverModule;
    }

    public AdministrationModulesRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(AdministrationModulesRegistry registry) {
        this.registry = registry;
    }

    public boolean isEnabled(JahiaUser user, String siteKey) {
        if (StringUtils.isEmpty(permissionName)) {
            // no permission check required
            return true;
        }
        if (isServerModule()) {
            return user.isPermitted(new PermissionIdentity(permissionName));
        } else {
            return user.isPermitted(new PermissionIdentity(permissionName, siteKey));
        }
    }
    
    public boolean isSelected(ParamBean ctx) {
        boolean selected = false;
        if ("module".equals(urlType)) {
            selected = StringUtils.equals(getUrlAction(), ctx.getParameter("do"));
        } else if ("struts".equals(urlType)) {
            selected = StringUtils.equals(getUrlKey(), ctx.getParameter("group"));
        }
        
        return selected;
    }

    public String getActionURL(ParamBean paramBean) throws JahiaException {
        if (urlType == null) {
            return urlAction;
        }
        
        if ("module".equals(urlType)) {
            return JahiaAdministration.composeActionURL(paramBean.getRequest(),paramBean.getResponse(),urlAction,urlParams);
        } else if ("struts".equals(urlType)) {
            return paramBean.composeStrutsUrl(urlAction,urlParams);
        } else if ("link".equals(urlType)) {
            return paramBean.getResponse().encodeURL(
                    paramBean.getContextPath() + urlAction);
        } else {
            return urlAction;
        }
    }

    public String getUrlKey() {
        return urlKey;
    }

    public void setUrlKey(String urlKey) {
        this.urlKey = urlKey;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getUrlType() {
        return urlType;
    }

    public void setUrlType(String urlType) {
        this.urlType = urlType;
    }

    public String getUrlAction() {
        return urlAction;
    }

    public void setUrlAction(String urlAction) {
        this.urlAction = urlAction;
    }

    public String getUrlParams() {
        return urlParams;
    }

    public void setUrlParams(String urlParams) {
        this.urlParams = urlParams;
    }

    public void init() {
        registry.add(this);
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getIconSmall() {
        return iconSmall;
    }

    public void setIconSmall(String iconSmall) {
        this.iconSmall = iconSmall;
    }
    
    public static String getMessage(String key) {
        return JahiaResourceBundle.getJahiaInternalResource(key, Jahia.getThreadParamBean().getUILocale());
    }
}
