/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 * http://www.jahia.com
 *
 * Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 *
 * 1/ GPL
 * ==================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ===================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render.scripting.bundle;

import org.osgi.framework.Bundle;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.List;

/**
 * A {@link ScriptEngineFactory} implementation that makes sure that {@link ScriptEngine} are created using the
 * appropriate class loader as some implementations have this requirement in order to work properly in an OSGi
 * environment.
 */
class BundleScriptEngineFactory implements ScriptEngineFactory {

    private final ScriptEngineFactory factory;
    private final BundleScriptingContext context;
    private final String wrappedFactoryClassName;
    private final BundleScriptEngineFactoryConfigurator configurator;

    BundleScriptEngineFactory(ScriptEngineFactory factory, BundleScriptEngineFactoryConfigurator configurator,
                              BundleScriptingContext context) {
        this.factory = factory;
        wrappedFactoryClassName = factory.getClass().getCanonicalName();
        this.context = context;
        this.configurator = configurator;
    }

    public String getEngineName() {
        return factory.getEngineName();
    }

    public String getEngineVersion() {
        return factory.getEngineVersion();
    }

    public List<String> getExtensions() {
        return factory.getExtensions();
    }

    public String getLanguageName() {
        return factory.getLanguageName();
    }

    public String getLanguageVersion() {
        return factory.getLanguageVersion();
    }

    public String getMethodCallSyntax(String obj, String m, String... args) {
        return factory.getMethodCallSyntax(obj, m, args);
    }

    public List<String> getMimeTypes() {
        return factory.getMimeTypes();
    }

    public List<String> getNames() {
        return factory.getNames();
    }

    public String getOutputStatement(String toDisplay) {
        return factory.getOutputStatement(toDisplay);
    }

    public Object getParameter(String key) {
        return factory.getParameter(key);
    }

    public String getProgram(String... statements) {
        return factory.getProgram(statements);
    }

    /**
     * Returns an instance of a {@link ScriptEngine} loaded using the class loader that was used to load this
     * {@link ScriptEngineFactory} so that proper class visibility is established.
     *
     * @see ScriptEngineFactory#getScriptEngine()
     */
    public ScriptEngine getScriptEngine() {
        final ClassLoader contextClassLoader = context.getClassLoader();
        ScriptEngine engine;
        if (contextClassLoader != null) {
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            engine = new BundleScriptEngine(factory.getScriptEngine(), this);
            Thread.currentThread().setContextClassLoader(old);
        } else {
            engine = factory.getScriptEngine();
        }
        return engine;
    }

    @Override
    public String toString() {
        return "BundleScriptEngineFactory wrapping " + wrappedFactoryClassName;
    }

    BundleScriptingContext getContext() {
        return context;
    }

    String getWrappedFactoryClassName() {
        return wrappedFactoryClassName;
    }

    private ScriptEngineFactory getWrappedFactory() {
        return factory;
    }

    /**
     * Configures the {@link ScriptEngineFactory} when its bundle is started but before it is registered with the
     * {@link BundleScriptResolver}. Note that this means that Spring context is not available at this time.
     *
     * @param bundle the bundle the factory was loaded from
     */
    void configurePreRegistration(Bundle bundle) {
        if (configurator != null) {
            configurator.configure(getWrappedFactory(), bundle, getContext().getClassLoader());
        }
    }

    /**
     * Performs any clean up operations when the bundle this factory was loaded from is stopped.
     */
    public void destroy() {
        if (configurator != null) {
            configurator.destroy(getWrappedFactory());
        }
    }

    /**
     * Configures this {@link ScriptEngineFactory} if needed right before a {@link javax.script.ScriptEngine}
     * instance is created. This is useful when some configuration details are not yet available when the bundle that
     * declared it is started (e.g. when the configuration is in a Spring context that is not yet available during
     * module startup).
     *
     * Note that this method is called each time a {@link javax.script.ScriptEngine} instance is retrieved so it needs
     * to be efficient, be thread-safe and guard against multiple calls when the configuration needs to happen only
     * once.
     */
    public void configurePreScriptEngineCreation() {
        if (configurator != null) {
            configurator.configurePreScriptEngineCreation(getWrappedFactory());
        }
    }
}