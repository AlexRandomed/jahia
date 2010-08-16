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

package org.jahia.services.cache.clusterservice.batch;

import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.CacheListener;
import org.jahia.services.cache.clusterservice.ClusterServiceCacheProvider;
import org.jahia.services.cache.clusterservice.ClusterCacheMessage;

import java.util.Collection;
import java.util.Set;

/**
 * User: Serge Huber
 * Date: 8 mai 2006
 * Time: 11:30:48
 * Copyright (C) Jahia Inc.
 */
public class BatchingClusterServiceCacheImpl implements CacheImplementation {

    protected final static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ClusterServiceCacheProvider.class);

    ClusterCacheMessageBatcher batcher;
    CacheImplementation underlyingCacheImplementation;

    public BatchingClusterServiceCacheImpl(String name, ClusterCacheMessageBatcher batcher, CacheImplementation underlyingCacheImplementation) {
        this.batcher = batcher;
        this.underlyingCacheImplementation = underlyingCacheImplementation;
        this.underlyingCacheImplementation.setName(name);
    }

    /**
     * Puts an entry in the cache, using optional groups
     *
     * @param key
     * @param groups the groups this entry should be part of, or null if it's part of no group.
     * @param value
     */
    public void put(Object key, String[] groups, Object value) {
        boolean newEntry = true;
        if (underlyingCacheImplementation.get(key) != null) {
            newEntry = false;
        }
        underlyingCacheImplementation.put(key, groups, value);
        if (!newEntry) {
            batcher.addMessageToBatch(new ClusterCacheMessage(getName(), ClusterCacheMessage.INVALIDATEENTRY_EVENT, key, null));
        }
    }

    public void flushAll(boolean propagate) {
    	underlyingCacheImplementation.flushAll(propagate);
        if (propagate) {
            batcher.addMessageToBatch(new ClusterCacheMessage(getName(), ClusterCacheMessage.FLUSH_EVENT, null, null));
        }
    }

    public void flushGroup(String groupName) {
    	underlyingCacheImplementation.flushGroup(groupName);
        batcher.addMessageToBatch(new ClusterCacheMessage(getName(), ClusterCacheMessage.FLUSHGROUP_EVENT, null, groupName));
    }

    public void remove(Object key) {
    	underlyingCacheImplementation.remove(key);
        batcher.addMessageToBatch(new ClusterCacheMessage(getName(), ClusterCacheMessage.REMOVE_EVENT, key, null));
    }

    protected void onFlush(ClusterCacheMessage clusterCacheMessage) {
        if (logger.isDebugEnabled()) {
            logger.debug(getName() + " cache event received, flushing local cache");
        }
        underlyingCacheImplementation.flushAll(false);
    }

    protected void onFlushGroup(ClusterCacheMessage clusterCacheMessage) {
        if (logger.isDebugEnabled()) {
            logger.debug(getName() + " cache event received, flushing local cache group " + clusterCacheMessage.getGroupName());
        }
        underlyingCacheImplementation.flushGroup(clusterCacheMessage.getGroupName());
    }

    protected void onInvalidateEntry(ClusterCacheMessage clusterCacheMessage) {
        if (logger.isDebugEnabled()) {
            logger.debug(getName() + " cache event received, invalidating value for key " + clusterCacheMessage.getEntryKey());
        }
        underlyingCacheImplementation.remove(clusterCacheMessage.getEntryKey());
    }

    protected void onRemove(ClusterCacheMessage clusterCacheMessage) {
        if (logger.isDebugEnabled()) {
            logger.debug(getName() + " cache event received, removing key " + clusterCacheMessage.getEntryKey());
        }
        underlyingCacheImplementation.remove(clusterCacheMessage.getEntryKey());
    }

	public void addListener(CacheListener listener) {
		underlyingCacheImplementation.addListener(listener);
	}

	public boolean containsKey(Object key) {
		return underlyingCacheImplementation.containsKey(key);
	}

	public Object get(Object key) {
		return underlyingCacheImplementation.get(key);
	}

	public long getCacheLimit() {
		return underlyingCacheImplementation.getCacheLimit();
	}

    public long getCacheGroupsLimit() {
        return underlyingCacheImplementation.getCacheGroupsLimit();
    }

    public void setCacheGroupsLimit(long groupsLimit) {
        underlyingCacheImplementation.setCacheGroupsLimit(groupsLimit);
    }

    public Set getGroupKeys(String groupName) {
        return underlyingCacheImplementation.getGroupKeys(groupName);
    }

    public String getName() {
		return underlyingCacheImplementation.getName();
	}

	public void removeListener(CacheListener listener) {
		underlyingCacheImplementation.removeListener(listener);
	}

	public void setCacheLimit(long limit) {
		underlyingCacheImplementation.setCacheLimit(limit);
	}

	public void setName(String name) {
		underlyingCacheImplementation.setName(name);
	}
	
    public boolean isEmpty() {
        return underlyingCacheImplementation.isEmpty();
    }   	

	public int size() {
		return underlyingCacheImplementation.size();
	}

    public long getGroupsSize() {
        return underlyingCacheImplementation.getGroupsSize();
    }

    public long getGroupsKeysTotal() {
        return underlyingCacheImplementation.getGroupsKeysTotal();
    }
    
    public Collection<Object> getKeys() {
        return underlyingCacheImplementation.getKeys();
    }

}
