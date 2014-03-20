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
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
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
package org.jahia.ajax.gwt.client.widget.usergroup;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Renders a button for opening user/group picker.
 * 
 * @author Sergiy Shyrkov
 */
public class UserGroupSelectButton extends InlineHTML {

    private static final Set<String> MODES = new HashSet<String>(3);

    static {
        MODES.add("both");
        MODES.add("users");
        MODES.add("groups");
    }

    public static native void add(String fieldId, String callback,
            String principalType, String principalKey, String principalName) /*-{
      var callbackResult = true;
      if (callback) {
          try {
              callbackResult = eval('$wnd.' + callback)(principalType, principalKey, principalName);
          } catch (e) {};
      }
      try {
          if (callbackResult && fieldId && $wnd.document.getElementById(fieldId)) {
              var target = $wnd.document.getElementById(fieldId);
              if (typeof target.options != 'undefined') {
                  var found = false;
                  for (var i=0; i < target.length; i++) {
                      if (target.options[i].value == principalKey) {
                          found = true;
                          break;
                      }
                  }
                  if (!found) {
                      target.options[target.length] = new Option(principalName, principalKey, true);
                  }
              } else {
                  target.value = principalName;
              }
          }
      } catch (e) { };
    }-*/;

    private static String getAttribute(Element elem, String attributeName,
            String defaultValue) {
        return elem.getAttribute(attributeName).length() > 0 ? elem
                .getAttribute(attributeName) : defaultValue;
    }

    private String callback;

    private String fieldId;

    private String mode;

    private boolean showSiteSelector;

    private boolean singleSelectionMode;

    /**
     * Initializes an instance of this class.
     * 
     * @param elem
     */
    public UserGroupSelectButton(Element elem) {
        super(getAttribute(elem, "label", ""));
        mode = getAttribute(elem, "mode", "both");
        mode = MODES.contains(mode) ? mode : "both";
        setStyleName(getAttribute(elem, "styleName",
                "usergroup-button usergroup-mode-" + mode));
        showSiteSelector = Boolean.valueOf(getAttribute(elem,
                "showSiteSelector", "false"));
        singleSelectionMode = Boolean.valueOf(getAttribute(elem,
                "singleSelectionMode", "false"));

        callback = getAttribute(elem, "onSelect", null);
        fieldId = getAttribute(elem, "fieldId", null);

        init();
    }

    private int getViewMode() {
        int viewMode = UserGroupSelect.VIEW_TABS;
        if ("users".equals(mode)) {
            viewMode = UserGroupSelect.VIEW_USERS;
        } else if ("groups".equals(mode)) {
            viewMode = UserGroupSelect.VIEW_GROUPS;
        }
        return viewMode;
    }

    private void init() {
        addClickListener(new ClickListener() {
            public void onClick(Widget sender) {

                new UserGroupSelect(new UserGroupAdder() {
                    public void addGroups(List<GWTJahiaGroup> groups) {
                        for (final GWTJahiaGroup group : groups) {
                            add(fieldId, callback, "g", group.getGroupKey(),
                                    group.getDisplay());
                        }
                    }

                    public void addUsers(List<GWTJahiaUser> users) {
                        for (final GWTJahiaUser user : users) {
                            add(fieldId, callback, "u", user.getUserKey(), user
                                    .getDisplay());
                        }
                    }
                }, getViewMode(), "currentSite", singleSelectionMode);
            }
        });
    }
}
