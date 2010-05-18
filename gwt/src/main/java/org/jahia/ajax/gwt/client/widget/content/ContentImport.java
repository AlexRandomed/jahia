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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.form.FileUploadField;

/**
 * User: toto
 * Date: Nov 13, 2008 - 7:31:46 PM
 */
public class ContentImport extends Window {

    private Linker m_linker;

    public ContentImport(final Linker linker, final GWTJahiaNode n) {
        super();

        m_linker = linker;

        setHeading(Messages.getResource("label.export"));
        setSize(500, 150);
        setResizable(false);
        setModal(true);
        ButtonBar buttons = new ButtonBar();

        final FormPanel form = new FormPanel();
        form.setFrame(false);
        form.setHeaderVisible(false);
        form.setBorders(false);

        final FileUploadField field = new FileUploadField("import");
        field.setFieldLabel(Messages.getResource("label.import"));
        form.add(field);

        Button submit = new Button(Messages.getResource("label.ok"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                doImport(n.getPath(), field.getValue());
            }
        });

        Button cancel = new Button(Messages.getResource("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });

        buttons.add(submit);
        buttons.add(cancel);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setBottomComponent(buttons);
        add(form);
    }

    public void doImport(String path, Object value) {
        Log.debug(path + " " + value);
        JahiaContentManagementService.App.getInstance().importContent(path, value.toString(), new BaseAsyncCallback() {

            public void onApplicationFailure(Throwable caught) {
                com.google.gwt.user.client.Window.alert(Messages.getResource("fm_fail") + "\n" + caught.getLocalizedMessage());
                Log.error(Messages.getResource("fm_fail"), caught);
            }

            public void onSuccess(Object result) {
                m_linker.refresh(Linker.REFRESH_ALL);
                hide();
            }
        });
    }

}