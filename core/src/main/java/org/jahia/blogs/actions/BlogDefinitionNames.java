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

package org.jahia.blogs.actions;

import org.jahia.params.ProcessingContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

import java.util.Properties;

import org.apache.log4j.Logger;

/**
 *  
 <content:declareContainerList name="entries" title="List of entries">
 <content:declareContainer>
    <content:declareField name="title" title="Title" type="SmallText"
        titleKey="blog.title" bundleKey="<%=resBundleID%>"/>
    <content:declareField name="body" title="Body" type="BigText"
        titleKey="blog.body" bundleKey="<%=resBundleID%>"/>
    <content:declareField name="attachement" title="Attachment associated" type="File"
        titleKey="blog.attachement" bundleKey="<%=resBundleID%>"/>
    <content:declareField name="date" title="Date" type="Date" value="<jahia_calendar[dd.mm.yyyy / HH:MM]>"
        titleKey="blog.date" bundleKey="<%=resBundleID%>"/>
    <content:declareField name="author" title="Author" type="SharedSmallText"
        value="<%=jData.params().getUser().getUsername()%>"
        titleKey="blog.author" bundleKey="<%=resBundleID%>"/>
    <content:declareField name="excerpt"
            title="The Excerpt of this post" type="BigText" />
    <content:declareField name="keyWords"
            title="Comma seperated Keywords for this post" type="SmallText" />

    <content:declareContainerList name="comments" title="List of comments">
    <content:declareContainer>
        <content:declareField name="author" title="Name of the author" type="SharedSmallText"
            titleKey="blog.commentAuthor" bundleKey="<%=resBundleID%>"
            value="<%=jData.params().getUser().getUsername()%>"/>
        <content:declareField name="title" title="Title of this comment" type="SmallText"
            titleKey="blog.commentTitle" bundleKey="<%=resBundleID%>"/>
        <content:declareField name="body" title="Body of this comment" type="BigText"
            titleKey="blog.commentBody" bundleKey="<%=resBundleID%>"/>
        <content:declareField name="date" title="Date" type="Date"
            value="<jahia_calendar[dd.mm.yyyy / HH:MM]>"
            titleKey="blog.commentDate" bundleKey="<%=resBundleID%>"/>
    </content:declareContainer>
    </content:declareContainerList>

    <content:declareContainerList name="trackBackPingURLs"
        title="List of TrackBacks Ping URLs">
    <content:declareContainer>
        <content:declareField name="pingURL"
            title="URL to send the ping to" type="SharedSmallText" />
    </content:declareContainer>
    </content:declareContainerList>

    <content:declareContainerList name="trackbacks"
        title="List of MovableType TrackBacks Ping data">
    <content:declareContainer>
        <content:declareField name="trackBackTitle"
            title="Title of the blog entry sent in the ping" type="SharedSmallText"/>
        <content:declareField name="trackBackExcerpt"
            title="Excerpt of the blog entry for this trackback." type="BigText"/>
        <content:declareField name="trackBackUrl"
            title="Url (permalink) of the blog entry for this trackback" type="SharedSmallText"/>
        <content:declareField name="trackBackBlog_name"
            title="Name of the blog for this trackback" type="SharedSmallText"/>
        <content:declareField name="trackBackPing_IP"
            title="The IP address of the host that sent the ping" type="SharedSmallText"/>
    </content:declareContainer>
    </content:declareContainerList>

  </content:declareContainer>
  </content:declareContainerList>

 * Simple class holding containerlist & field definition names. 
 *
 * @author Xavier Lawrence
 */
public class BlogDefinitionNames {
    
    // log4j logger
    static Logger log = Logger.getLogger(BlogDefinitionNames.class);
    
    public static final String RELATIVE_PATH = "/WEB-INF/etc/config/blogs.properties";       
    
    public static final String BLOG_POSTS_LIST_NAME = "postContainerList";
    public static final String POST_TITLE = "postTitle";
    public static final String POST_BODY = "postBody";
    public static final String POST_DATE = "postDate";
    public static final String POST_AUTHOR = "postAuthor";
    public static final String POST_EXCERPT = "postExcerpt";
    public static final String POST_KEYWORDS= "postKeyWords";

    public static final String BLOG_TB_PING_LIST = "tbPings";
    public static final String TB_PING_URL = "tbPingURL";
    
    public static final String BLOG_TB_LIST = "postTB_Pings";
    public static final String TB_URL = "trackBackUrl";
    public static final String TB_TITLE = "trackBackTitle";
    public static final String TB_BLOG_NAME = "trackBackBlogName";
    public static final String TB_EXCERPT = "trackBackExcerpt";
    public static final String TB_PING_IP = "pingIP";
    
    private Properties names;
    private ProcessingContext jParams;
    
    /** Creates a new instance of BlogDefinitionNames */
    public BlogDefinitionNames(ProcessingContext jParams) {
        try {
            this.jParams = jParams;
            names = new Properties();
            load();
            
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * Loads the definition names and values
     */
    private void load() throws IOException {
        
        FileInputStream fis = new FileInputStream(getFile());
        names.load(fis);
        fis.close();      
                
//        names.setProperty(BLOG_POSTS_LIST_NAME, "entries");
//        names.setProperty(BLOG_TB_LIST, "trackbacks");
//        names.setProperty(BLOG_TB_PING_LIST, "trackBackPingURLs");
//        
//        names.setProperty(POST_TITLE, "title");
//        names.setProperty(POST_BODY, "body");
//        names.setProperty(POST_DATE, "date");
//        names.setProperty(POST_AUTHOR, "author");
//        names.setProperty(POST_EXCERPT, "excerpt");
//        names.setProperty(POST_KEYWORDS, "keyWords");
//        
//        names.setProperty(TB_PING_URL, "pingURL");
//        
//        names.setProperty(TB_URL, "trackBackUrl");
//        names.setProperty(TB_TITLE, "trackBackTitle");
//        names.setProperty(TB_BLOG_NAME, "trackBackBlog_name");
//        names.setProperty(TB_EXCERPT, "trackBackExcerpt");
//        names.setProperty(TB_PING_IP, "trackBackPing_IP");
    }
    
    /**
     * Returns the value of a given definition name
     */
    public String getValue(String name) {
        return names.getProperty(name);
    }
    
    /**
     * returns the properties file
     */
    private File getFile() throws IOException {
        return new File(jParams.settings().getPathResolver().
                resolvePath(RELATIVE_PATH));
    }
}
