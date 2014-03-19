/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.cache;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/**
 * Composite cache key, used when we want to create a key that
 * can be flushed using groups.
 * User: Serge Huber
 * Date: Jul 14, 2005
 * Time: 2:35:29 PM
 * Copyright (C) Jahia Inc.
 */
public class GroupCacheKey implements Serializable, Comparable<GroupCacheKey> {

    private static final long serialVersionUID = 9006071384859530347L;
    
    private static String groupSeparator = "#$#G_";
    private static String keyGroupSeparator = "$$$";

    private final Object key;
    private final Set<String> groups;

    /**
     * This contructor will generate a key based on the combination
     * of the group names.
     * In order to avoid name conflict between key and groups names,
     * it is however HIGHLY recommended that you use one of the
     * constructors that seperatly specify the key and group names.
     * @param groups
     */
    public GroupCacheKey(Set<String> groups) {
        this.key = groups.toString();
        this.groups = groups;
    }

    public GroupCacheKey(Object key, Set<String> groups) {
        this.key = key;
        this.groups = groups;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public String[] getGroupArray() {
        return (String[]) groups.toArray(new String[groups.size()]);
    }

    public boolean containsGroup(Object group) {
        return groups.contains(group);
    }

    /*
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object.getClass() == GroupCacheKey.class) {
            GroupCacheKey otherKey = (GroupCacheKey) object;
            return otherKey.equals(key) && otherKey.getGroups().equals(groups);
        } else {
            return false;
        }
    }
    */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupCacheKey)) return false;

        final GroupCacheKey groupCacheKey = (GroupCacheKey) o;

        if (!key.equals(groupCacheKey.key)) return false;

        return true;
    }

    public int hashCode() {
        return key.hashCode();
    }

    public String toString() {
        StringBuffer result = new StringBuffer(groups.size()*5);
        result.append(key);
        result.append(keyGroupSeparator);
        result.append(groupSeparator);
        Iterator<String> keyIter = groups.iterator();
        while (keyIter.hasNext()) {
            result.append((String) keyIter.next());
            if (keyIter.hasNext()) {
                result.append(groupSeparator);
            }
        }
        return result.toString();
    }

    public int compareTo(GroupCacheKey otherKey) {
        return toString().compareTo(otherKey.toString());
    }

    public static String getKeyGroupSeparator() {
        return keyGroupSeparator;
    }

    public static void setKeyGroupSeparator(String keyGroupSeparator) {
        GroupCacheKey.keyGroupSeparator = keyGroupSeparator;
    }

    public static String getGroupSeparator() {
        return groupSeparator;
    }

    public static void setGroupSeparator(String groupSeparator) {
        GroupCacheKey.groupSeparator = groupSeparator;
    }

    public Object getKey() {
        return key;
    }
}
