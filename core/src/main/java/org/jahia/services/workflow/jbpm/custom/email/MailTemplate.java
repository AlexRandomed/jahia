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

package org.jahia.services.workflow.jbpm.custom.email;

import java.util.ArrayList;
import java.util.List;

public class MailTemplate {

    private AddressTemplate from;
    private AddressTemplate to;
    private AddressTemplate cc;
    private AddressTemplate bcc;
    private String subject;
    private String text;
    private String html;
    private String language;
    private List<AttachmentTemplate> attachmentTemplates = new ArrayList<AttachmentTemplate>();

    public AddressTemplate getFrom() {
        return from;
    }

    public void setFrom(AddressTemplate from) {
        this.from = from;
    }

    public AddressTemplate getTo() {
        return to;
    }

    public void setTo(AddressTemplate to) {
        this.to = to;
    }

    public AddressTemplate getCc() {
        return cc;
    }

    public void setCc(AddressTemplate cc) {
        this.cc = cc;
    }

    public AddressTemplate getBcc() {
        return bcc;
    }

    public void setBcc(AddressTemplate bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<AttachmentTemplate> getAttachmentTemplates() {
        return attachmentTemplates;
    }

    public boolean addAttachmentTemplate(AttachmentTemplate attachmentTemplate) {
        return attachmentTemplates.add(attachmentTemplate);
    }
}
