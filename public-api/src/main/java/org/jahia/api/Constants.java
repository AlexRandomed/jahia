/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.api;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 6 juil. 2007
 * Time: 18:54:16
 * To change this template use File | Settings | File Templates.
 */
public class Constants {
    public static final String JCR_NS = "http://www.jcp.org/jcr/1.0";
    public static final String NT_NS = "http://www.jcp.org/jcr/nt/1.0";
    public static final String MIX_NS = "http://www.jcp.org/jcr/mix/1.0";

    public static final String JCR_PREF = "jcr";
    public static final String NT_PREF = "nt";
    public static final String MIX_PREF = "mix";

    public static final String JCR_AUTOCREATED = "jcr:autoCreated";
    public static final String JCR_BASEVERSION = "jcr:baseVersion";
    public static final String JCR_CHILD = "jcr:child";
    public static final String JCR_CHILDNODEDEFINITION = "jcr:childNodeDefinition";
    public static final String JCR_CONTENT = "jcr:content";
    public static final String JCR_CREATED = "jcr:created";
    public static final String JCR_CREATEDBY = "jcr:createdBy";
    public static final String JCR_DATA = "jcr:data";
    public static final String JCR_DEFAULTPRIMARYTYPE = "jcr:defaultPrimaryType";
    public static final String JCR_DEFAULTVALUES = "jcr:defaultValues";
    public static final String JCR_ENCODING = "jcr:encoding";
    public static final String JCR_FROZENMIXINTYPES = "jcr:frozenMixinTypes";
    public static final String JCR_FROZENNODE = "jcr:frozenNode";
    public static final String JCR_FROZENPRIMARYTYPE = "jcr:frozenPrimaryType";
    public static final String JCR_FROZENUUID = "jcr:frozenUuid";
    public static final String JCR_HASORDERABLECHILDNODES = "jcr:hasOrderableChildNodes";
    public static final String JCR_ISCHECKEDOUT = "jcr:isCheckedOut";
    public static final String JCR_ISMIXIN = "jcr:isMixin";
    public static final String JCR_LANGUAGE = "jcr:language";
    public static final String JCR_LASTMODIFIED = "jcr:lastModified";
    public static final String JCR_LASTMODIFIEDBY = "jcr:lastModifiedBy";    
    public static final String JCR_LOCKISDEEP = "jcr:lockIsDeep";
    public static final String JCR_LOCKOWNER = "jcr:lockOwner";
    public static final String JCR_MANDATORY = "jcr:mandatory";
    public static final String JCR_MERGEFAILED = "jcr:mergeFailed";
    public static final String JCR_MIMETYPE = "jcr:mimeType";
    public static final String JCR_MIXINTYPES = "jcr:mixinTypes";
    public static final String JCR_MULTIPLE = "jcr:multiple";
    public static final String JCR_NAME = "jcr:name";
    public static final String JCR_NODETYPENAME = "jcr:nodeTypeName";
    public static final String JCR_ONPARENTVERSION = "jcr:onParentVersion";
    public static final String JCR_PREDECESSORS = "jcr:predecessors";
    public static final String JCR_PRIMARYITEMNAME = "jcr:primaryItemName";
    public static final String JCR_PRIMARYTYPE = "jcr:primaryType";
    public static final String JCR_PROPERTYDEFINITION = "jcr:propertyDefinition";
    public static final String JCR_PROTECTED = "jcr:protected";
    public static final String JCR_REQUIREDPRIMARYTYPES = "jcr:requiredPrimaryTypes";
    public static final String JCR_REQUIREDTYPE = "jcr:requiredType";
    public static final String JCR_ROOTVERSION = "jcr:rootVersion";
    public static final String JCR_SAMENAMESIBLINGS = "jcr:sameNameSiblings";
    public static final String JCR_STATEMENT = "jcr:statement";
    public static final String JCR_SUCCESSORS = "jcr:successors";
    public static final String JCR_SUPERTYPES = "jcr:supertypes";
    public static final String JCR_SYSTEM = "jcr:system";
    public static final String JCR_TITLE = "jcr:title";    
    public static final String JCR_UUID = "jcr:uuid";
    public static final String JCR_VALUECONSTRAINTS = "jcr:valueConstraints";
    public static final String JCR_VERSIONHISTORY = "jcr:versionHistory";
    public static final String JCR_VERSIONLABELS = "jcr:versionLabels";
    public static final String JCR_VERSIONSTORAGE = "jcr:versionStorage";
    public static final String JCR_VERSIONABLEUUID = "jcr:versionableUuid";
    public static final String JCR_PATH = "jcr:path";
    public static final String JCR_SCORE = "jcr:score";
    public static final String MIX_LANGUAGE = "mix:language";    
    public static final String MIX_LOCKABLE = "mix:lockable";
    public static final String MIX_MIMETYPE = "mix:mimeType";
    public static final String MIX_REFERENCEABLE = "mix:referenceable";
    public static final String MIX_VERSIONABLE = "mix:versionable";
    public static final String MIX_CREATED = "mix:created";
    public static final String MIX_CREATED_BY = "mix:createdBy";
    public static final String MIX_LAST_MODIFIED = "mix:lastModified";
    public static final String MIX_TITLE = "mix:title";
    public static final String NT_BASE = "nt:base";
    public static final String NT_CHILDNODEDEFINITION = "nt:childNodeDefinition";
    public static final String NT_FILE = "nt:file";
    public static final String NT_FOLDER = "nt:folder";
    public static final String NT_FROZENNODE = "nt:frozenNode";
    public static final String NT_HIERARCHYNODE = "nt:hierarchyNode";
    public static final String NT_LINKEDFILE = "nt:linkedFile";
    public static final String NT_NODETYPE = "nt:nodeType";
    public static final String NT_PROPERTYDEFINITION = "nt:propertyDefinition";
    public static final String NT_QUERY = "nt:query";
    public static final String NT_RESOURCE = "nt:resource";
    public static final String NT_UNSTRUCTURED = "nt:unstructured";
    public static final String NT_VERSION = "nt:version";
    public static final String NT_VERSIONHISTORY = "nt:versionHistory";
    public static final String NT_VERSIONLABELS = "nt:versionLabels";
    public static final String NT_VERSIONEDCHILD = "nt:versionedChild";

