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
package org.jahia.services.seo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.io.Serializable;

/**
 * Vanity Url mapped to content
 * 
 * @author Benjamin Papez
 * 
 */
public class VanityUrl implements Serializable {

    private static final long serialVersionUID = 5736971828491108878L;

    private String url;

    private String language;

    private String identifier;

    private String path;

    private String site;

    private boolean defaultMapping;

    private boolean active;

    public VanityUrl() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * @param url mapping URL
     * @param site virtual site of the mapped content object
     * @param language the mapping language
     */
    public VanityUrl(String url, String site, String language) {
        super();
        this.url = StringUtils.stripEnd(StringUtils.startsWith(url, "/") ? url : '/' + url, "/");
        this.site = site;
        this.language = language;
    }

    /**
     * Initializes an instance of this class.
     * @param url mapping URL
     * @param site virtual site of the mapped content object
     * @param language the mapping language
     * @param defaultMapping is it the default mapping for the node?
     * @param active whether the mapping is active
     */
    public VanityUrl(String url, String site, String language, boolean defaultMapping, boolean active) {
        this(url, site, language);
        this.defaultMapping = defaultMapping;
        this.active = active;
    }

    /**
     * Returns the vanity URL 
     * @return the vanity URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the vanity URL
     * @param url the vanity URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the language of the content object to which the vanity URL maps to 
     * @return language of the mapping
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the language of the content object to which the vanity URL maps to
     * @param language the mapping language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Returns true whether this URL mapping is the default one for the language
     * @return true whether this URL mapping is the default one for the language, otherwise false
     */
    public boolean isDefaultMapping() {
        return defaultMapping;
    }

    /**
     * Sets the default flag for the URL mapping - true if mapping should be the 
     * default for the language, otherwise false
     * @param defaultMapping true if mapping should be the default for the language, otherwise false
     */
    public void setDefaultMapping(boolean defaultMapping) {
        this.defaultMapping = defaultMapping;
    }

    /**
     * Returns true if the URL mapping is activated or false if it is not activated.
     * @return true if the URL mapping is activated or false if it is not activated.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the activation flag for the URL mapping - true if mapping is active,
     * or false if is not active (will not be assigned and resource can no longer be found via the mapping)
     * @param active true if mapping should be active, otherwise false
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Gets the UUID of the mapping node in the JCR repository or null if URL is 
     * not saved yet
     * @return the UUID of the mapping node in the JCR repository or null if not saved yet 
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Set the unique identifier of the node in the JCR repository
     * @param identifier the unique identifier (JCR repository node UUID)
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the path of the node in the JCR repository or null if not yet set 
     * @return the path of the node in the JCR repository or null if not yet set
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the path of the node in the JCR repository
     * @param path the path of the node in the JCR repository
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Gets the virtual site of the mapped content object
     * @return the virtual site of the mapped content object
     */
    public String getSite() {
        return site;
    }

    /**
     * Sets the virtual site of the mapped content object
     * @param site virtual site of the mapped content object
     */
    public void setSite(String site) {
        this.site = site;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getUrl()).append(getPath()).append(
                getLanguage()).append(getSite()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj)
                || obj != null
                && (this.getClass() == obj.getClass())
                && new EqualsBuilder().append(getUrl(),
                        ((VanityUrl) obj).getUrl()).append(getPath(),
                        ((VanityUrl) obj).getPath()).append(getLanguage(),
                        ((VanityUrl) obj).getLanguage()).append(getSite(),
                        ((VanityUrl) obj).getSite()).isEquals();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
