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

//
package org.jahia.services.metadata;

import java.util.List;

import org.aopalliance.intercept.Interceptor;
import org.jahia.content.ContentDefinition;
import org.jahia.content.ObjectKey;
import org.jahia.data.events.JahiaEventListenerInterface;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;

/**
 * Metadata Service
 *
 * @author Khue Nguyen
 */
public abstract class MetadataService extends JahiaService
{

    /**
     * Reload configuration file from disk
     *
     * @throws JahiaException
     */
    public abstract void reloadConfigurationFile()
    throws java.io.FileNotFoundException, JahiaException;

    /**
     * Return the JahiaEventListener used to handle Metadata
     *
     */
    public abstract JahiaEventListenerInterface getMetadataEventListener();

    /**
     * Add an aopalliance interceptor to the JahiaEventListener
     *
     */
    public abstract void addAOPInterceptor(Interceptor interceptor);

    /**
     * Returns an array list of metadata that match this contentDefinition
     *
     * @param contentDefinition ContentDefinition
     * @return boolean
     */
    public abstract List getMatchingMetadatas(ContentDefinition contentDefinition);

    /**
     * Returns true if the given field definition is declared in metadata config file
     * @param name the metadata name ( jahia field definition name )
     * @return
     */
    public abstract boolean isDeclaredMetadata(String name);

    /**
     * Create all metadata associations between a ContentDefinition and
     * registered Metadata Definitions
     *
     * @param contentDefinition ContentDefinition
     * @throws JahiaException
     */
    public abstract void assignMetadataToContentDefinition(ContentDefinition contentDefinition)
    throws JahiaException;

    /**
     * Returns an array of ObjectKey that are metadatas
     *
     * @param name String, the metadata name
     * @throws JahiaException
     * @return ArrayList
     */
    public abstract List<ObjectKey> getMetadataByName(String name)
    throws JahiaException;

    /**
     * Returns an array of ObjectKey that are metadatas of a given site
     *
     * @param name String, the metadata name
     * @param siteId, the site id
     * @throws JahiaException
     * @return ArrayList
     * @throws JahiaException
     */
    public abstract List<ObjectKey> getMetadataByName(String name, int siteId)
    throws JahiaException;

}

