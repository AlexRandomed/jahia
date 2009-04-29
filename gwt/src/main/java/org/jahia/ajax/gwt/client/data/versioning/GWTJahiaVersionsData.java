/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data.versioning;

import org.jahia.ajax.gwt.client.data.GWTJahiaVersion;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 29 ao�t 2008
 * Time: 11:58:02
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaVersionsData implements Serializable {

    private String[] versionRowDataHeadLabels;

    private List<GWTJahiaVersion> versions;

    public GWTJahiaVersionsData() {
    }

    public GWTJahiaVersionsData(String[] versionRowDataHeadLabels, List<GWTJahiaVersion> versions) {
        this.versionRowDataHeadLabels = versionRowDataHeadLabels;
        this.versions = versions;
    }

    public String[] getVersionRowDataHeadLabels() {
        return versionRowDataHeadLabels;
    }

    public void setVersionRowDataHeadLabels(String[] versionRowDataHeadLabels) {
        this.versionRowDataHeadLabels = versionRowDataHeadLabels;
    }

    public List<GWTJahiaVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<GWTJahiaVersion> versions) {
        this.versions = versions;
    }
}
