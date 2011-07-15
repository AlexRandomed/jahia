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

package org.jahia.services.workflow.jbpm;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mail.MailEndpoint;
import org.springframework.beans.factory.DisposableBean;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.mail.MailServiceImpl;
import org.jbpm.pvm.internal.email.spi.MailSession;

import javax.mail.Message;
import java.util.Collection;

/**
 * Mail session used in the jBPM processes.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 14 sept. 2010
 */
public class JBPMMailSession implements MailSession, DisposableBean {
    private MailServiceImpl mailService;
    private ProducerTemplate template;

    public JBPMMailSession() {
        mailService = (MailServiceImpl) SpringContextSingleton.getBean("MailService");
        template = mailService.getCamelContext().createProducerTemplate();
    }

    public void send(Collection<Message> emails) {
        if (mailService.isEnabled()) {
            for (Message email : emails) {
                CamelContext context = mailService.getCamelContext();
                MailEndpoint endpoint = (MailEndpoint) context.getEndpoint(mailService.getEndpointUri());
                Exchange exchange = endpoint.createExchange(email);
                template.send("seda:mailUsers?multipleConsumers=true",
                        exchange);
            }
        }
    }

	public void destroy() throws Exception {
		if (template != null) {
			template.stop();
		}
    }
}
