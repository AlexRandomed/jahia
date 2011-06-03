/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.contentengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.seo.GWTJahiaUrlMapping;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

/**
 * Represents a dedicated tab for configuring URL mapping for content objects
 * and other SEO-related settings.
 * 
 * @author Sergiy Shyrkov
 */
public class SeoTabItem extends EditEngineTabItem {

    private transient UrlMappingEditor activeEditor;

    private transient Map<String, UrlMappingEditor> editorsByLanguage = new HashMap<String, UrlMappingEditor>(1);


    @Override public AsyncTabItem create(GWTEngineTab engineTab, NodeHolder engine) {
        setHandleCreate(false);
        return super.create(engineTab,engine);
    }

    @Override
    public void init(NodeHolder engine, AsyncTabItem tab, String locale) {
        if (engine.getNode() == null) {
            return;
        }

        UrlMappingEditor next = getEditor(engine, tab, locale);
        if (activeEditor != null) {
            if (activeEditor == next) {
                // same as current --> do nothing
                return;
            }
            activeEditor.setVisible(false);
        }
        next.setVisible(true);
        next.layout();
        activeEditor = next;

        tab.layout();
    }

    public void setProcessed(boolean processed) {
        if (!processed && editorsByLanguage != null) {
            editorsByLanguage.clear();
            activeEditor = null;
        }
        super.setProcessed(processed);
    }    
    
    private UrlMappingEditor getEditor(NodeHolder engine, AsyncTabItem tab, String locale) {
        UrlMappingEditor editor = editorsByLanguage.get(locale);
        if (editor == null) {
            boolean editable = (!engine.isExistingNode() || (PermissionsUtils.isPermitted("jcr:modifyProperties", engine.getNode()) && !engine.getNode().isLocked()));
            editor = new UrlMappingEditor(engine.getNode(), locale, editable);
            editor.setVisible(false);
            editorsByLanguage.put(locale, editor);
            tab.add(editor);
        }
        return editor;
    }
    
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties) {
        Set<String> langs = new HashSet<String>(editorsByLanguage.keySet());
        if (langs.isEmpty()) {
            return;
        }
        List<GWTJahiaUrlMapping> mappings = new ArrayList<GWTJahiaUrlMapping>();
        for (UrlMappingEditor editor : editorsByLanguage.values()) {
            mappings.addAll(editor.getMappings());
        }
        if (!node.getNodeTypes().contains("jmix:vanityUrlMapped")) {
            node.getNodeTypes().add("jmix:vanityUrlMapped");
        }
        node.set("vanityMappings", mappings);
    }
}
