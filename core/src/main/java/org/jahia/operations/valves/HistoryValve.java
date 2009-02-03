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

package org.jahia.operations.valves;

import org.jahia.pipelines.valves.ValveContext;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.PipelineException;
import org.jahia.params.ProcessingContext;
import org.jahia.data.beans.history.HistoryBean;
import org.jahia.exceptions.JahiaException;

import java.util.List;
import java.util.ArrayList;

/**
 * User: jahia
 * Date: 25 avr. 2008
 * Time: 17:00:24
 */
public class HistoryValve implements Valve {
    protected static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HistoryValve.class);
    public static final String ORG_JAHIA_TOOLBAR_HISTORY = "org.jahia.toolbar.history";

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        logger.debug("starting " + this.getClass().getName() + ".invoke()...");
        ProcessingContext processingContext = (ProcessingContext) context;

        // update history bean list
        updateHistoryBeanList(processingContext);


        // invoke next valve
        valveContext.invokeNext(context);
    }

    private void updateHistoryBeanList(ProcessingContext processingContext) {
        List<HistoryBean> historyBeanList = (List<HistoryBean>) processingContext.getSessionState().getAttribute(ORG_JAHIA_TOOLBAR_HISTORY);
        if (historyBeanList == null) {
            historyBeanList = new ArrayList();
        }

        // create a new history bean
        HistoryBean historyBean = new HistoryBean();
        historyBean.setPid(processingContext.getPageID());
        if (processingContext.getThePage() != null) {
            historyBean.setPageTitle(processingContext.getThePage().getTitle());
        }
        try {
            historyBean.setUrl(processingContext.composePageUrl(processingContext.getPageID()));
        } catch (JahiaException e) {
            logger.error(e, e);
        }

        // remove if exist
        if (historyBeanList.contains(historyBean)) {
            // if same history bean, juste remove it
             historyBeanList.remove(historyBean);
        }

        // add it to history bean position
        historyBeanList.add(0, historyBean);

        // TO Do: add it in jahia.properties
        int maxHistory = 10;
        if (historyBeanList.size() > 11) {
            historyBeanList.remove(historyBeanList.size() - 1);
        }

        processingContext.getSessionState().setAttribute(ORG_JAHIA_TOOLBAR_HISTORY, historyBeanList);
    }

    public void initialize() {
        // nothing to do here
    }
}