    public static final String JAHIA_NS = "http://www.jahia.org/jahia/1.0";
    public static final String JAHIANT_NS = "http://www.jahia.org/jahia/nt/1.0";
    public static final String JAHIAMIX_NS = "http://www.jahia.org/jahia/mix/1.0";

    public static final String JAHIA_PREF = "j";
    public static final String JAHIANT_PREF = "jnt";
    public static final String JAHIAMIX_PREF = "jmix";

    public static final String JAHIAMIX_CATEGORIZED = "jmix:categorized";
    public static final String JAHIAMIX_LASTPUBLISHED = "jmix:lastPublished";
    public static final String JAHIAMIX_HIERARCHYNODE = "jmix:hierarchyNode";
    public static final String JAHIAMIX_SHAREABLE = "jmix:shareable";

    public final static String JAHIAMIX_IMAGE = "jmix:image" ;

    public static final String JAHIAMIX_EXTENSION = "jmix:extension";

    public static final String JAHIAMIX_HIERARCHY = "jmix:hierarchyNode";
    
    public static final String JAHIAMIX_TAGGED = "jmix:tagged";
    
    public static final String JAHIANT_FOLDER = "jnt:folder";
    public static final String JAHIANT_FILE = "jnt:file";
    public static final String JAHIANT_RESOURCE = "jnt:resource";
    public static final String JAHIANT_SYMLINK = "jnt:symLink";
    public static final String JAHIANT_SYSTEMFOLDER = "jnt:systemfolder";
    public static final String JAHIANT_VIRTUALSITE = "jnt:virtualsite";
    public static final String JAHIANT_SYSTEM = "jnt:system";
    public static final String JAHIANT_AXISFOLDER = "jnt:axisFolder";
    public static final String JAHIANT_USER = "jnt:user";
    public static final String JAHIANT_CATEGORY = "jnt:category";
    public static final String JAHIANT_TRANSLATION = "jnt:translation";

