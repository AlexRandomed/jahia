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
package org.jahia.services.uicomponents.bean.editmode;

import org.jahia.ajax.gwt.client.widget.contentengine.EditEngineTabItem;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.uicomponents.bean.Visibility;
import org.jahia.services.uicomponents.bean.contentmanager.ManagerConfiguration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * User: ktlili
 * Date: Apr 14, 2010
 * Time: 12:37:29 PM
 */
public class EngineTab implements Serializable, Comparable<EngineTab>, InitializingBean, DisposableBean {
    
    private static final long serialVersionUID = -5995531303789738603L;
    
    private String id;
    private String title;
    private String titleKey;
    private Visibility visibility;
    private EditEngineTabItem tabItem;
    private int order;
    private String requiredPermission;

    private Object parent;
    private Object parentEditConfiguration;
    private Object parentManagerConfiguration;
    private int position = -1;
    private String positionAfter;
    private String positionBefore;
    
    public EngineTab() {
        super();
    }
    
    public EngineTab(String id) {
        this();
        setId(id);
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public EditEngineTabItem getTabItem() {
        return tabItem;
    }

    public void setTabItem(EditEngineTabItem item) {
        this.tabItem = item;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public void setParentEditConfiguration(EditConfiguration config) {
        this.parentEditConfiguration = config;
    }

    public void setParentManagerConfiguration(ManagerConfiguration config) {
        this.parentManagerConfiguration = config;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public int compareTo(EngineTab o) {
        return getOrder() - o.getOrder();
    }

    public void afterPropertiesSet() throws Exception {
        if (parent instanceof List) {
            for (Object o : (List) parent) {
                addTab(o);
            }
        } else {
            addTab(parent);
        }
        addTab(parentManagerConfiguration);
        addTab(parentEditConfiguration);
    }

    public void destroy() throws Exception {
        if (!JahiaContextLoaderListener.isRunning()) {
            return;
        }
        if (parent instanceof List) {
            for (Object o : (List) parent) {
                removeTab(getEngineTabs(o), getId());
            }
        } else {
            removeTab(getEngineTabs(parent), getId());
        }
        removeTab(getEngineTabs(parent), getId());
        removeTab(getEngineTabs(parentEditConfiguration), getId());
        removeTab(getEngineTabs(parentManagerConfiguration), getId());
    }

    private void addTab(Object parent) {
        List<EngineTab> tabs = getEngineTabs(parent);

        if (tabs != null) {
            removeTab(tabs, getId());

            int index = -1;
            if (position >= 0) {
                index = position;
            } else if (positionBefore != null) {
                index = tabs.indexOf(new EngineTab(positionBefore));
            } else if (positionAfter != null) {
                index = tabs.indexOf(new EngineTab(positionAfter));
                if (index != -1) {
                    index++;
                }
                if (index >= tabs.size()) {
                    index = -1;
                }
            }
            if (index != -1) {
                tabs.add(index, this);
            } else {
                tabs.add(this);
            }
        } else if (this.parent != null) {
            throw new IllegalArgumentException("Unknown parent type '"
                    + this.parent.getClass().getName()
                    + "'. Can accept EditConfiguration, ManagerConfiguration, Engine or"
                    + " a String value with a beanId of the those beans");
        }
    }

    protected static void removeTab(List<EngineTab> tabs, String tabId) {
        if (tabs != null && tabId != null && tabId.length() > 0) {
            for (Iterator<EngineTab> iterator = tabs.iterator(); iterator.hasNext();) {
                EngineTab tab = iterator.next();
                if (tab.getId() != null && tab.getId().equals(tabId)) {
                    iterator.remove();
                }
            }
        }
    }

    private List<EngineTab> getEngineTabs(Object parent) {
        if (parent == null) {
            return null;
        }
        if (parent instanceof String) {
            parent = SpringContextSingleton.getBean((String) parent);
        }
        List<EngineTab> tabs = null;
        if (parent instanceof EditConfiguration) {
            tabs = ((EditConfiguration) parent).getDefaultEditConfiguration().getEngineTabs();
            if (tabs == null) {
                tabs = new LinkedList<EngineTab>();
                ((EditConfiguration) parent).getDefaultEditConfiguration().setEngineTabs(tabs);
            }
        } else if (parent instanceof ManagerConfiguration) {
            tabs = ((ManagerConfiguration) parent).getEngineTabs();
            if (tabs == null) {
                tabs = new LinkedList<EngineTab>();
                ((ManagerConfiguration) parent).setEngineTabs(tabs);
            }
        } else if (parent instanceof EngineConfiguration) {
            tabs = ((EngineConfiguration) parent).getEngineTabs();
            if (tabs == null) {
                tabs = new LinkedList<EngineTab>();
                ((EngineConfiguration) parent).setEngineTabs(tabs);
            }
        } else if (parent instanceof Engine) {
            tabs = ((Engine) parent).getTabs();
            if (tabs == null) {
                tabs = new LinkedList<EngineTab>();
                ((Engine) parent).setTabs(tabs);
            }
        }
        return tabs;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setPositionAfter(String positionAfter) {
        this.positionAfter = positionAfter;
    }

    public void setPositionBefore(String positionBefore) {
        this.positionBefore = positionBefore;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj != null && this.getClass() == obj.getClass()) {
            EngineTab other = (EngineTab) obj;
            return getId() != null ? other.getId() != null && getId().equals(other.getId()) : other
                    .getId() == null;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
