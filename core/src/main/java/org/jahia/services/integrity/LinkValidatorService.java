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

package org.jahia.services.integrity;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.integrity.Link.Type;
import org.jahia.services.version.RevisionEntry;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.HTMLElementName;

/**
 * Jahia service implementation for performing different kinds of link
 * validations.
 * 
 * @author Sergiy Shyrkov
 */
public class LinkValidatorService extends JahiaService {

    private static final transient Logger logger = Logger
            .getLogger(LinkValidatorService.class);

    private static void addLinks(Source source, String tagName,
            String attributeName, RevisionEntry field, List<Link> foundLinks) {
        List<StartTag> tags = source.getAllStartTags(tagName);
        for (StartTag tag : tags) {
            String url = tag.getAttributeValue(attributeName);
            if (url != null
                    && url.length() > 0
                    && (url.startsWith("http://") || url.startsWith("https://"))) {
                foundLinks.add(new Link(Type.EXTERNAL, url, field));
            }
        }
    }

    /**
     * Returns an instance of this service.
     * 
     * @return an instance of this service
     */
    public static LinkValidatorService getInstace() {
        return (LinkValidatorService) SpringContextSingleton
                .getBean(LinkValidatorService.class.getName());
    }

    private Map<Link.Type, LinkValidator> validators = Collections.EMPTY_MAP;

    /**
     * Returns all links to be validated for the specified site ID.
     * 
     * @param siteId
     *            the ID of the site, where links will be validated.
     * @return a list of {@link Link} objects to be validated
     * @see #validate(List)
     */
    public List<Link> getAllLinks(int siteId) {
        List<Link> links = new LinkedList<Link>();

        if (logger.isDebugEnabled()) {
            logger.info("Found " + links.size()
                    + " external page links to be validated for site with ID="
                    + siteId);
        }

        return links;
    }

    /**
     * Returns the {@link LinkValidator} instance according to the specified
     * link type.
     * 
     * @param linkType
     *            the type of the link to be validated
     * @return the {@link LinkValidator} instance according to the specified
     *         link type
     */
    private LinkValidator getValidator(Link.Type linkType) {
        LinkValidator validator = validators.get(linkType);
        if (validator == null) {
            throw new UnsupportedOperationException("Unsupported link type '"
                    + linkType + "'. Not validator can be found to handle it.");
        }

        return validator;
    }

    public void setValidators(Map<Link.Type, LinkValidator> validators) {
        this.validators = validators;
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.JahiaService#start()
     */
    @Override
    public void start() throws JahiaInitializationException {
        // do nothing
        logger.debug("LinkValidatorService is started.");
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.services.JahiaService#stop()
     */
    @Override
    public void stop() throws JahiaException {
        logger
                .debug("Shutting down all instances of the MultiThreadedHttpConnectionManager");
        MultiThreadedHttpConnectionManager.shutdownAll();
        logger.debug("LinkValidatorService stopped.");
    }

    /**
     * Performs validation for the specified list of links.
     * 
     * @param links
     *            links to be validated
     * @return a list of {@link LinkValidationResult} objects with the results
     *         of validation
     */
    public Map<Link, LinkValidationResult> validate(List<Link> links) {
        Map<Link, LinkValidationResult> results = ListOrderedMap
                .decorate(new HashMap<Link, LinkValidationResult>());
        Map<Type, Map<String, LinkValidationResult>> cache = LazyMap.decorate(
                new HashMap<Type, Map<String, LinkValidationResult>>(),
                new Factory() {
                    public Object create() {
                        return new HashMap<String, LinkValidationResult>();
                    }
                });
        for (Link link : links) {
            LinkValidationResult linkResult = cache.get(link.getType()).get(
                    link.getUrl());
            if (null == linkResult) {
                linkResult = getValidator(link.getType()).validate(link);
                cache.get(link.getType()).put(link.getUrl(), linkResult);
            }
            results.put(link, linkResult);
        }
        cache.clear();

        return results;
    }

}