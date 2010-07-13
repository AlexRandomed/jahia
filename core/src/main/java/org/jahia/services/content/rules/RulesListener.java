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
package org.jahia.services.content.rules;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.StatelessSession;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.compiler.PackageBuilderErrors;
import org.drools.rule.Package;
import org.drools.rule.builder.dialect.java.JavaDialectConfiguration;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.settings.SettingsBean;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.io.*;
import java.util.*;

/**
 * Jahia rules-based event listener.
 * User: toto
 * Date: 6 juil. 2007
 * Time: 18:03:47
 */
public class RulesListener extends DefaultEventListener {
    private static Logger logger = Logger.getLogger(RulesListener.class);

    private static List<RulesListener> instances = new ArrayList<RulesListener>();

    private Timer rulesTimer = new Timer("rules-timer", true);

    private RuleBase ruleBase;
    private long lastRead = 0;

    private static final int UPDATE_DELAY_FOR_LOCKED_NODE = 2000;
    private Set<String> ruleFiles;
    private String serverId;
    
    private ThreadLocal<Boolean> inRules = new ThreadLocal<Boolean>();
    private PackageBuilder builder;

    private List<File> dslFiles = new LinkedList<File>();
    private Map<String, Object> globalObjects = new LinkedHashMap<String, Object>();

    public RulesListener() {
        instances.add(this);
    }

