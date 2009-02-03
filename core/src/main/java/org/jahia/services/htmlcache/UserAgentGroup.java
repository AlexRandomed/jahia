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

 package org.jahia.services.htmlcache;

/**
 * Title:        Jahia
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 * @author Serge Huber
 * @version 1.0
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * The purpose of this class is to store all the information relative to a group
 * of user agents, as well as the regular expressions that are use to evaluate
 * if a new user agent should fit into this group.
 * @author Serge Huber
 */
public class UserAgentGroup {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (UserAgentGroup.class);

    private String name;
    private List regexpList = new ArrayList();
    private List regexpStrings = new ArrayList();
    private Set userAgentSet = new TreeSet();

    /**
     * This constructor is used notably when de-serializing objects from
     * persistant storage, in the case of this implementation most probably
     * from XML files.
     * @param name the name of the group
     * @param regexpStrings a List of Strings that contain the regular expressions
     * to be evaluated using the matchesInsertCriterias method. This allows for
     * quick evaluation of wether a newly encountered user agent should be
     * insert into this group. This is a list because the order of the entries
     * is crucial to correct evaluation of the criterias
     * @param userAgentSet a list of Strings containing user agent string to
     * initialize the group with.
     */
    public UserAgentGroup(String name, List regexpStrings, Set userAgentSet) {
        this.name = name;
        Iterator regexpIter = regexpStrings.iterator();
        while (regexpIter.hasNext()) {
            Object curRegExpObj = regexpIter.next();
            if (curRegExpObj instanceof String) {
                String curRegExpStr = (String) curRegExpObj;
                try {
                    RE curRE = new RE(curRegExpStr);
                    this.regexpList.add(curRE);
                    this.regexpStrings.add(curRegExpStr);
                } catch (RESyntaxException res) {
                    logger.error("Invalid regular expression syntax : " +
                                         curRegExpStr + ", ignoring it...");
                }
            }
        }
        Iterator userAgentIter = userAgentSet.iterator();
        while (userAgentIter.hasNext()) {
            Object userAgentObj = userAgentIter.next();
            if (userAgentObj instanceof String) {
                String userAgentStr = (String) userAgentObj;
                this.userAgentSet.add(userAgentStr);
            }
        }
    }

    /**
     * Tests the specified user agent against all the regular expressions
     * declared for this group.
     * @param newUserAgent a String containing the user agent to test.
     * @return true as soon as a regexp matched the specified user agent string.
     * The evaluation is immediately stopped as soon as the first regexp matches
     * the user agent string
     */
    public boolean matchesInsertCriterias(String newUserAgent) {
        Iterator regexpIter = regexpList.iterator();
        while (regexpIter.hasNext()) {
            RE curRE = (RE) regexpIter.next();
            if (curRE.match(newUserAgent)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the name of this user agent group
     * @return a String object containing the name of this user agent group.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns an enumation to be able to view all the elements of the regular
     * expression list.
     * @return an Iterator of String objects that contain the regular
     * expression. The order should normally be the same as at the time of
     * creation of the UserAgentGroup instance.
     * @todo FIXME : test the ordering...
     */
    public Iterator getRegExpStrings() {
        return regexpStrings.iterator();
    }

    /**
     * Returns an iterator on a set of user agent strings that are contained in
     * this group.
     * @return an Iterator object on a Set of String objects that contain the
     * user agent string inserted when creating this instance of the object.
     */
    public Iterator getUserAgentSetIterator() {
        return userAgentSet.iterator();
    }

    /**
     * Adds the specified user agent into the set of this group. Warning : there
     * is no verification that this user agent complies to the regular expressions
     * Use matchInsertCriterias first to know if you should insert this agent
     * in this group.
     * @param newUserAgent the new user agent String to insert in this group.
     * @return true if insertion was successful, false otherwise (which means
     * the user agent already exists in the set).
     */
    public boolean setUserAgent(String newUserAgent) {
        return userAgentSet.add(newUserAgent);
    }

}
