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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.commons.client.ui.form;

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.config.client.JahiaGWTParameters;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.google.gwt.dom.client.*;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 26, 2008
 * Time: 2:23:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileUploadField extends AdapterField {
    public FileUploadField(String name) {
        super(new Uploader(name));

        setName(name);
        ((Uploader)getWidget()).addListener( new Listener() {
            public void onChange(Event event) {
                setValue(((Uploader)getWidget()).getKey());
//                setRawValue();
                // set the value ...
            }
        });
    }

    static class Uploader extends HorizontalPanel {
        private String name;
        private String key;
        private FormPanel form;
        private Text status;
        private Button clear;
        private NamedFrame target;

        private List<Listener> listeners = new ArrayList<Listener>();

        public Uploader(String defname) {
            super();

            status = new Text();

            target = new NamedFrame("target"+defname) {
                public void onBrowserEvent(Event event) {
                    Log.debug("LOADED");
                    Log.error(((FrameElement)getElement().cast()).getContentDocument().getClass().toString());
                    Document document = ((FrameElement) getElement().cast()).getContentDocument();
                    Log.error(document.getClass().toString());
                    Element elem = document.getElementById("uploaded");
                    if (elem != null) {
                        name = (elem.getAttribute("name"));
                        key = (elem.getAttribute("key"));
                        status.setText(name);
                        clear.setText("Clear");
                        for (Listener listener : listeners) {
                            listener.onChange(event);
                        }
                    }
                    super.onBrowserEvent(event);    //To change body of overridden methods use File | Settings | File Templates.
                }
            };
            target.sinkEvents(Event.ONLOAD);
            target.setVisible(false);
            form = new FormPanel(target);
            String entryPoint = JahiaGWTParameters.getServiceEntryPoint();
            if (entryPoint == null) {
                entryPoint = "/gwt/";
            }
            form.setAction(entryPoint + "fileupload"); // should do
            form.setEncoding(FormPanel.ENCODING_MULTIPART);
            form.setMethod(FormPanel.METHOD_POST);

            clear = new Button("Stop", new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    init();
                }

            });

            init();

            add(status);
            add(form);
            add(clear);
            add(target);
        }

        public void addListener(Listener list) {
            listeners.add(list);
        }

        public String getName() {
            return name;
        }

        public String getKey() {
            return key;
        }

        private void init() {
            name = null;
            key = null;

            final FileUpload upload = new FileUpload() {

                public final void onBrowserEvent(Event event) {
                    status.setText("Uploading ...");
                    form.submit();
                    form.setVisible(false);
                    clear.setText("Stop");
                    clear.setVisible(true);
                    super.onBrowserEvent(event);
                }
            };
            upload.sinkEvents(Event.ONCHANGE | Event.ONKEYUP);
            upload.setName("asyncupload");
            form.clear();
            form.add(upload);
            form.setVisible(true);
            status.setText("");
            clear.setVisible(false);
        }


    }

    interface Listener {
        public void onChange(Event event);
    }

}