    public static RulesListener getInstance(String workspace) {
        for (RulesListener instance : instances) {
            if (instance.workspace.equals(workspace)) {
                return instance;
            }
        }
        return null;
    }

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED;
    }

    public int getOperationTypes() {
        return JCRObservationManager.SESSION_SAVE;
    }

    public String getPath() {
        return "/";
    }

    public String[] getNodeTypes() {
        return null;
    }

    public Set<String> getRuleFiles() {
        return ruleFiles;
    }

    private StatelessSession getStatelessSession(Map<String, Object> globals) {
        StatelessSession session = ruleBase.newStatelessSession();
        for (Map.Entry<String, Object> entry : globals.entrySet()) {
            session.setGlobal(entry.getKey(), entry.getValue());
        }
        return session;
    }

    public void executeRules(Object fact, Map<String, Object> globals) {
        getStatelessSession(globals).execute(fact);
    }

    public void executeRules(Object[] facts, Map<String, Object> globals) {
        getStatelessSession(globals).execute(facts);
    }

    public void executeRules(Collection<?> facts, Map<String, Object> globals) {
        getStatelessSession(globals).execute(facts);
    }

    public void setRuleFiles(Set<String> ruleFiles) {
        this.ruleFiles = ruleFiles;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public void start() {
        initRules();
    }

    private void initRules() {
        try {
            RuleBaseConfiguration conf = new RuleBaseConfiguration();
            //conf.setAssertBehaviour( AssertBehaviour.IDENTITY );
            //conf.setRemoveIdentities( true );
            ruleBase = RuleBaseFactory.newRuleBase(conf);
            Properties properties = new Properties();
            properties.setProperty("drools.dialect.java.compiler", "JANINO");
            PackageBuilderConfiguration cfg = new PackageBuilderConfiguration(properties);
            JavaDialectConfiguration javaConf = (JavaDialectConfiguration) cfg.getDialectConfiguration("java");
            javaConf.setCompiler(JavaDialectConfiguration.JANINO);

            builder = new PackageBuilder(cfg);
            for (String s : ruleFiles) {
                InputStreamReader drl = new InputStreamReader(new FileInputStream(SettingsBean.getInstance().getJahiaEtcDiskPath() + s));
                dslFiles.add(new File(SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/rules/rules.dsl"));
                builder.addPackageFromDrl(drl, new StringReader(getDslFiles()));
            }

            //            builder.addRuleFlow( new InputStreamReader( getClass().getResourceAsStream( "ruleflow.rfm" ) ) );

            PackageBuilderErrors errors = builder.getErrors();

            if (errors.getErrors().length == 0) {
                Package pkg = builder.getPackage();
                ruleBase.addPackage(pkg);
            } else {
                logger.error("---------------------------------------------------------------------------------");
                logger.error("Errors when compiling rules : " + errors.toString());
                logger.error("---------------------------------------------------------------------------------");
            }
            lastRead = System.currentTimeMillis();
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getDslFiles() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (File dslFile : dslFiles) {
            stringBuilder.append(FileUtils.readFileToString(dslFile, "UTF-8")).append("\n");
        }
        return stringBuilder.toString();
    }

    public void addRules(File dsrlFile) {
        InputStreamReader drl = null;
        try {
            Properties properties = new Properties();
            properties.setProperty("drools.dialect.java.compiler", "JANINO");
            PackageBuilderConfiguration cfg = new PackageBuilderConfiguration(properties);
            JavaDialectConfiguration javaConf = (JavaDialectConfiguration) cfg.getDialectConfiguration("java");
            javaConf.setCompiler(JavaDialectConfiguration.JANINO);

            PackageBuilder packageBuilder = new PackageBuilder(cfg);

            drl = new InputStreamReader(new FileInputStream(dsrlFile));

            packageBuilder.addPackageFromDrl(drl,new StringReader(getDslFiles()));

            PackageBuilderErrors errors = packageBuilder.getErrors();

            if (errors.getErrors().length == 0) {
                Package pkg = packageBuilder.getPackage();
                if (ruleBase.getPackage(pkg.getName()) != null) {
                    ruleBase.removePackage(pkg.getName());
                }
                ruleBase.addPackage(pkg);
                logger.info("Rules for " + pkg.getName() + " updated.");
            } else {
                logger.error("---------------------------------------------------------------------------------");
                logger.error("Errors when compiling rules in " + dsrlFile + " : " + errors.toString());
                logger.error("---------------------------------------------------------------------------------");
            }
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(drl);
        }
    }

    private long lastModified() {
        long last = 0;
        for (String s : ruleFiles) {
            last = Math.max(last, new File(SettingsBean.getInstance().getJahiaEtcDiskPath() + s).lastModified());
        }
        return last;
    }


    public void onEvent(EventIterator eventIterator) {
        if (((JCREventIterator)eventIterator).getOperationType() != JCRObservationManager.SESSION_SAVE) {
            return;
        }

        final JCRSessionWrapper session = ((JCREventIterator) eventIterator).getSession();
        final String userId = session.getUser() != null ? session.getUser().getName():"";
        final Locale locale = session.getLocale();

        final Map<String, AddedNodeFact> eventsMap = new HashMap<String, AddedNodeFact>();

        if (Boolean.TRUE.equals(inRules.get())) {
            System.out.println(" inrules event, skip");
            return;
        }

        if (ruleBase == null || SettingsBean.getInstance().isDevelopmentMode() && lastModified() > lastRead) {
            initRules();
            if (ruleBase == null) {
                return;
            }
        }

        final List<Object> list = new ArrayList<Object>();

        try {
            final List<Event> events = new ArrayList<Event>();
            while (eventIterator.hasNext()) {
                Event event = eventIterator.nextEvent();
                events.add(event);
            }
            JCRTemplate.getInstance().doExecuteWithSystemSession(
                    userId, workspace, locale, new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper s) throws RepositoryException {
                            Iterator<Event> it = events.iterator();

                            while (it.hasNext()) {
                                Event event = it.next();
                                if (isExternal(event)) {
                                    continue;
                                }
                                try {
                                    if (!event.getPath().startsWith("/jcr:system/")) {
                                        if (event.getType() == Event.NODE_ADDED) {
                                            JCRNodeWrapper n = s.getNode(event.getPath());
                                            if (n.isNodeType("jmix:observable")) {
                                                final String identifier = n.getIdentifier();
                                                AddedNodeFact rn = eventsMap.get(identifier);
                                                if (rn == null) {
                                                    rn = new AddedNodeFact(n);
                                                    eventsMap.put(identifier, rn);
                                                }
                                                list.add(rn);
                                            }
                                        } else if (event.getType() == Event.PROPERTY_ADDED || event.getType() == Event.PROPERTY_CHANGED) {
                                            String path = event.getPath();
                                            String propertyName = path.substring(path.lastIndexOf('/') + 1);
                                            if (!propertiesToIgnore.contains(propertyName)) {
                                                JCRPropertyWrapper p = (JCRPropertyWrapper) s.getItem(path);

                                                JCRNodeWrapper parent = p.getParent();
//                                    if (parent.isNodeType("jnt:translation")) {
//                                        parent = parent.getParent();
//                                    }
                                                if (parent.isNodeType(Constants.NT_RESOURCE) || parent.isNodeType("jmix:observable")) {
                                                    AddedNodeFact rn;
                                                    if (parent.isNodeType(Constants.MIX_REFERENCEABLE)) {
                                                        final String identifier = parent.getIdentifier();
                                                        rn = eventsMap.get(identifier);
                                                        if (rn == null) {
                                                            rn = new AddedNodeFact(parent);
                                                            eventsMap.put(identifier, rn);
                                                        }
                                                    } else {
                                                        rn = new AddedNodeFact(parent);
                                                    }
                                                    list.add(new ChangedPropertyFact(rn, p));
                                                }
                                            }
                                        } else if (event.getType() == Event.NODE_REMOVED) {
                                            String parentPath = null;
                                            try {
                                                parentPath = StringUtils.substringBeforeLast(event.getPath(), "/");
                                                JCRNodeWrapper parent = s.getNode(parentPath);
                                                final String identifier = parent.getIdentifier();
                                                AddedNodeFact w = eventsMap.get(identifier);
                                                if (w == null) {
                                                    w = new AddedNodeFact(parent);
                                                    eventsMap.put(identifier, w);
                                                }

                                                final DeletedNodeFact e = new DeletedNodeFact(w, event.getPath());
                                                e.setIdentifier(event.getIdentifier());
                                                e.setSession(s);
                                                list.add(e);
                                            } catch (PathNotFoundException e) {
                                            }
                                        } else if (event.getType() == Event.PROPERTY_REMOVED) {
                                            String path = event.getPath();
                                            int index = path.lastIndexOf('/');
                                            String nodePath = path.substring(0, index);
                                            String propertyName = path.substring(index + 1);
                                            if (!propertiesToIgnore.contains(propertyName)) {
                                                try {
                                                    JCRNodeWrapper n = s.getNode(nodePath);
                                                    String key = n.isNodeType(Constants.MIX_REFERENCEABLE) ? n.getIdentifier() : n.getPath();
                                                    AddedNodeFact rn = eventsMap.get(key);
                                                    if (rn == null) {
                                                        rn = new AddedNodeFact(n);
                                                        eventsMap.put(key, rn);
                                                    }
                                                    list.add(new DeletedPropertyFact(rn, propertyName));
                                                } catch (PathNotFoundException e) {
                                                    // ignore if parent has also been deleted ?
                                                }
                                            }
                                        }
                                    }
                                } catch (PathNotFoundException pnfe) {
                                    logger.error("Error when executing event. Unable to find node or property for path: " + event.getPath(), pnfe);
                                } catch (Exception e) {
                                    logger.error("Error when executing event", e);
                                }
                            }
                            if (!list.isEmpty()) {
                                long time = System.currentTimeMillis();
                                if (logger.isDebugEnabled()) {
                                    if (list.size() > 3) {
                                        logger.debug("Executing rules for " + list.subList(0, 3) + " ... and " + (list.size() - 3) + " other nodes");
                                    } else {
                                        logger.debug("Executing rules for " + list);
                                    }
                                }
                                final List<Updateable> delayedUpdates = new ArrayList<Updateable>();


                                Map<String, Object> globals = getGlobals(userId, delayedUpdates);

                                executeRules(list, globals);

                                if (list.size() > 3) {
                                    logger.info("Rules executed for " + workspace + " " + list.subList(0, 3) + " ... and " + (list.size() - 3) + " other nodes in " + (System.currentTimeMillis() - time) + "ms");
                                } else {
                                    logger.info("Rules executed for " + workspace + " " + list + " in " + (System.currentTimeMillis() - time) + "ms");
                                }

                                if (s.hasPendingChanges()) {
                                    inRules.set(Boolean.TRUE);
                                    try {
                                        s.save();
                                    } finally {
                                        inRules.set(null);
                                    }
                                }

                                if (!delayedUpdates.isEmpty()) {
                                    TimerTask t = new DelayedUpdatesTimerTask(userId, delayedUpdates);
                                    rulesTimer.schedule(t, UPDATE_DELAY_FOR_LOCKED_NODE);
                                }

                                
//                                Set<Object> objects = new HashSet<Object>();
//                                for (Iterator<Object> iterator = list.iterator(); iterator.hasNext();) {
//                                    Object o = iterator.next();
//                                    if (o instanceof NodeWrapper) {
//                                        objects.add(o);
//                                    } else if (o instanceof PropertyWrapper) {
//                                        objects.add(((PropertyWrapper) o).getNode());
//                                    }
//                                }
//                                for (Iterator<Object> iterator = objects.iterator(); iterator.hasNext();) {
//                                    NodeWrapper nodeWrapper = (NodeWrapper) iterator.next();
//                                    Node n = nodeWrapper.getNode();
////                        if (n.isNodeType(Constants.MIX_VERSIONABLE)) {
////                            n.checkin();
////                            n.checkout();
////                        }
//                                }
                            }
                            return null;
                        }
                    });
        } catch (Exception e) {
            logger.error("Error when executing event", e);
        }
    }

    public Map<String, Object> getGlobals(String username, List<Updateable> delayedUpdates) {
        Map<String, Object> globals = new HashMap<String, Object>();

        globals.put("logger", logger);
        globals.put("user", new User(username));
        globals.put("workspace", workspace);
        globals.put("delayedUpdates", delayedUpdates);
        for (Map.Entry<String, Object> entry : globalObjects.entrySet()) {
            globals.put(entry.getKey(),entry.getValue());
        }
        return globals;
    }

    public void addRulesDescriptor(File file) {
        dslFiles.add(file);
    }

    public void setGlobalObjects(Map<String, Object> globalObjects) {
        this.globalObjects = globalObjects;
    }

    public void addGlobalObject(String key, Object value) {
        globalObjects.put(key,value);
    }

    class DelayedUpdatesTimerTask extends TimerTask {
        private String username;
        private List<Updateable> updates;
        private int count = 1;

        DelayedUpdatesTimerTask(String username, List<Updateable> updates) {
            this.username = username;
            this.updates = updates;
        }

        DelayedUpdatesTimerTask(String username, List<Updateable> updates, int count) {
            this.username = username;
            this.updates = updates;
            this.count = count;
        }

        public void run() {
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper s) throws RepositoryException {
                        List<Updateable> newDelayed = new ArrayList<Updateable>();

                        for (Updateable p : updates) {
                            p.doUpdate(s, newDelayed);
                        }
                        s.save();
                        if (!newDelayed.isEmpty()) {
                            updates = newDelayed;
                            if (count < 3) {
                                rulesTimer.schedule(new DelayedUpdatesTimerTask(username, newDelayed, count + 1), UPDATE_DELAY_FOR_LOCKED_NODE * count);
                            } else {
                                logger.error("Node still locked, max count reached, forget pending changes");
                            }
                        }
                        return null;
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Cannot set property", e);
            }
        }
    }

    public static List<RulesListener> getInstances() {
        return instances;
    }
}