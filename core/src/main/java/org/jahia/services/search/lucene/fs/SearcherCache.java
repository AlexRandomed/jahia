/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jahia.services.search.lucene.fs;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * Primary API for dealing with Solr's internal caches.
 * 
 * @version $Id: SolrCache.java 555343 2007-07-11 17:46:25Z hossman $
 */
public interface SearcherCache {
  public final static Logger log = Logger.getLogger(SearcherCache.class.getName());


  /**
   * The initialization routine.  Instance specific arguments are passed in
   * the <code>args</code> map.
   * <p>
   * The persistence object will exist across different lifetimes of similar caches.
   * For example, all filter caches will share the same persistence object, sometimes
   * at the same time (it must be threadsafe).  If null is passed, then the cache
   * implementation should create and return a new persistence object.  If not null,
   * the passed in object should be returned again.
   * <p>
   * Since it will exist across the lifetime of many caches, care should be taken to
   * not reference any particular cache instance and prevent it from being
   * garbage collected (no using inner classes unless they are static).
   * <p>
   * The persistence object is designed to be used as a way for statistics
   * to accumulate across all instances of the same type of cache, however the
   * object may be of any type desired by the cache implementation.
   * <p>
   * The {@link CacheRegenerator} is what the cache uses during auto-warming to
   * renenerate an item in the new cache from an entry in the old cache.
   *
   */
  public Object init(Properties args, Object persistence, CacheRegenerator regenerator);
  // I don't think we need a factory for faster creation given that these
  // will be associated with slow-to-create SolrIndexSearchers.
  // change to NamedList when other plugins do?

  /**
   * Name the Cache can be referenced with by SolrRequestHandlers.
   *
   * This method must return the identifier that the Cache instance 
   * expects SolrRequestHandlers to use when requesting access to it 
   * from the SolrIndexSearcher.  It is <strong>strongly</strong> 
   * recommended that this method return the value of the "name" 
   * parameter from the init args.
   *
   * :TODO: verify this.
   */
  public String name();


  // Should SearcherCache just extend the java.util.Map interface?
  // Following the conventions of the java.util.Map interface in any case.

  /** :TODO: copy from Map */
  public int size();

  /** :TODO: copy from Map */
  public Object put(Object key, Object value);

  /** :TODO: copy from Map */
  public Object get(Object key);

  /** :TODO: copy from Map */
  public void clear();

  /** 
   * Iterator of possible States for cache instances.
   * :TODO: only state that seems to ever be set is LIVE ?
  */
  public enum State { 
    /** :TODO */
    CREATED, 
    /** :TODO */
    STATICWARMING, 
    /** :TODO */
    AUTOWARMING, 
    /** :TODO */
    LIVE 
  }

  /**
   * Set different cache states.
   * The state a cache is in can have an effect on how statistics are kept.
   * The cache user (SolrIndexSearcher) will take care of switching
   * cache states.
   */
  public void setState(State state);

  /**
   * Returns the last State set on this instance
   *
   * @see #setState
   */
  public State getState();


  /**
   * Warm this cache associated with <code>searcher</code> using the <code>old</code>
   * cache object.  <code>this</code> and <code>old</code> will have the same concrete type.
   */
  void warm(JahiaIndexSearcher searcher, SearcherCache old) throws IOException;
  // Q: an alternative to passing the searcher here would be to pass it in
  // init and have the cache implementation save it.


  /** Frees any non-memory resources */
  public void close();

}