    public static final String JAHIANT_LEGACY_ROOT = "jnt:jahiaLegacyRoot";
    public static final String JAHIANT_JAHIACONTENT = "jnt:jahiacontent";
    public static final String JAHIANT_PAGE = "jnt:page";
    public static final String JAHIANT_CONTAINER = "jnt:container";
    public static final String JAHIANT_CONTENTLIST = "jnt:contentList";
    public static final String JAHIANT_CONTAINERLIST = "jnt:containerList";
    public static final String JAHIANT_PAGE_LINK = "jmix:link";
    public static final String JAHIANT_INTERNAL_PAGE_LINK = "jnt:nodeLink";
    public static final String JAHIANT_EXTERNAL_PAGE_LINK = "jnt:externalLink";
    public static final String JAHIANT_DIRECT_PAGE_LINK = "jnt:nodeLink";
    public static final String JAHIANT_PORTLET = "jnt:portlet";
    public static final String JAHIANT_LAYOUT = "jnt:layout";
    public static final String JAHIANT_LAYOUTITEM = "jnt:layoutItem";
    public static final String JAHIANT_MOUNTPOINT = "jnt:mountPoint";
    public static final String JAHIANT_VFSMOUNTPOINT = "jnt:vfsMountPoint";
    public static final String JAHIANT_TAG = "jnt:tag";
    public static final String JAHIANT_TASK = "jnt:task";
    public static final String JAHIANT_TASKS = "jnt:tasks";
    
    public static final String LASTPUBLISHED = "j:lastPublished";
    public static final String LASTPUBLISHEDBY = "j:lastPublishedBy";    
    public static final String CONTENT = "content";
    public static final String SYSTEM = "j:system";
    public static final String FILTERS = "j:filters";
    public static final String AUTOMATIONS = "j:automations";
    public static final String EXTRACTED_TEXT = "j:extractedText";
    public static final String EXTRACTION_DATE = "j:lastExtractionDate";
    public static final String ORIGINAL_UUID = "j:originalUuid";
    public static final String NODENAME = "j:nodename";
    public static final String URL = "j:url";
    public static final String NODE = "j:node";    
    public static final String ALT = "j:alt";
    public static final String TARGET = "j:target";

    public static final String APPLY_ACL = "j:applyAcl";
    public static final String THUMBNAIL = "j:thumbnail";
    public static final String FULLPATH = "j:fullpath";
    public static final String TAGS = "j:tags";

    public static final String JAHIANT_VIRTUALSITES_FOLDER = "jnt:virtualsitesFolder";
    public static final String JAHIANT_GROUP = "jnt:group";
    public static final String JAHIANT_MEMBERS = "jnt:members";
    public static final String JAHIANT_MEMBER = "jnt:member";

    public static final String JCR_READ_RIGHTS = "jcr:read";
    public static final String JCR_WRITE_RIGHTS = "jcr:write";
    public static final String JCR_MODIFYACCESSCONTROL_RIGHTS = "jcr:modifyAccessControl";

    public static final String JCR_READ_RIGHTS_LIVE = "jcr:read_live";
    public static final String JCR_WRITE_RIGHTS_LIVE = "jcr:write_live";

    public static final String GRANT = "GRANT";
    public static final String DENY = "DENY";

    public static final String LIVE_WORKSPACE = "live";
    public static final String EDIT_WORKSPACE = "default";
}
