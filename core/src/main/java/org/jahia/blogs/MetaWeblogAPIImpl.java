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

package org.jahia.blogs;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.jahia.blogs.actions.AbstractAction;
import org.jahia.blogs.actions.DeletePostAction;
import org.jahia.blogs.actions.EditPostAction;
import org.jahia.blogs.actions.GetCategoriesAction;
import org.jahia.blogs.actions.GetPostAction;
import org.jahia.blogs.actions.GetRecentPostsAction;
import org.jahia.blogs.actions.NewMediaObjectAction;
import org.jahia.blogs.actions.NewPostAction;
import org.jahia.blogs.api.MetaWeblogAPI;
import org.jahia.blogs.api.XMLRPCConstants;

/**
 * Implementation of the MetaWeblogAPI.
 *
 * @author Xavier Lawrence
 */
public class MetaWeblogAPIImpl extends BloggerAPIImpl implements MetaWeblogAPI,
        XMLRPCConstants {
    
    // log4j logger
    static Logger log = Logger.getLogger(MetaWeblogAPIImpl.class);
    
    /**
     */
    public boolean editPost(final String postID, final String userName,
            final String password, final Map struct,
            final boolean publish) throws XmlRpcException {
        log.debug("metaWebLog.editPost: " +postID+ ", " +userName+ ", " +
                password+ ", " +struct+ ", "+publish);
        
        AbstractAction action = new EditPostAction(postID, userName,
                password, struct, publish);
        
        try {
            return ((Boolean)action.execute()).booleanValue();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e.getMessage().indexOf("Login") != -1) {
                throw new XmlRpcException(AUTHORIZATION_EXCEPTION,
                        AUTHORIZATION_EXCEPTION_MSG);
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, e.getMessage());
            }
        } finally {
            action = null;
        }
    }
    
    /**
     */
    public Map getPost(final String postID, final String userName,
            final String password) throws XmlRpcException {
        log.debug("metaWebLog.getPost: " +postID+ ", " +userName+ ", " +
                password);
        
        AbstractAction action = new GetPostAction(postID, userName,
                password);
        
        try {
            return (Map)action.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e.getMessage().indexOf("Login") != -1) {
                throw new XmlRpcException(AUTHORIZATION_EXCEPTION,
                        AUTHORIZATION_EXCEPTION_MSG);
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, e.getMessage());
            }
        } finally {
            action = null;
        }
    }
    
    /**
     */
    public List getRecentPosts(final String blogID, final String userName,
            final String password, final int numposts) throws XmlRpcException {
        log.debug("metaWebLog.getRecentPosts: " +blogID+ ", " +userName+ ", " +
                password+ ", " +numposts);
        
        AbstractAction action = new GetRecentPostsAction(blogID, userName,
                password, numposts);
        
        try {
            return (List)action.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e.getMessage().indexOf("Login") != -1) {
                throw new XmlRpcException(AUTHORIZATION_EXCEPTION,
                        AUTHORIZATION_EXCEPTION_MSG);
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, e.getMessage());
            }
        } finally {
            action = null;
        }
    }
    
    /**
     */
    public String newPost(final String blogID, final String userName,
            final String password, final Map struct,
            final boolean publish) throws XmlRpcException {
        log.debug("metaWebLog.newPost: " +blogID+ ", " +userName+ ", " +
                password+ ", " +struct+ ", "+publish);
        
        AbstractAction action = new NewPostAction(blogID, userName,
                password, struct, publish);
        
        try {
            return (String)action.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e.getMessage().indexOf("Login") != -1) {
                throw new XmlRpcException(AUTHORIZATION_EXCEPTION,
                        AUTHORIZATION_EXCEPTION_MSG);
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, e.getMessage());
            }
        } finally {
            action = null;
        }
    }
    
    /**
     * Added to support some of the cell phone implementation of the MetaWeblog
     * implementation client side (Sony Ericsson P900). Has the same behavior as 
     * its twin method.
     */
    public String newPost(final String blogID, final String userName,
            final String password, final Map struct,
            final boolean publish, boolean tralala) throws XmlRpcException {
        log.debug("metaWebLog.newPost: " +blogID+ ", " +userName+ ", " +
                password+ ", " +struct+ ", "+publish+ ", " +tralala);
        
        AbstractAction action = new NewPostAction(blogID, userName,
                password, struct, publish);
        
        try {
            return (String)action.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e.getMessage().indexOf("Login") != -1) {
                throw new XmlRpcException(AUTHORIZATION_EXCEPTION,
                        AUTHORIZATION_EXCEPTION_MSG);
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, e.getMessage());
            }
        } finally {
            action = null;
        }
    }
    
    /**
     */
    public boolean deletePost(String postID, String userName, String password,
            boolean publish) throws XmlRpcException {
        log.debug("metaWebLog.deletePost: " +postID+ ", " +userName+ ", " +
                password+ ", "+publish);
        
        AbstractAction action = new DeletePostAction(postID, userName,
                password, publish);
        
        try {
            return ((Boolean)action.execute()).booleanValue();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e.getMessage().indexOf("Login") != -1) {
                throw new XmlRpcException(AUTHORIZATION_EXCEPTION,
                        AUTHORIZATION_EXCEPTION_MSG);
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, e.getMessage());
            }
        } finally {
            action = null;
        }
    }
    
    /**
     */
    public Map getCategories(final String blogID, final String userName,
            final String password) throws XmlRpcException {
        log.debug("metaWebLog.getCategories: " +blogID+ ", " +userName+ ", " +
                password);
        
        AbstractAction action = new GetCategoriesAction(blogID, userName,
                password, true);
        
        try {
            return (Map)action.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e.getMessage().indexOf("Login") != -1) {
                throw new XmlRpcException(AUTHORIZATION_EXCEPTION,
                        AUTHORIZATION_EXCEPTION_MSG);
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, e.getMessage());
            }
        } finally {
            action = null;
        }
    }
    
    /**
     */
    public Map newMediaObject(final String blogID, final String userName,
            final String password, final Map struct) throws XmlRpcException {
        log.debug("metaWebLog.newMediaObject: " +blogID+ ", " +userName+ ", " +
                password+ ", " +struct);
        
        AbstractAction action = new NewMediaObjectAction(blogID, userName,
                password, struct);
        
        try {
            return (Map)action.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e.getMessage().indexOf("Login") != -1) {
                throw new XmlRpcException(AUTHORIZATION_EXCEPTION,
                        AUTHORIZATION_EXCEPTION_MSG);
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, e.getMessage());
            }
        } finally {
            action = null;
        }
    }
}
