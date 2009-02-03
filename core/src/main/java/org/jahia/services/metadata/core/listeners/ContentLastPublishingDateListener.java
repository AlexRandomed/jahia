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

package org.jahia.services.metadata.core.listeners;

import java.util.HashMap;
import java.util.Map;

import org.jahia.bin.Jahia;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentFieldKey;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.PageReferenceableInterface;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.fields.ContentField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.version.EntryLoadRequest;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author not attributable
 * @version 1.0
 */

public class ContentLastPublishingDateListener extends JahiaEventListener {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContentLastPublishingDateListener.class);

    private String metadataName;
    private static final Map lastUpdateTimeMap = new HashMap();
    private Long updateTimeMinInterval;

    public void setMetadataName(String metadataName) {
        this.metadataName = metadataName;
    }

    public String getMetadataName() {
        return this.metadataName;
    }

    public Long getUpdateTimeMinInterval() {
        if (updateTimeMinInterval == null) {
            updateTimeMinInterval = new Long(300);
        }
        return updateTimeMinInterval;
    }

    public void setUpdateTimeMinInterval(Long updateTimeMinInterval) {
        this.updateTimeMinInterval = updateTimeMinInterval;
    }

    public void contentActivation(ContentActivationEvent theEvent) {
        try {
            ObjectKey objectKey = (ObjectKey) theEvent.getObject();
            ContentObject contentObject = ContentObject.getContentObjectInstance(objectKey);
            if (contentObject == null) {
                return;
            }
            if (objectKey != null) {
                if (objectKey instanceof ContentFieldKey) {
                    if (((ContentField) contentObject).getContainerID() > 0) {
                        objectKey = new ContentContainerKey(((ContentField) contentObject).getContainerID());
                        try {
                            contentObject = contentObject.getParent(Jahia.getThreadParamBean().getEntryLoadRequest());
                        } catch (JahiaException e) {
                            logger.error(e.getMessage(), e);
                            return;
                        }
                    }
                }
                Long lastUpdateTime;
                synchronized (lastUpdateTimeMap) {
                    lastUpdateTime = (Long) lastUpdateTimeMap.get(contentObject.getObjectKey());
                    if (lastUpdateTime != null) {
                        if (theEvent.getEventTime() < lastUpdateTime.longValue() ||
                                (theEvent.getEventTime() - lastUpdateTime.longValue()) < updateTimeMinInterval.longValue()) {
                            logger.debug("Skip updating last modification metadata for content " + contentObject.getObjectKey());
                            return;
                        }
                    }
                    lastUpdateTimeMap.put(contentObject.getObjectKey(), new Long(theEvent.getEventTime()));
                }
                updateMetadata(contentObject, Jahia.getThreadParamBean(), theEvent);

                // propagate to parent page
                if (contentObject instanceof PageReferenceableInterface) {
                    PageReferenceableInterface pageRefObj = (PageReferenceableInterface) contentObject;
                    try {
                        ContentPage parentPage = pageRefObj.getPage();
                        boolean update = false;
                        if (parentPage != null) {
                            synchronized (lastUpdateTimeMap) {
                                lastUpdateTime = (Long) lastUpdateTimeMap.get(parentPage.getObjectKey());
                                if (lastUpdateTime != null) {
                                    if ((theEvent.getEventTime() - lastUpdateTime.longValue())
                                            > this.getUpdateTimeMinInterval().longValue()) {
                                        update = true;
                                    }
                                } else {
                                    update = true;
                                }
                                if (update) {
                                    lastUpdateTimeMap.put(parentPage.getObjectKey(), new Long(theEvent.getEventTime()));
                                }
                            }
                            if (update) {
                                updateMetadata(parentPage, Jahia.getThreadParamBean(), theEvent);
                            }

                        }
                    } catch (Exception t) {
                        logger.debug("exception occured updating last modif date metadata", t);
                    }
                }

                // propagate to parent container list if need
                if (contentObject instanceof ContentContainer) {
                    ContentContainer container = (ContentContainer) contentObject;
                    ContentContainerList ctnList = ContentContainerList
                            .getContainerList(container.getParentContainerListID());
                    boolean update = false;
                    synchronized (lastUpdateTimeMap) {
                        lastUpdateTime = (Long) lastUpdateTimeMap.get(ctnList.getObjectKey());
                        if (lastUpdateTime != null) {
                            if ((theEvent.getEventTime() - lastUpdateTime.longValue())
                                    > this.getUpdateTimeMinInterval().longValue()) {
                                update = true;
                            }
                        } else {
                            update = true;
                        }
                        if (update) {
                            lastUpdateTimeMap.put(ctnList.getObjectKey(), new Long(theEvent.getEventTime()));
                        }
                    }
                    if (update) {
                        updateMetadata(ctnList, theEvent.getProcessingContext(), theEvent);
                    }

                }
            }
        } catch (Exception t) {
            logger.debug(t);
        }
    }

    protected void updateMetadata(ContentObject contentObject,
                                  ProcessingContext jParams,
                                  JahiaEvent theEvent)
            throws Exception {
        EntryLoadRequest savedEntryLoadRequest = jParams.getSubstituteEntryLoadRequest();
        boolean resetStagingLoadRequest = MetadataTools.switchToStagingEntryLoadRequest(jParams);
        try {
            JahiaField jahiaField = contentObject.getMetadataAsJahiaField(this.getMetadataName(), jParams, true);
            if (jahiaField == null) {
                synchronized(lastUpdateTimeMap) {lastUpdateTimeMap.remove(contentObject.getObjectKey());}
                return;
            }
            jahiaField.setObject(String.valueOf(theEvent.getEventTime()));
            jahiaField.save(jParams);
        } finally {
            if (resetStagingLoadRequest){
                jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);
            }
        }
    }

}
